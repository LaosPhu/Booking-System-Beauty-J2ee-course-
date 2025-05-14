package com.J2EEWEB.beautyweb.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VNPayConfig {

    @Value("${vnpay.payment.url}")
    private String paymentUrl;

    @Value("${vnpay.merchant.id}")
    private String merchantId;

    @Value("${vnpay.secret.key}")
    private String secretKey;

    //  Getter methods for the properties
    public String getPaymentUrl() {
        return paymentUrl;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getSecretKey() {
        return secretKey;
    }
}