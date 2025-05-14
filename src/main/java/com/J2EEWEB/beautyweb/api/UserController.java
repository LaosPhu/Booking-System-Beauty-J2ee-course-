package com.J2EEWEB.beautyweb.api;
import com.J2EEWEB.beautyweb.entity.User;
import com.J2EEWEB.beautyweb.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @GetMapping("/getAll")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }
    // UserController.java
    @GetMapping("/GetbyEmail/{emailStart}")
    public ResponseEntity<User> getUsersByEmailStart(@PathVariable String emailStart) {
        Optional<User> user = userRepository.findByEmailContaining(emailStart);
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Or return an empty list
        }
        return new ResponseEntity<>(user.get(), HttpStatus.OK);
    }
    @GetMapping("/getbyUsername/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return new ResponseEntity<>(user.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpSession session) {

        // Simulate authentication logic (replace with your actual authentication)
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            if (password.equals(user.get().getPassword())) {
                session.setAttribute("isLoggedIn", true);
                session.setAttribute("userRole", user.get().getRole());
                session.setAttribute("username", username); // Store username
                session.setAttribute("currentUser", user.get());
                Map<String, Object> response = new HashMap<>();
                response.put("role", user.get().getRole());
                response.put("username", username);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();

                response.put("error", "Invalid credentials");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } else {
            Map<String, Object> response = new HashMap<>();

            response.put("error", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            // In a real application, you would validate the password against a stored hash

        }
    }

        @GetMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
            session.invalidate();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Logged out successfully");
            return ResponseEntity.ok()
                    .header("Cache-Control", "no-store, max-age=0, must-revalidate")
                    .header("Pragma", "no-cache")
                    .body(response);

    }
    @GetMapping("/profile")
    public ResponseEntity<User> profile(HttpSession session) {

                    // Now you have the JSESSIONID, but you can't directly get the User object from it.
                    // The JSESSIONID is just an ID, not the actual user data.
                    // You need to retrieve the user data from the session, using the JSESSIONID.
                    if (session != null) {
                        //  session attribute where you stored the user object
                        String username = (String) session.getAttribute("username");
                        Optional<User> user = userRepository.findByUsername(username);
                        if (user != null) {
                            return ResponseEntity.ok(user.get()); // Return the user object.
                        }
                    }
                    // If the session is invalid or the user attribute is not found, return an error.
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
    @PostMapping("/changepassword")
    public ResponseEntity<String> changePassword(HttpSession session,
                                                 @RequestParam("oldPassword") String oldPassword,
                                                 @RequestParam("newPassword") String newPassword,
                                                 @RequestParam("confirmNewPassword") String confirmNewPassword) {
        if (session != null) {
            String username = (String) session.getAttribute("username");
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                // Check if new password and confirmation match
                if (!newPassword.equals(confirmNewPassword)) {
                    return ResponseEntity.badRequest().body("New password and confirmation do not match.");
                }
                //  Additional password strength check (optional)
                if (newPassword.length() < 8) {
                    return ResponseEntity.badRequest().body("New password must be at least 8 characters long.");
                }
                if(newPassword.equals(oldPassword)){
                    return ResponseEntity.badRequest().body("New password matches old password.");
                }
                if (oldPassword.equals(user.get().getPassword())) {
                    user.get().setPassword(newPassword); // In a real application, you would hash the new password before saving it
                    userRepository.save(user.get());
                    return ResponseEntity.ok("Password changed successfully.");
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid old password.");
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found."); //Should not happen
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired. Please log in again.");
    }
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();

        // Kiểm tra bắt buộc các trường
        if (user.getFirstName() == null || user.getLastName() == null ||
                user.getEmail() == null || user.getPhoneNumber() == null ||
                user.getUsername() == null || user.getPassword() == null ||
                user.getAddress() == null) {
            response.put("error", "Missing required fields");
            return ResponseEntity.badRequest().body(response);
        }

        // Kiểm tra độ dài password
        if (user.getPassword().length() < 8) {
            response.put("error", "Password must be at least 8 characters long");
            return ResponseEntity.badRequest().body(response);
        }

        // Kiểm tra định dạng số điện thoại (11 số)
        if (!user.getPhoneNumber().matches("\\d{10}")) {
            response.put("error", "Phone number must be exactly 10 digits");
            return ResponseEntity.badRequest().body(response);
        }

        // Kiểm tra trùng lặp username, email, phone
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            response.put("error", "Username already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            response.put("error", "Email already in use");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        if (userRepository.findByPhoneNumber(user.getPhoneNumber()).isPresent()) {
            response.put("error", "Phone number already in use");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        // Thiết lập các trường mặc định
        user.setRegistrationDate(LocalDateTime.now());
        user.setRole("customer");
        user.setStatus(true);

        userRepository.save(user);
        response.put("message", "User registered successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    public User GetUserfromUsername(HttpSession session){
        String username = (String) session.getAttribute("username");
        Optional<User> user = userRepository.findByUsername(username);
        return user.get();
    }
}


