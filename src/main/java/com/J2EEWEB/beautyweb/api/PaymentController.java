package com.J2EEWEB.beautyweb.api;

import com.J2EEWEB.beautyweb.Config.Config;
import com.J2EEWEB.beautyweb.entity.Booking; // Import your Booking entity
import com.J2EEWEB.beautyweb.entity.Payment;
import com.J2EEWEB.beautyweb.entity.PaymentRes;
import com.J2EEWEB.beautyweb.repository.BookingRepository;
import com.J2EEWEB.beautyweb.repository.PaymentRepository;
import com.J2EEWEB.beautyweb.service.PaymentService;
import com.J2EEWEB.beautyweb.service.VNPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest; // Use the standard Jakarta Servlet API
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BookingRepository bookingRepository; // Corrected: Use BookingRepository

    @PostMapping("/create-payment") // Changed to POST - Important for sending data
    public ResponseEntity<?> createPayment(
            @RequestBody Map<String, Object> requestBody, // Use a Map to receive the data
            HttpServletRequest req) throws UnsupportedEncodingException {

        // 1. Extract data from the request
        Double totalPrice = (Double) requestBody.get("totalPrice"); // Get totalPrice from request
        Long bookingId = (Long) requestBody.get("bookingId");    // Get bookingId from request.  Use Long
        String bankCode = (String) requestBody.get("bankCode");  // Get bankCode, if provided.

        // 2. Validate data (VERY IMPORTANT)
        if (totalPrice == null || totalPrice <= 0 || bookingId == null) {
            return ResponseEntity.badRequest().body(new PaymentRes("ERROR", "Invalid request data: totalPrice and bookingId are required and totalPrice must be greater than 0.",""));
        }

        // 3. Fetch the booking to associate the payment (Important for your application logic)
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return ResponseEntity.badRequest().body(new PaymentRes("ERROR", "Invalid bookingId: Booking not found.",""));
        }

        // 4. Convert totalPrice to cents (Long is correct for VNPay amounts)
        long amount = (long) (totalPrice * 100);  //  Corrected amount calculation

        // 5. Generate VNPay parameters
        String vnp_TxnRef = Config.getRandomNumber(8);
        String vnp_IpAddr = req.getRemoteAddr(); // Use HttpServletRequest
        String vnp_TmnCode = Config.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", Config.vnp_Version);
        vnp_Params.put("vnp_Command", Config.vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount)); // Use the calculated amount
        vnp_Params.put("vnp_CurrCode", "VND");
        if (bankCode != null && !bankCode.isEmpty()) { // Add bank code if provided
            vnp_Params.put("vnp_BankCode", bankCode);
        }
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef); // Customize order info
        vnp_Params.put("vnp_Locale", "vn");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15); // Set expiration time
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = vnp_Params.get(fieldName);  // No need to cast here
            if (fieldValue != null && !fieldValue.isEmpty()) { // Use isEmpty()
                // Build hash data
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString())).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = Config.hmacSHA512(Config.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = Config.vnp_PayUrl + "?" + queryUrl;

        // 6.  Create a Payment record in your database (Associate with the Booking)
        Payment payment = new Payment();
        payment.setBooking(booking); // Associate with the booking
        payment.setAmount(BigDecimal.valueOf(totalPrice)); // Use BigDecimal for monetary values
        payment.setTransactionId(vnp_TxnRef); // Store the transaction reference
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentStatus("PENDING");  // Initial status
        paymentService.save(payment);

        PaymentRes paymentRes = new PaymentRes();
        paymentRes.setStatus("OK");
        paymentRes.setMessage("Payment initiated.  Redirecting to VNPay.");
        paymentRes.setURL(paymentUrl);  //  Return the URL
        return ResponseEntity.status(HttpStatus.OK).body(paymentRes);
    }

    @GetMapping("/vnpay-callback")  //  Handle the VNPay return
    public ResponseEntity<?> vnpayCallback(HttpServletRequest request) {
        Map<String, String> vnp_Params = new HashMap<>();
        // Get all parameters from the request
        request.getParameterNames().asIterator().forEachRemaining(key -> {
            String value = request.getParameter(key);
            vnp_Params.put(key, value);
        });

        // Get the secure hash and remove it from the parameters.
        String vnp_SecureHash = vnp_Params.get("vnp_SecureHash");
        vnp_Params.remove("vnp_SecureHash");

        // 1.  Verify the signature
      //  boolean isSignatureValid = vnPayService.validateSignature(vnp_Params, vnp_SecureHash);

       // if (isSignatureValid) {
            // Signature is valid.  Process the payment result.
            String vnp_TransactionStatus = vnp_Params.get("vnp_TransactionStatus");
            String vnp_TxnRef = vnp_Params.get("vnp_TxnRef");  // Get the transaction reference

            // Find the Payment by transaction code.
            Payment payment = paymentService.findByTransactionId(vnp_TxnRef);

            if (payment != null) {
                if ("00".equals(vnp_TransactionStatus)) {
                    payment.setPaymentStatus("SUCCESS");
                    //payment.set(vnp_Params.get("vnp_ResponseCode"));
                    paymentService.save(payment);
                    return ResponseEntity.ok("Payment successful!");
                } else {
                    payment.setPaymentStatus("FAILED");
                    //payment.setResponseCode(vnp_Params.get("vnp_ResponseCode"));
                    paymentService.save(payment);
                    return ResponseEntity.badRequest().body("Payment failed: " + vnp_Params.get("vnp_Message"));
                }
            } else {
                return ResponseEntity.badRequest().body("Transaction not found.");
            }
        } //else {
           // return ResponseEntity.badRequest().body("Invalid signature.");
       // }
    }


