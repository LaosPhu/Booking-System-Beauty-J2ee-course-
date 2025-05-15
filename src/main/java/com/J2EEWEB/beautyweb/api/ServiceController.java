package com.J2EEWEB.beautyweb.api;

import com.J2EEWEB.beautyweb.entity.Service;
import com.J2EEWEB.beautyweb.entity.User;
import com.J2EEWEB.beautyweb.repository.ServiceRepository;
import com.J2EEWEB.beautyweb.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

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

    @PutMapping("/edit/{serviceId}")
    public ResponseEntity<Service> updateService(@PathVariable Long serviceId, @RequestBody Service updatedService) {
        Optional<Service> serviceOptional = serviceRepository.findById(serviceId);
        if (serviceOptional.isPresent()) {
            Service service = serviceOptional.get();
            // Cập nhật các trường
            service.setName(updatedService.getName());
            service.setDescription(updatedService.getDescription());
            service.setDuration(updatedService.getDuration());
            service.setPrice(updatedService.getPrice());
            service.setCategory(updatedService.getCategory());
            service.setImageURL(updatedService.getImageURL());

            // Giữ nguyên status hiện tại hoặc có thể cập nhật nếu muốn
            // service.setStatus(updatedService.isStatus());  // Nếu cần

            serviceRepository.save(service);
            return new ResponseEntity<>(service, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
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

    @GetMapping("/images")
    public ResponseEntity<List<String>> getImageList() {
        try {
            // Giả sử thư mục ảnh ở src/main/resources/static/images
            File folder = new File(new ClassPathResource("static/images").getFile().getAbsolutePath());
            String[] imageFiles = folder.list((dir, name) -> name.matches(".*\\.(png|jpg|jpeg|gif)"));
            List<String> images = imageFiles != null ? Arrays.asList(imageFiles) : new ArrayList<>();
            return new ResponseEntity<>(images, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countServices() {
        long count = serviceRepository.count();
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

}
