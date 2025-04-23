package com.J2EEWEB.beautyweb.api;

import com.J2EEWEB.beautyweb.entity.Booking;
import com.J2EEWEB.beautyweb.entity.BookingDetails;
import com.J2EEWEB.beautyweb.entity.Service;
import com.J2EEWEB.beautyweb.entity.User;
import com.J2EEWEB.beautyweb.repository.BookingDetailsRepository;
import com.J2EEWEB.beautyweb.repository.BookingRepository;
import com.J2EEWEB.beautyweb.repository.ServiceRepository;
import com.J2EEWEB.beautyweb.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/booking")
public class BookingRestController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private BookingDetailsRepository bookingDetailsRepository;
    //  Get all bookings.  No authentication required.
    @GetMapping("/getAll")
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    // Get all bookings for the logged-in user.
    @GetMapping("/getAllByUserID")
    public ResponseEntity<List<Booking>> getAllBookingsByUserID(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // Or a message: "User not logged in"
        }
        Optional<User> user = userRepository.findByUsername(username);
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Or a message: "User not found"
        }
        Optional<List<Booking>> bookings = bookingRepository.findByCustomer(user.get());
        if (bookings.isPresent()) {
            return new ResponseEntity<>(bookings.get(), HttpStatus.OK);
        }
        return ResponseEntity.ok(List.of()); // Return an empty list, not a 400 error, if no bookings
    }

    // Create a new booking.  Requires user authentication.
    @PostMapping("/create")
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        Optional<User> user = userRepository.findByUsername(username);
        if (!user.isPresent()) {
            return ResponseEntity.badRequest().body(null);
        }

        booking.setCustomer(user.get()); //  Associate the booking with the logged-in user.
        Booking savedBooking = bookingRepository.save(booking);
        return new ResponseEntity<>(savedBooking, HttpStatus.CREATED);
    }

    // Get a booking by its ID.  Consider whether this should be restricted to the user who made the booking.
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id, HttpSession session) {
        Optional<Booking> booking = bookingRepository.findById(id);
        if (booking.isPresent()) {
            //  Additional check:  Only allow access if the logged-in user is the one who made the booking.
            String username = (String) session.getAttribute("username");
            if (username != null) {
                Optional<User> user = userRepository.findByUsername(username);
                if(user.isPresent() && booking.get().getCustomer().getUserId().equals(user.get().getUserId()))
                {
                    return new ResponseEntity<>(booking.get(), HttpStatus.OK);
                }
                else
                {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
                }

            }
            return new ResponseEntity<>(booking.get(), HttpStatus.OK);

        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Update an existing booking.  Should typically be restricted to the user who made the booking.
    @PutMapping("/{id}")
    public ResponseEntity<Booking> updateBooking(@PathVariable Long id, @RequestBody Booking updatedBooking, HttpSession session) {
        Optional<Booking> existingBooking = bookingRepository.findById(id);
        if (existingBooking.isPresent()) {
            Booking booking = existingBooking.get();

            //  Check:  Only allow update if the logged-in user is the one who made the booking.
            String username = (String) session.getAttribute("username");
            if (username != null) {
                Optional<User> user = userRepository.findByUsername(username);
                if(user.isPresent() && booking.getCustomer().getUserId().equals(user.get().getUserId()))
                {
                    updatedBooking.setBookingId(id); // Ensure the ID is set,
                    updatedBooking.setCustomer(booking.getCustomer()); // Keep the original customer
                    Booking savedBooking = bookingRepository.save(updatedBooking);
                    return new ResponseEntity<>(savedBooking, HttpStatus.OK);
                }
                else
                {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Delete a booking. Should typically be restricted to the user who made the booking or an admin.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id, HttpSession session) {
        Optional<Booking> booking = bookingRepository.findById(id);
        if (booking.isPresent()) {
            //  Check:  Only allow deletion if the logged-in user is the one who made the booking.
            String username = (String) session.getAttribute("username");
            if (username != null) {
                Optional<User> user = userRepository.findByUsername(username);
                if(user.isPresent() && booking.get().getCustomer().getUserId().equals(user.get().getUserId()))
                {
                    bookingRepository.deleteById(id);
                    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                }
                else{
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @PostMapping("/confirmCash")
    @Transactional //  Use a transaction to ensure data consistency
    public ResponseEntity<Long> confirmCashBooking(HttpSession session) {
        String username = (String) session.getAttribute("username");
        com.J2EEWEB.beautyweb.controller.BookingController.BookingData bookingData = (com.J2EEWEB.beautyweb.controller.BookingController.BookingData) session.getAttribute("bookingData");
        List<Service> selectedServices = (List<Service>) session.getAttribute("selectedServices");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body(null);
        }
        User user = userOptional.get();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm"); //added time formatter

        LocalDate bookingDate = LocalDate.parse(bookingData.getDate(), dateFormatter);
        LocalTime bookingTime = LocalTime.parse(bookingData.getTime(), timeFormatter);
        LocalDateTime bookingDateTime = LocalDateTime.of(bookingDate,bookingTime);
        // 1. Create Booking
        Booking booking = new Booking();
        booking.setCustomer(user);
        booking.setAppointmentDateTime(bookingDateTime);
        booking.setBookingDate(bookingDate);
        booking.setBookingStatus("Pending");
        booking.setTotalPrice(bookingData.getTotalPrice()); // Initial price is 0
        booking.setPaymentMethod("Cash");
        booking = bookingRepository.save(booking); // Save to get the bookingId
        Long bookingId = booking.getBookingId();

        // 2. Add Booking Details
        //BigDecimal totalPrice = BigDecimal.ZERO; // Use BigDecimal for calculations
        for (Service service : selectedServices) { // Iterate through the List<Service>
            Long serviceId = service.getServiceId(); // Extract the serviceId
            Optional<Service> serviceOptional = serviceRepository.findById(serviceId);
            if (serviceOptional.isPresent()) {
                Service serviceObj = serviceOptional.get(); // Use a different name to avoid shadowing
                BookingDetails bookingDetails = new BookingDetails(booking, serviceObj);
                bookingDetailsRepository.save(bookingDetails);
                //totalPrice = totalPrice.add(service.getPrice()); // Sum prices

            } else {
                // Handle the case where a service ID is invalid.  This is important!
                return ResponseEntity.badRequest().body(null); // Or throw an exception, or log an error
            }
        }


        return ResponseEntity.ok(bookingId); // Return the booking ID
    }
}
