package com.J2EEWEB.beautyweb.repository;

import com.J2EEWEB.beautyweb.entity.Booking;
import com.J2EEWEB.beautyweb.entity.BookingDetails;
import com.J2EEWEB.beautyweb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingDetailsRepository extends JpaRepository<BookingDetails,Long> {
    List<BookingDetails> findByBookingId(long bookingId);

}
