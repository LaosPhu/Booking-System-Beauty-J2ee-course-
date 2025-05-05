package com.J2EEWEB.beautyweb.api;

import com.J2EEWEB.beautyweb.entity.User;
import com.J2EEWEB.beautyweb.entity.GoogleUserInfo;
import com.J2EEWEB.beautyweb.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @GetMapping("/google")
    public void googleAuth(HttpServletResponse response) throws IOException {
        response.sendRedirect("https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=" + clientId + "&" +
                "redirect_uri=http://localhost:8001/api/auth/google/callback&" +
                "response_type=code&" +
                "scope=email%20profile&" +  // Request both email and profile info
                "access_type=offline");
    }

    @GetMapping("/google/callback")
    public ResponseEntity<?> googleCallback(@RequestParam String code, HttpSession session) {
        try {
            // 1. Exchange authorization code for tokens
            String tokens = exchangeCodeForTokens(code);

            // 2. Get user info from Google
            GoogleUserInfo userInfo = getUserInfoFromGoogle(tokens);

            // 3. Find or create user in database
            User user = userRepository.findByEmail(userInfo.getEmail())
                    .orElseGet(() -> {
                        User newUser = new User(
                                userInfo.getFirstName() + " " + userInfo.getLastName(), // Full name
                                userInfo.getEmail(),
                                userInfo.getSub()
                        );

                        // Set username as email if not provided
                        if (newUser.getUsername() == null) {
                            newUser.setUsername(userInfo.getEmail());
                        }

                        return userRepository.save(newUser);
                    });

            // 4. Store user info in session
            session.setAttribute("user", user);
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());

            // 5. Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("user", user);
            response.put("redirectUrl", user.getRole().equals("admin") ? "/admin" : "/");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "status", "error",
                            "message", "Google authentication failed: " + e.getMessage()
                    ));
        }
    }

    private String exchangeCodeForTokens(String code) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("code", code);
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("redirect_uri", "http://localhost:8001/api/auth/google/callback");
        map.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://oauth2.googleapis.com/token",
                request,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to exchange code for tokens: " + response.getBody());
        }

        return response.getBody();
    }

    private GoogleUserInfo getUserInfoFromGoogle(String accessToken) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                "https://people.googleapis.com/v1/people/me?personFields=names,emailAddresses,photos",  // People API endpoint
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        // Parse response
        JsonNode userInfo = new ObjectMapper().readTree(response.getBody());

        GoogleUserInfo info = new GoogleUserInfo();
        info.setEmail(userInfo.path("emailAddresses").get(0).path("value").asText());
        info.setNameFromFullName(userInfo.path("names").get(0).path("displayName").asText());
        info.setPicture(userInfo.path("photos").get(0).path("url").asText());
        info.setSub(userInfo.path("names").get(0).path("metadata").path("source").path("id").asText());

        return info;
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("status", "success", "message", "Logged out successfully"));
    }
    @PostMapping("/google/signin")
    public ResponseEntity<Map<String, Object>> googleSignIn(@RequestBody Map<String, String> request,
                                                            HttpSession session) {
        try {
            String credential = request.get("credential");
            GoogleUserInfo userInfo = verifyGoogleToken(credential);

            // Find or create user
            User user = userRepository.findByEmail(userInfo.getEmail())
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setUsername(userInfo.getEmail()); // Using email as username
                        newUser.setEmail(userInfo.getEmail());
                        newUser.setFirstName(userInfo.getFirstName());
                        newUser.setLastName(userInfo.getLastName());
                        newUser.setRole("user"); // Default role
                        newUser.setPassword(""); // No password for Google users
                        return userRepository.save(newUser);
                    });

            // Set session attributes - SAME AS REGULAR LOGIN
            session.setAttribute("isLoggedIn", true);
            session.setAttribute("userRole", user.getRole());
            session.setAttribute("username", user.getUsername());

            // Return response - SAME STRUCTURE AS REGULAR LOGIN
            Map<String, Object> response = new HashMap<>();
            response.put("role", user.getRole());
            response.put("username", user.getUsername());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Google authentication failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    // Add this helper method to verify Google ID token
    private GoogleUserInfo verifyGoogleToken(String credential) throws IOException {
        // Using Google's tokeninfo endpoint for validation
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + credential;

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Invalid Google ID token");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode tokenInfo = objectMapper.readTree(response.getBody());

        GoogleUserInfo userInfo = new GoogleUserInfo();
        userInfo.setEmail(tokenInfo.get("email").asText());
        userInfo.setNameFromFullName(tokenInfo.get("name").asText());
        userInfo.setPicture(tokenInfo.get("picture").asText());
        userInfo.setSub(tokenInfo.get("sub").asText());

        return userInfo;
    }
}