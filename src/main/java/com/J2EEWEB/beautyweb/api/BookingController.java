package com.J2EEWEB.beautyweb.api;
import com.J2EEWEB.beautyweb.entity.Booking;
import com.J2EEWEB.beautyweb.entity.User;
import com.J2EEWEB.beautyweb.repository.BookingDetailsRepository;
import com.J2EEWEB.beautyweb.repository.BookingRepository;
import com.J2EEWEB.beautyweb.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.badRequest;

@RestController
@RequestMapping("/api/booking")
public class BookingController {
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingDetailsRepository bookingDetailsRepository;
    @GetMapping("/getAll")
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }
    @GetMapping("/getAllbyID")
    public ResponseEntity<List<Booking>> getAllBookingsByUserID(HttpSession session){
        String username = (String) session.getAttribute("username");
        Optional<User> user = userRepository.findByUsername(username);
        Long userId = user.get().getUserId();
        Optional<List<Booking>> bookings = bookingRepository.findByCustomer(user.get());
        if(bookings.isPresent()) {
            return new ResponseEntity<>(bookings.get(), HttpStatus.OK);
        }
        return  ResponseEntity.badRequest().body(null);

    }
}
