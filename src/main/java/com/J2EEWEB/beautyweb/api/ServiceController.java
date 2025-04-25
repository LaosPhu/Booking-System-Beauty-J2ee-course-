package com.J2EEWEB.beautyweb.api;

import com.J2EEWEB.beautyweb.entity.Service;
import com.J2EEWEB.beautyweb.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
    @GetMapping("/getById/{serviceId}")
    public ResponseEntity<Service> getById(@PathVariable long serviceId) {
        Optional<Service> service = serviceRepository.findById(serviceId);
        return new ResponseEntity<>(service.get(), HttpStatus.OK);
    }
}
