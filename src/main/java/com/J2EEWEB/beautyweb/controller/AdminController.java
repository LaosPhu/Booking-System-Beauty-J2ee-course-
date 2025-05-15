package com.J2EEWEB.beautyweb.controller;

import com.J2EEWEB.beautyweb.entity.Booking;
import com.J2EEWEB.beautyweb.entity.Service;
import com.J2EEWEB.beautyweb.entity.User;
import com.J2EEWEB.beautyweb.repository.BookingRepository;
import com.J2EEWEB.beautyweb.repository.ServiceRepository;
import com.J2EEWEB.beautyweb.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class AdminController {

    @GetMapping("/admin")
    public String adminPage(Model model, HttpSession session) {
        return "admin/admin-main";
    }
    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/admin-booking")
    public String showBookings(Model model, HttpSession session) {
        List<Booking> bookings = bookingRepository.findAll();
        model.addAttribute("bookings", bookings);
        return "admin/admin-booking";
    }
    @Autowired
    private ServiceRepository serviceRepository;
    @GetMapping("/admin-service")
    public String adminServicePage(Model model, HttpSession session) {
        List<Service> services = serviceRepository.findAll();
        model.addAttribute("services", services);
        return "admin/admin-service";
    }

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/admin-user")
    public String adminUserPage(Model model) {
        List<User> userList = userRepository.findAll();
        model.addAttribute("users", userList);
        return "admin/admin-user";
    }
}
