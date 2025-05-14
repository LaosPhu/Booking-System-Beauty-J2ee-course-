package com.J2EEWEB.beautyweb.entity;
public class GoogleUserInfo {
    private String email;
    private String firstName;
    private String lastName;
    private String picture;
    private String sub; // Google's unique user ID

    // Constructors
    public GoogleUserInfo() {}

    public GoogleUserInfo(String email, String fullName, String picture, String sub) {
        this.email = email;
        this.setNameFromFullName(fullName);
        this.picture = picture;
        this.sub = sub;
    }

    // Helper method to split full name into first and last names
    public void setNameFromFullName(String fullName) {
        if (fullName != null) {
            String[] names = fullName.split(" ");
            this.firstName = names[0];
            this.lastName = names.length > 1 ? names[names.length - 1] : "";
        }
    }

    // Getters and setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }
}