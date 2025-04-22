package com.J2EEWEB.beautyweb.api;

import com.J2EEWEB.beautyweb.entity.Service;
import com.J2EEWEB.beautyweb.entity.User;
import com.J2EEWEB.beautyweb.repository.ServiceRepository;
import com.J2EEWEB.beautyweb.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service")
public class ServiceController {
    @Autowired
    private ServiceRepository serviceRepository;

    @GetMapping("/getAll")
    public ResponseEntity<List<Service>> getAllUsers() {
        List<Service> services = serviceRepository.findAll();
        return new ResponseEntity<>(services, HttpStatus.OK);
    }
}
