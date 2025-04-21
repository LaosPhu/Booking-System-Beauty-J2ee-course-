package com.J2EEWEB.beautyweb.controller;

import com.J2EEWEB.beautyweb.entity.Service;
import com.J2EEWEB.beautyweb.repository.ServiceRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private ServiceRepository serviceRepository;
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
    @GetMapping("/booking")
    public String booking(@RequestParam(required = false) Long selectedServiceId,
                          Model model) {
        List<Service> services = serviceRepository.findAll();
        model.addAttribute("services", services);
        model.addAttribute("selectedServiceId", selectedServiceId);

        if (selectedServiceId != null && selectedServiceId != 0) {
            Optional<Service> selectedService = serviceRepository.findById(selectedServiceId);
            selectedService.ifPresent(service -> model.addAttribute("selectedService", service));
        }
        return "booking";
    }
    @PostMapping("/confirm")
    public String confirmBooking(@RequestParam String date,
                                 @RequestParam String time,
                                 @RequestParam String name,
                                 @RequestParam String email,
                                 @RequestParam String phone,
                                 @RequestParam(required = false) String message,
                                 @RequestParam List<Long> serviceId, // Đây là danh sách ID dịch vụ đã chọn
                                 Model model) {

        List<Service> selectedServices = serviceRepository.findAllById(serviceId);
        BigDecimal totalPrice = selectedServices.stream()
                .map(Service::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("selectedServices", selectedServices);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("date", date);
        model.addAttribute("time", time);
        model.addAttribute("name", name);
        model.addAttribute("email", email);
        model.addAttribute("phone", phone);
        model.addAttribute("message", message);

        return "confirm"; // Trả về trang confirm với các dữ liệu đã điền
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
    public String profile(@PathVariable String selection, Model model){
        String fragmentName = "fragments/profile/" + selection + "-fragment";
        model.addAttribute("fragmentName", fragmentName);

        return "profile";
    }
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}