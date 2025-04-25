package com.J2EEWEB.beautyweb.service;

import com.J2EEWEB.beautyweb.entity.Booking;
import com.J2EEWEB.beautyweb.entity.Payment;
import com.J2EEWEB.beautyweb.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    /**
     * Saves a new payment or updates an existing payment.
     *
     * @param payment The Payment object to save or update.
     * @return The saved Payment object.
     */
    @Transactional
    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }

    /**
     * Retrieves a payment by its unique ID.
     *
     * @param id The ID of the payment to retrieve.
     * @return An Optional containing the Payment object, or an empty Optional if not found.
     */
    public Optional<Payment> findById(Long id) {
        return paymentRepository.findById(id);
    }

    /**
     * Retrieves a payment by its transaction ID.
     *
     * @param transactionId The transaction ID of the payment to retrieve.
     * @return  Payment object, or null if not found.
     */
    public Payment findByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId);
    }

    /**
     * Retrieves all payments.
     *
     * @return A list of all Payment objects.
     */
    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    /**
     * Updates the payment status.
     *
     * @param id     The ID of the payment to update.
     * @param status The new payment status.
     * @return true if the update was successful, false otherwise.
     */
    @Transactional
    public boolean updatePaymentStatus(Long id, String status) {
        Optional<Payment> optionalPayment = paymentRepository.findById(id);
        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();
            payment.setPaymentStatus(status);
            payment.setPaymentDate(LocalDateTime.now()); //update the payment date
            paymentRepository.save(payment);
            return true;
        }
        return false;
    }

    /**
     * Deletes a payment by its ID.
     *
     * @param id The ID of the payment to delete.
     */
    @Transactional
    public void deleteById(Long id) {
        paymentRepository.deleteById(id);
    }

    public Payment findByBooking(long booking) {
        return paymentRepository.findByBooking(booking);
    }
}

