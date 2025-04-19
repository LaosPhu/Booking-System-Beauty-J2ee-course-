package com.J2EEWEB.beautyweb.entity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer; // Changed to User



    @Column(nullable = false)
    private LocalDateTime appointmentDateTime;


    @Column(nullable = false)
    private LocalDate bookingDate;

    private String bookingStatus;



    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private String paymentMethod; // "Cash" or "Online"


    // Constructors, Getters, Setters
    public Booking() {
    }

    public Booking(User customer, LocalDateTime appointmentDateTime,  LocalDate bookingDate, String bookingStatus,  BigDecimal totalPrice, String paymentMethod) { //changed customer and staff to User
        this.customer = customer;
        this.appointmentDateTime = appointmentDateTime;
        this.bookingDate = bookingDate;
        this.bookingStatus = bookingStatus;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public User getCustomer() { //changed to User
        return customer;
    }

    public void setCustomer(User customer) { //changed to User
        this.customer = customer;
    }


    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }


    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }


    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}

