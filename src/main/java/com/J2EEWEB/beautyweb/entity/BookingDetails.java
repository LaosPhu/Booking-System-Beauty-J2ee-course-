package com.J2EEWEB.beautyweb.entity;

import jakarta.persistence.*;

@Entity
public class BookingDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingDetailsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    // Constructors, Getters, Setters

    public BookingDetails() {
    }

    public BookingDetails(Booking booking, Service service) {
        this.booking = booking;
        this.service = service;
    }

    public Long getBookingDetailsId() {
        return bookingDetailsId;
    }

    public void setBookingDetailsId(Long bookingDetailsId) {
        this.bookingDetailsId = bookingDetailsId;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }
}