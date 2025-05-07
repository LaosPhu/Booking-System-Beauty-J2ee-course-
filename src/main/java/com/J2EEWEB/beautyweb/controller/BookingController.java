package com.J2EEWEB.beautyweb.controller;

import com.J2EEWEB.beautyweb.entity.Service;
import com.J2EEWEB.beautyweb.repository.BookingRepository;
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
public class BookingController {
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private BookingRepository bookingRepository;

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
    @GetMapping("/payment/success")
    public String paymentsuccess(Model model){
        return "payment/paymentsuccess";
    }
    @PostMapping("/confirm")
    public String confirmBooking(@RequestParam String date,
                                 @RequestParam String time,
                                 @RequestParam String name,
                                 @RequestParam String email,
                                 @RequestParam String phone,
                                 @RequestParam(required = false) String message,
                                 @RequestParam List<Long> serviceId, // Đây là danh sách ID dịch vụ đã chọn
                                 Model model,
                                 HttpSession httpSession) {

        List<Service> selectedServices = serviceRepository.findAllById(serviceId);
        BigDecimal totalPrice = selectedServices.stream()
                .map(Service::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        httpSession.setAttribute("bookingData",new BookingData(date,time,message,totalPrice));
        httpSession.setAttribute("selectedServices",selectedServices);
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


    public static class BookingData {
        String date;
        String time;
        String message;
        BigDecimal totalPrice;

        public BookingData(String date, String time, String message,  BigDecimal totalPrice) {
            this.date = date;
            this.time = time;
            this.message = message;
            this.totalPrice = totalPrice;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public BigDecimal getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
        }
    }
    @GetMapping("/payment/paymentsuccess")
    public String paymentSuccess(@RequestParam(value = "bookingId", required = false) Long bookingId, Model model) {
        if (bookingId != null) {
            model.addAttribute("bookingId", bookingId);
        }
        return "Payment/paymentsuccess"; // Return the name of your HTML file (without the .html extension)
    }

    @GetMapping("/payment/paymentfailure")
    public String paymentFailure(@RequestParam(value = "bookingId", required = false) Long bookingId,
                                 @RequestParam(value = "message", required = false) String message,
                                 Model model) {
        if (bookingId != null) {
            model.addAttribute("bookingId", bookingId);
        }
        if (message != null) {
            model.addAttribute("message", message);
        }
        return "Payment/paymentfailure"; // Return the name of your HTML file
    }
}
