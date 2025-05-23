package com.J2EEWEB.beautyweb.entity;


import java.io.Serializable;

public class PaymentRes implements Serializable {
    public String status, message,URL;

    public PaymentRes(){}
    public PaymentRes(String status, String message, String URL) {
        this.status = status;
        this.message = message;
        this.URL = URL;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }
}
