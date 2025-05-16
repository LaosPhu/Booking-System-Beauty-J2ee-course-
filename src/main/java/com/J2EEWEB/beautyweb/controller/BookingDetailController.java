package com.J2EEWEB.beautyweb.controller;

import com.J2EEWEB.beautyweb.entity.Booking;
import com.J2EEWEB.beautyweb.entity.BookingDetails;
import com.J2EEWEB.beautyweb.entity.Service;
import com.J2EEWEB.beautyweb.repository.BookingDetailsRepository;
import com.J2EEWEB.beautyweb.repository.BookingRepository;
import com.J2EEWEB.beautyweb.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class BookingDetailController {
    @Autowired
    private BookingDetailsRepository bookingDetailsRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @GetMapping("/booking/details/{bookingId}")
    public String getBookingDetails(@PathVariable Long bookingId, Model model) {
        Optional<Booking> optionalBooking = bookingRepository.getBookingByBookingId(bookingId);
        if (optionalBooking.isEmpty()) {
            return "redirect:/admin-booking";
        }

        Booking booking = optionalBooking.get();
        List<BookingDetails> details = bookingDetailsRepository.findByBookingId(bookingId);

        // Tạo map BookingDetails -> Service
        Map<BookingDetails, Service> detailServiceMap = new LinkedHashMap<>();
        for (BookingDetails detail : details) {
            Service service = serviceRepository.findById(detail.getServiceId()).orElse(null);
            if (service != null) {
                detailServiceMap.put(detail, service);
            }
        }

        model.addAttribute("booking", booking);
        model.addAttribute("detailServiceMap", detailServiceMap);

        return "admin/admin-booking-detail";
    }
}
