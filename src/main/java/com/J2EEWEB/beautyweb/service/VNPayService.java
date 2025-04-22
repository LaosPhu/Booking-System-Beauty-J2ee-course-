package com.J2EEWEB.beautyweb.service;
import com.J2EEWEB.beautyweb.Config.VNPayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class VNPayService {

    @Autowired
    private VNPayConfig vnPayConfig;

    /**
     * Build VNPay payment URL.
     *
     * @param amount        Payment amount.
     * @param orderInfo     Order description.
     * @param ipAddress     IP address of the client.
     * @param transactionId Unique transaction ID.
     * @return VNPay payment URL.
     */
    public String createVNPayRequest(long amount, String orderInfo, String ipAddress, String transactionId) throws UnsupportedEncodingException {


        Map<String, String> vnpayParams = new HashMap<>();
        vnpayParams.put("vnp_Version", "2.1.0");
        vnpayParams.put("vnp_Command", "pay");
        vnpayParams.put("vnp_TmnCode", vnPayConfig.getMerchantId());
        vnpayParams.put("vnp_Amount", String.valueOf(amount * 100)); // Amount in cents
        vnpayParams.put("vnp_CurrCode", "VND");
        vnpayParams.put("vnp_TxnRef", transactionId);  // Use the provided transactionId
        vnpayParams.put("vnp_OrderInfo", orderInfo);
        vnpayParams.put("vnp_OrderType", "topup");  //  Type of transaction
        vnpayParams.put("vnp_Locale", "vn"); // Or "en" for English
        vnpayParams.put("vnp_ReturnUrl", "http://localhost:8001/vnpay-payment/vnpay-callback");  // Important:  Your callback URL
        vnpayParams.put("vnp_IpAddr", ipAddress);

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String orderCreateTime = now.format(formatter);
        vnpayParams.put("vnp_CreateDate", orderCreateTime);

        // Add more parameters as needed from VNPay's API documentation

        List<String> fieldNames = new ArrayList<>(vnpayParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = vnpayParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                // Build the query string (for the URL)
                try {
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()))
                            .append("=")
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()))
                            .append("&");
                    // Build the data string for the hash
                    hashData.append(fieldName)
                            .append("=")
                            .append(fieldValue)
                            .append("&");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    // Handle the exception appropriately (e.g., log, throw a custom exception)
                    throw e;
                }
            }
        }
        if (hashData.length() > 0) {
            hashData.deleteCharAt(hashData.length() - 1); // Remove the trailing "&"
        }
        if (query.length() > 0) {
            query.deleteCharAt(query.length() - 1); // Remove the trailing "&"
        }

        String vnp_SecureHash = hmacSHA256(hashData.toString(), vnPayConfig.getSecretKey());  // Use the secret key from config
        vnpayParams.put("vnp_SecureHash", vnp_SecureHash);
        String vnpayUrl = vnPayConfig.getPaymentUrl() + "?" + query.toString();
        return vnpayUrl;
    }

    /**
     * Generate VNPay payment confirmation.
     *
     * @param request
     * @return
     */
    public boolean isVNPayResponseValid(HttpServletRequest request) {
        // Get all parameters from the request
        Map<String, String> vnpayParams = new HashMap<>();
        request.getParameterNames().asIterator().forEachRemaining(key -> {
            String value = request.getParameter(key);
            vnpayParams.put(key, value);
        });

        // Get the secure hash from the response and remove it from the parameter map
        String vnp_SecureHash = vnpayParams.get("vnp_SecureHash");
        if (vnp_SecureHash == null) {
            return false; //  No hash provided
        }
        vnpayParams.remove("vnp_SecureHash");
        vnpayParams.remove("vnp_SecureHashType");

        // Sort the parameters alphabetically
        List<String> fieldNames = new ArrayList<>(vnpayParams.keySet());
        Collections.sort(fieldNames);

        // Build the data string for the hash verification
        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = vnpayParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName)
                        .append("=")
                        .append(fieldValue)
                        .append("&");
            }
        }
        if (hashData.length() > 0) {
            hashData.deleteCharAt(hashData.length() - 1); // Remove the trailing "&"
        }
        // Calculate the expected hash
        String expectedHash = hmacSHA256(hashData.toString(), vnPayConfig.getSecretKey()); // Use the secret key from config

        // Compare the expected hash with the received hash
        return expectedHash.equalsIgnoreCase(vnp_SecureHash);
    }

    /**
     * Generate hash using HMAC-SHA256 algorithm.
     *
     * @param data   Data to hash.
     * @param key    Secret key.
     * @return Hash value.
     */
    private String hmacSHA256(String data, String key) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hashBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            // Convert the byte array to a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            // Handle the exception appropriately (e.g., log, throw a custom exception)
            return ""; // Or throw an exception
        }
    }

    /**
     * Generate a random transaction ID.
     *
     * @return
     */
    public String generateTransactionId() {
        Random random = new Random();
        int n = 10000000 + random.nextInt(90000000);
        return String.valueOf(n);
    }
}