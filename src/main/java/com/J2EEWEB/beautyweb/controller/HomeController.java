package com.J2EEWEB.beautyweb.controller;

import com.J2EEWEB.beautyweb.entity.Service;
import com.J2EEWEB.beautyweb.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

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
    public String booking(Model model) {
        List<Service> services = serviceRepository.findAll();
        model.addAttribute("services", services);
        return "booking"; // Render the Thymeleaf template
    }
    @GetMapping("/home")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}