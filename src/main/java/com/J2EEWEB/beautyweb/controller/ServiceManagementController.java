package com.J2EEWEB.beautyweb.controller;

import com.J2EEWEB.beautyweb.entity.Service;
import com.J2EEWEB.beautyweb.repository.ServiceRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ServiceManagementController {

    @Autowired
    private ServiceRepository serviceRepository;


    @GetMapping("/admin-service")
    public String adminServicePage(Model model, HttpSession session) {
        List<Service> services = serviceRepository.findAll();
        model.addAttribute("services", services);
        return "admin/admin-service";
    }
}