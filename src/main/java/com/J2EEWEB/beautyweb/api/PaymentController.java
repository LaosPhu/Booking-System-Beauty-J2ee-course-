package com.J2EEWEB.beautyweb.api;

import com.J2EEWEB.beautyweb.Config.Config;
import com.J2EEWEB.beautyweb.entity.Booking;
import com.J2EEWEB.beautyweb.entity.Payment;
import com.J2EEWEB.beautyweb.entity.PaymentRes;
import com.J2EEWEB.beautyweb.repository.BookingRepository;
import com.J2EEWEB.beautyweb.repository.PaymentRepository;
import com.J2EEWEB.beautyweb.service.PaymentService;
import com.J2EEWEB.beautyweb.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView; // Import RedirectView
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
    private BookingRepository bookingRepository;


    @GetMapping("/getByBooking/{bookingId}")
    public ResponseEntity<Payment> getByBooking(@PathVariable long bookingId) {
        Payment payment = paymentService.findByBooking(bookingId);
        if(payment ==null){
            return new ResponseEntity<>(null,HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(payment, HttpStatus.OK);
    }

    @PostMapping("/create-payment")
    public ResponseEntity<?> createPayment(
            @RequestParam("totalPrice") BigDecimal totalPrice,
            @RequestParam("bookingId") Long bookingId,
            @RequestParam(value = "bankCode", defaultValue = "NCB") String bankCode,
            HttpServletRequest req) throws UnsupportedEncodingException {


        // Validate bank code
        List<String> validBankCodes = Arrays.asList("NCB", "VNBANK", "INTCARD");
        if (!validBankCodes.contains(bankCode)) {
            bankCode = "NCB"; // Default to NCB if invalid
        }
        String orderInfo = "Online payment for bookingId " + bookingId;
        String orderType = "other";

        if (totalPrice == null || bookingId == 0) {
            return ResponseEntity.badRequest().body(new PaymentRes("ERROR", "Invalid request data: totalPrice and bookingId are required and totalPrice must be greater than 0.", ""));
        }

        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return ResponseEntity.badRequest().body(new PaymentRes("ERROR", "Invalid bookingId: Booking not found.", ""));
        }

        long amount = totalPrice.longValue();

        String vnp_TxnRef = Config.getRandomNumber(8);
        String vnp_TmnCode = Config.vnp_TmnCode;
        String vnp_IpAddr = Config.vnp_IpAddr;
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", Config.vnp_Version);
        vnp_Params.put("vnp_Command", Config.vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_BankCode", bankCode);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfo != null ? orderInfo : "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_ReturnUrl", Config.vnp_ReturnUrl); // Use the configured return URL
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        vnp_Params.put("vnp_CurrCode", "VND");

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
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


        Payment checkexisting = paymentService.findByBooking(booking.getBookingId());
        if (checkexisting == null || checkexisting.getPaymentStatus().equals("FAILED")) {
            Payment payment = new Payment();
            payment.setBooking(booking.getBookingId());
            payment.setAmount(totalPrice);
            payment.setTransactionId(vnp_TxnRef);
            payment.setPaymentDate(LocalDateTime.now());
            payment.setPaymentStatus("PENDING");
            paymentService.save(payment);
        } else {
            checkexisting.setAmount(totalPrice);
            checkexisting.setTransactionId(vnp_TxnRef);
            checkexisting.setPaymentDate(LocalDateTime.now());
            checkexisting.setPaymentStatus("PENDING");
            paymentService.save(checkexisting);
        }
        PaymentRes paymentRes = new PaymentRes();
        paymentRes.setStatus("OK");
        paymentRes.setMessage("Payment initiated.  Redirecting to VNPay.");
        paymentRes.setURL(paymentUrl);
        return ResponseEntity.status(HttpStatus.OK).body(paymentRes);
    }

    @GetMapping("/vnpay-callback")
    public RedirectView vnpayCallback(HttpServletRequest request) {
        Map<String, String> vnp_Params = new HashMap<>();
        request.getParameterNames().asIterator().forEachRemaining(key -> {
            String value = request.getParameter(key);
            vnp_Params.put(key, value);
        });

        String vnp_SecureHash = vnp_Params.get("vnp_SecureHash");
        vnp_Params.remove("vnp_SecureHash");

        boolean isSignatureValid = vnPayService.validateSignature(vnp_Params, vnp_SecureHash);

        RedirectView redirectView = new RedirectView(); // Use RedirectView

        if (isSignatureValid) {
            String vnp_TransactionStatus = vnp_Params.get("vnp_TransactionStatus");
            String vnp_TxnRef = vnp_Params.get("vnp_TxnRef");
            String vnp_ResponseCode = vnp_Params.get("vnp_ResponseCode"); // Get the response code.
            String vnp_Message = vnp_Params.get("vnp_Message");  // Get the message

            Payment payment = paymentService.findByTransactionId(vnp_TxnRef);

            if (payment != null) {
                if ("00".equals(vnp_TransactionStatus)) {
                    payment.setPaymentStatus("SUCCESS");
                    payment.setResponseCode(vnp_ResponseCode);
                    paymentService.save(payment);
                    // Redirect to a success page
                    redirectView.setUrl("/payment/paymentsuccess"); //  success URL
                } else {
                    payment.setPaymentStatus("FAILED");
                    payment.setResponseCode(vnp_ResponseCode);
                    paymentService.save(payment);
                    // Redirect to a failure page, include error details
                    redirectView.setUrl("/payment/paymentfailure" ); // failure URL
                }
            } else {
                // Handle the case where the transaction is not found.  This is important!
                redirectView.setUrl("/payment/paymentfailure");
            }
        } else {
            //  Invalid signature.
            redirectView.setUrl("/payment/paymentfailure" );
        }
        return redirectView;
    }
    @PostMapping("/test-payment")
    public ResponseEntity<?> testPayment(
            @RequestParam("totalPrice") BigDecimal totalPrice,
            HttpServletRequest req) throws UnsupportedEncodingException {


        String bankCode = "INTCARD";
        String orderType = "other";
        String orderInfo = "";


        long amount = totalPrice.longValue();

        String vnp_TxnRef = Config.getRandomNumber(8);
        String vnp_TmnCode = Config.vnp_TmnCode;
        String vnp_IpAddr = Config.vnp_IpAddr;
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", Config.vnp_Version);
        vnp_Params.put("vnp_Command", Config.vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_BankCode", "VNPAYQR");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo",  "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_ReturnUrl", Config.vnp_ReturnUrl); // Use the configured return URL
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        vnp_Params.put("vnp_CurrCode", "VND");

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
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



        PaymentRes paymentRes = new PaymentRes();
        paymentRes.setStatus("OK");
        paymentRes.setMessage("Payment initiated.  Redirecting to VNPay.");
        paymentRes.setURL(paymentUrl);
        return ResponseEntity.status(HttpStatus.OK).body(paymentRes);
    }

}
