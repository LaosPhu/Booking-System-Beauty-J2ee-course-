package com.J2EEWEB.beautyweb.entity;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(nullable = false)
    private LocalDateTime appointmentDateTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private LocalDate bookingDate;

    private String bookingStatus;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Constructors, Getters, Setters
    public Booking() {
    }

    public Booking(Customer customer, Service service, Staff staff, LocalDateTime appointmentDateTime, LocalDateTime endTime, LocalDate bookingDate, String bookingStatus, String notes) {
        this.customer = customer;
        this.service = service;
        this.staff = staff;
        this.appointmentDateTime = appointmentDateTime;
        this.endTime = endTime;
        this.bookingDate = bookingDate;
        this.bookingStatus = bookingStatus;
        this.notes = notes;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

