package com.J2EEWEB.beautyweb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
public class BookingDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingDetailsId;

    @Column(name="booking_id", nullable = false)
    private long bookingId;

    @Column(name = "service_id", nullable = false)
    private long serviceId;

    // Constructors, Getters, Setters

    public BookingDetails() {
    }

    public BookingDetails(long bookingId, long serviceId) {
        this.bookingId = bookingId;
        this.serviceId = serviceId;
    }

    public Long getBookingDetailsId() {
        return bookingDetailsId;
    }

    public void setBookingDetailsId(Long bookingDetailsId) {
        this.bookingDetailsId = bookingDetailsId;
    }

    public long getBookingId() {
        return bookingId;
    }

    public void setBookingId(long bookingId) {
        this.bookingId = bookingId;
    }

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }
}