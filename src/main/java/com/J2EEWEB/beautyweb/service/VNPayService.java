
        package com.J2EEWEB.beautyweb.service;

import com.J2EEWEB.beautyweb.Config.Config; // Import your Config class
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

@Service
public class VNPayService {

    /**
     * Validates the signature of the VNPay response.
     *
     * @param vnp_Params  The parameters received from VNPay.
     * @param vnp_SecureHash The secure hash from VNPay.
     * @return true if the signature is valid, false otherwise.
     */
    public boolean validateSignature(Map<String, String> vnp_Params, String vnp_SecureHash) {
        if (vnp_SecureHash == null || vnp_SecureHash.isEmpty()) {
            return false;
        }

        // Create a copy of the parameters map to avoid modifying the original
        Map<String, String> params = new HashMap<>(vnp_Params);

        // Remove the secure hash from the parameters
        params.remove("vnp_SecureHash");

        // Get the list of parameter names
        List<String> fieldNames = new ArrayList<>(params.keySet());

        // Sort the parameter names alphabetically
        Collections.sort(fieldNames);

        // Build the data string to be hashed
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                try {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString())).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                } catch (UnsupportedEncodingException e) {
                    // Handle the exception properly.  Log it and/or return false.
                    e.printStackTrace(); // Log the error
                    return false; // Consider returning false here
                }
            }
        }
        // Calculate the expected hash
        String expectedHash = hmacSHA512(Config.secretKey, hashData.toString()); // Use Config.secretKey

        // Compare the expected hash with the received hash
        return expectedHash.equals(vnp_SecureHash);
    }

    /**
     * Computes the HMAC-SHA512 hash of the input data using the provided key.
     *
     * @param key  The secret key.
     * @param data The data to be hashed.
     * @return The HMAC-SHA512 hash of the data.
     */
    private String hmacSHA512(String key, String data) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(secretKeySpec);
            byte[] hashBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            // Handle the exception properly.  Log it and/or throw a RuntimeException.
            e.printStackTrace(); // Log the error
            throw new RuntimeException("Error calculating HMAC-SHA512 hash: " + e.getMessage()); // Wrap and rethrow
        }
    }

    /**
     * Converts an array of bytes to its hexadecimal string representation.
     *
     * @param bytes The byte array.
     * @return The hexadecimal string representation of the bytes.
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

}
