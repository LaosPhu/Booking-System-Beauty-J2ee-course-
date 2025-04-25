package com.J2EEWEB.beautyweb.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;


@Entity
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serviceId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int duration;

    @Column(nullable = false)
    private BigDecimal price;

    private String category;

    @Column(nullable = true)
    private boolean status = true;

    @Column(nullable = true)
    private String imageURL;
    // Constructors, Getters, Setters
    public Service() {
    }

    public Service(String name, String description, int duration, BigDecimal price, String category,String ImageURL,boolean status) {
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.price = price;
        this.category = category;
        this.imageURL = imageURL;
        this.status = true;
    }

    public Service(Long serviceId, String name, String description, int duration, BigDecimal price, String category, String imageURL,boolean status) {
        this.serviceId = serviceId;
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.price = price;
        this.category = category;
        this.imageURL = imageURL;
        this.status=status;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}