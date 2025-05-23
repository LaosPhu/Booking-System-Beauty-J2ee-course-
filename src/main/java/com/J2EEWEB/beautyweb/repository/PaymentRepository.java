package com.J2EEWEB.beautyweb.repository;

import com.J2EEWEB.beautyweb.entity.Booking;
import com.J2EEWEB.beautyweb.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
    Payment findByTransactionId(String transactionId);

    Payment findByBooking(long booking);
}
