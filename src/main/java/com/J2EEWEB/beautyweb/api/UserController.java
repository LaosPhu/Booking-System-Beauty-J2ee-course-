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

    }


