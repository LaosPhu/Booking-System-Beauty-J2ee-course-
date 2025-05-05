package com.J2EEWEB.beautyweb.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "[user]")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phoneNumber;
    private String address;
    private String username;
    private String password;
    private LocalDateTime registrationDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private String role; // "user" or "admin"

    @Column(nullable = true)
    private boolean status;

    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;

    private String providerId; // Google's unique user ID (sub)

    public enum AuthProvider {
        LOCAL, GOOGLE
    }

    // Constructors
    public User() {
    }

    // Constructor for local registration
    public User(String firstName, String lastName, String email, String username,
                String password, String phoneNumber, String address,
                LocalDateTime registrationDate, String notes, String role, boolean status) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.registrationDate = registrationDate;
        this.notes = notes;
        this.role = role;
        this.status = status;
        this.authProvider = AuthProvider.LOCAL;
    }

    // Constructor for Google registration
    public User(String fullName, String email, String providerId) {
        this.setNameFromFullName(fullName);
        this.email = email;
        this.providerId = providerId;
        this.authProvider = AuthProvider.GOOGLE;
        this.registrationDate = LocalDateTime.now();
        this.role = "user";
        this.status = true;
    }

    // Helper method to split full name into first and last names
    public void setNameFromFullName(String fullName) {
        if (fullName != null) {
            String[] names = fullName.split(" ");
            this.firstName = names[0];
            this.lastName = names.length > 1 ? names[names.length - 1] : "";
        }
    }

    // Getters and Setters (unchanged from your original)
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long id) {
        this.userId = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
}