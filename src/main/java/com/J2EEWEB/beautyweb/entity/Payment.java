package com.J2EEWEB.beautyweb.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(nullable = false)
    private LocalDateTime paymentDate;

    @Column(nullable = false)
    private String paymentMethod;

    @Column(nullable = false)
    private BigDecimal amount;

    private String transactionId;
    private String paymentStatus;

    // Constructors, Getters, Setters
    public Payment() {
    }

    public Payment(Booking booking, LocalDateTime paymentDate, String paymentMethod, BigDecimal amount, String transactionId, String paymentStatus) {
        this.booking = booking;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.transactionId = transactionId;
        this.paymentStatus = paymentStatus;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}