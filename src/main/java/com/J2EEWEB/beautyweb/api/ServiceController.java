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
import java.util.Optional;

@RestController
@RequestMapping("/api/service")
public class ServiceController {
    @Autowired
    private ServiceRepository serviceRepository;
    @GetMapping("/getAll")
    public ResponseEntity<List<Service>> getAllUsers() {
        List<Service> services = serviceRepository.findByStatusTrue();
        return new ResponseEntity<>(services, HttpStatus.OK);
    }
    @PostMapping("/add")
    public ResponseEntity<Service> addService(@RequestBody Service service) {
        try {
            // Lưu dịch vụ vào cơ sở dữ liệu
            Service savedService = serviceRepository.save(service);
            return new ResponseEntity<>(savedService, HttpStatus.CREATED);
        } catch (Exception e) {
            // Nếu có lỗi xảy ra, trả về lỗi 500
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("/delete/{serviceId}")
    public ResponseEntity<Service> deleteService(@PathVariable Long serviceId) {
        Optional<Service> serviceOptional = serviceRepository.findById(serviceId);
        if (serviceOptional.isPresent()) {
            Service service = serviceOptional.get();
            service.setStatus(false);  // Chuyển status thành false
            serviceRepository.save(service);  // Lưu lại
            return new ResponseEntity<>(service, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}
