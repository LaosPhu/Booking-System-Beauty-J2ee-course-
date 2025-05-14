package com.J2EEWEB.beautyweb.api;

import com.J2EEWEB.beautyweb.entity.*;
import com.J2EEWEB.beautyweb.repository.BookingDetailsRepository;
import com.J2EEWEB.beautyweb.repository.BookingRepository;
import com.J2EEWEB.beautyweb.repository.ServiceRepository;
import com.J2EEWEB.beautyweb.repository.UserRepository;
import com.J2EEWEB.beautyweb.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

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

    @Autowired
    private JavaMailSender mailSender; // Inject the JavaMailSender

    @Autowired
    private PaymentService paymentService;
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
    @GetMapping("/details/{bookingId}")
    public ResponseEntity<List<Service>> getBookingDetailsByBookingId(@PathVariable Long bookingId) {
        //  input validation
        if (bookingId == null || bookingId <= 0) {
            return ResponseEntity.ok(List.of());
        }

        Optional<Booking> booking = bookingRepository.getBookingByBookingId(bookingId); // Use service
        if (!booking.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of());
        }

        List<BookingDetails> bookingDetails = bookingDetailsRepository.findByBookingId(booking.get().getBookingId()); // Use the service

        ArrayList<Long> selectedservices = new ArrayList<>();
        for(BookingDetails bookingDetail: bookingDetails){
            selectedservices.add(bookingDetail.getServiceId());
        }

        List<Service> services = serviceRepository.findAllById(selectedservices);

        return new ResponseEntity<>(services,HttpStatus.OK);
    }
    @PutMapping("/cancel/{bookingId}")
    public ResponseEntity<Booking> cancelBookingbyId(@PathVariable Long bookingId){
        if (bookingId == null || bookingId <= 0) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        Optional<Booking> booking = bookingRepository.getBookingByBookingId(bookingId); // Use service
        if (booking.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        booking.get().setBookingStatus("Cancelled");
        bookingRepository.save(booking.get());

        Payment payment = paymentService.findByBooking(bookingId);
        if(payment == null){
            payment = new Payment();
            payment.setPaymentStatus("Cancelled");
            payment.setAmount(booking.get().getTotalPrice());
            payment.setBooking(bookingId);
            payment.setTransactionId("Cancelled");
            payment.setPaymentDate(LocalDateTime.now());
            payment.setResponseCode("24");
        }
        else{
            payment.setPaymentStatus("Cancelled");
            payment.setTransactionId("Cancelled");
            payment.setPaymentDate(LocalDateTime.now());
            payment.setResponseCode("24");
        }
        paymentService.save(payment);
        return new ResponseEntity<>(booking.get(),HttpStatus.OK);
    }

    @PostMapping("/update-status/{bookingId}")
    public ResponseEntity<?> updateBookingStatus(@PathVariable long bookingId) {
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isEmpty()) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        Booking booking = bookingOptional.get();

        if (booking.getPaymentMethod().equalsIgnoreCase("Cash")) {
            LocalDateTime appointmentTime = booking.getAppointmentDateTime();
            LocalDateTime now = LocalDateTime.now();

            if (now.isAfter(appointmentTime) && booking.getBookingStatus().equalsIgnoreCase("Pending")) {
                booking.setBookingStatus("Completed");
                bookingRepository.save(booking);
            }
        }

        // Create a DTO to represent the booking data for the response.  This prevents Hibernate proxy issues.
        BookingDTO bookingDTO = convertToDTO(booking);

        return new ResponseEntity<>(bookingDTO, HttpStatus.OK);
    }


    //  Example: Get all booking details (for admin or debugging - use with caution)
    @GetMapping("/details/all")
    public ResponseEntity<List<BookingDetails>> getAllBookingDetails() {
        List<BookingDetails> allBookingDetails = bookingDetailsRepository.findAll();
        return ResponseEntity.ok(allBookingDetails);
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
    public ResponseEntity<Map<String, Object>> confirmCashBooking(HttpSession session) {
        try{
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
                BookingDetails bookingDetails = new BookingDetails(booking.getBookingId(), serviceObj.getServiceId());
                bookingDetailsRepository.save(bookingDetails);
                //totalPrice = totalPrice.add(service.getPrice()); // Sum prices

            } else {
                // Handle the case where a service ID is invalid.  This is important!
                return ResponseEntity.badRequest().body(null); // Or throw an exception, or log an error
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("bookingId", bookingId);
        response.put("status", "success");
        response.put("paymentMethod", "Cash");

        sendBookingConfirmationEmail(booking,selectedServices);


            return ResponseEntity.ok(response);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
    }
    }
    @PostMapping("/confirmOnline")
    @Transactional //  Use a transaction to ensure data consistency
    public ResponseEntity<Map<String,Object>> confirmOnlineBooking(HttpSession session) {
        try{
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
        booking.setPaymentMethod("Online");
        booking = bookingRepository.save(booking); // Save to get the bookingId
        Long bookingId = booking.getBookingId();

        // 2. Add Booking Details
        //BigDecimal totalPrice = BigDecimal.ZERO; // Use BigDecimal for calculations
        for (Service service : selectedServices) { // Iterate through the List<Service>
            Long serviceId = service.getServiceId(); // Extract the serviceId
            Optional<Service> serviceOptional = serviceRepository.findById(serviceId);
            if (serviceOptional.isPresent()) {
                Service serviceObj = serviceOptional.get(); // Use a different name to avoid shadowing
                BookingDetails bookingDetails = new BookingDetails(booking.getBookingId(), serviceObj.getServiceId());
                bookingDetailsRepository.save(bookingDetails);
                //totalPrice = totalPrice.add(service.getPrice()); // Sum prices

            } else {
                // Handle the case where a service ID is invalid.  This is important!
                return ResponseEntity.badRequest().body(null); // Or throw an exception, or log an error
            }
        }

        //3 Add payment
        Map<String, Object> response = new HashMap<>();
        response.put("bookingId", bookingId);
        response.put("status", "success");
        response.put("paymentMethod", "Online");

        sendBookingConfirmationEmail(booking,selectedServices);

        return ResponseEntity.ok(response);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
    }
    }

    @GetMapping("/check-slot")
    public ResponseEntity<Map<String, Object>> checkBookingSlot(@RequestParam String date,
                                                                @RequestParam String time) {
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            LocalDate bookingDate = LocalDate.parse(date, dateFormatter);
            LocalTime bookingTime = LocalTime.parse(time, timeFormatter);
            LocalDateTime appointmentDateTime = LocalDateTime.of(bookingDate, bookingTime);

            long count = bookingRepository.countByAppointmentDateTime(appointmentDateTime);

            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            response.put("available", count < 3); // true nếu chưa đủ 3 người

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid date or time format"));
        }
    }
    private void sendBookingConfirmationEmail(Booking booking, List<Service> services) {
        if (booking.getCustomer() != null && booking.getCustomer().getEmail() != null) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(booking.getCustomer().getEmail());
            message.setSubject("Your Spa Booking Confirmation");
            message.setText(buildConfirmationEmailBody(booking, services));

            try {
                mailSender.send(message);
                System.out.println("Confirmation email sent to: " + booking.getCustomer().getEmail());
            } catch (Exception e) {
                System.err.println("Error sending confirmation email: " + e.getMessage());
                // Consider logging the error for further investigation
            }
        } else {
            System.err.println("Cannot send confirmation email: Customer or email is null.");
        }
    }

    private String buildConfirmationEmailBody(Booking booking, List<Service> services) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(booking.getCustomer().getLastName()).append(" ").append(booking.getCustomer().getFirstName()).append(",\n\n");
        body.append("Thank you for your booking at our spa!\n\n");
        body.append("Your booking details are as follows:\n");
        body.append("Booking ID: ").append(booking.getBookingId()).append("\n");
        body.append("Date: ").append(booking.getAppointmentDateTime().format(dateFormatter)).append("\n");
        body.append("Time: ").append(booking.getAppointmentDateTime().format(timeFormatter)).append(" (").append(java.time.ZoneId.of("Asia/Ho_Chi_Minh")).append(")\n"); // Added timezone for clarity
        body.append("Payment Method: ").append(booking.getPaymentMethod()).append("\n");
        body.append("Total Price: ").append(String.format("%.2f VND", booking.getTotalPrice())).append("\n\n");

        body.append("Selected Services:\n");
        for (Service service : services) {
            body.append("- ").append(service.getName()).append(" (").append(service.getDuration()).append(" mins) - ").append(String.format("%.2f VND", service.getPrice())).append("\n");
        }
        body.append("\n");

        if (booking.getMessage() != null && !booking.getMessage().isEmpty()) {
            body.append("Special Request: ").append(booking.getMessage()).append("\n\n");
        }

        body.append("Please check your profile history for further information.\n");
        body.append("We look forward to welcoming you. If you have any questions or need to make changes to your booking, please contact us.\n\n");
        body.append("Sincerely,\nThe Bliss Spa Team");

        return body.toString();
    }
    private static class BookingDTO {
        private Long bookingId;
        private UserDTO customer;
        private LocalDateTime appointmentDateTime;
        private LocalDate bookingDate;
        private String bookingStatus;
        private BigDecimal totalPrice;
        private String paymentMethod;
        private String message;

        public BookingDTO(Long bookingId, UserDTO customer, LocalDateTime appointmentDateTime, LocalDate bookingDate, String bookingStatus, BigDecimal totalPrice, String paymentMethod, String message) {
            this.bookingId = bookingId;
            this.customer = customer;
            this.appointmentDateTime = appointmentDateTime;
            this.bookingDate = bookingDate;
            this.bookingStatus = bookingStatus;
            this.totalPrice = totalPrice;
            this.paymentMethod = paymentMethod;
            this.message = message;
        }

        public Long getBookingId() {
            return bookingId;
        }

        public UserDTO getCustomer() {
            return customer;
        }

        public LocalDateTime getAppointmentDateTime() {
            return appointmentDateTime;
        }

        public LocalDate getBookingDate() {
            return bookingDate;
        }

        public String getBookingStatus() {
            return bookingStatus;
        }

        public BigDecimal getTotalPrice() {
            return totalPrice;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public String getMessage() {
            return message;
        }
    }

    private static class UserDTO {
        private Long id;
        private String username;
        private String firstName;
        private String lastName;
        // Add other user fields as needed

        public UserDTO(Long id, String username, String firstName, String lastName) {
            this.id = id;
            this.username = username;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public Long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getFirstName() {
            return firstName;
        }
        public String getLastName() {
            return lastName;
        }
    }

    private BookingDTO convertToDTO(Booking booking) {
        User customer = booking.getCustomer();
        UserDTO customerDTO = null;
        if(customer != null){
            customerDTO = new UserDTO(customer.getUserId(), customer.getUsername(), customer.getFirstName(), customer.getLastName());
        }

        return new BookingDTO(
                booking.getBookingId(),
                customerDTO,
                booking.getAppointmentDateTime(),
                booking.getBookingDate(),
                booking.getBookingStatus(),
                booking.getTotalPrice(),
                booking.getPaymentMethod(),
                booking.getMessage()
        );
    }
}


