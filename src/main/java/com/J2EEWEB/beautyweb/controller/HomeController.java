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
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @GetMapping("/contact")
    public String contact() {
        return "redirect:/contact.html";
    }
    @GetMapping("/about")
    public String about() {
        return "redirect:/about.html";
    }
    @GetMapping("/blog")
    public String blog() {
        return "redirect:/blog.html";
    }
    @GetMapping("/service")
    public String service(Model model) {
        List<Service> services = serviceRepository.findAll();
        model.addAttribute("services", services);
        return "service"; // Render the Thymeleaf template
    }
    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null) {
            isLoggedIn = false; // Default to false if not set
        }
        model.addAttribute("isLoggedIn", isLoggedIn);
        return "index";
    }
    @GetMapping("/profile/{selection}")
    public String profile(@PathVariable String selection, Model model,HttpSession session){
        String fragmentName = "fragments/profile/" + selection + "-fragment";
        model.addAttribute("fragmentName", fragmentName);


        return "profile";
    }
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}