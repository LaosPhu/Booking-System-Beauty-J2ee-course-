package com.J2EEWEB.beautyweb.repository;

import com.J2EEWEB.beautyweb.entity.Booking;
import com.J2EEWEB.beautyweb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking,Long> {
    Optional<List<Booking>> findByCustomer(User customer); // Change parameter type to User

    Optional<Booking> getBookingByBookingId(Long bookingId);
    long countByAppointmentDateTime(LocalDateTime appointmentDateTime);
    boolean existsByCustomerAndAppointmentDateTime(User customer, LocalDateTime appointmentDateTime);
    long count();

}


