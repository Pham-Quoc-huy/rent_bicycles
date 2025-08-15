package com.example.dto;

import com.example.entity.Invoice;
import com.example.entity.User;
import com.example.entity.Station;
import java.time.LocalDateTime;

public class InvoiceResponse {
    
    private Long id;
    private UserResponse user;
    private StationResponse station;
    private Integer bikeQuantity;
    private Double totalPrice;
    private LocalDateTime rentalStartTime;
    private LocalDateTime rentalEndTime;
    private long totalTime;
    
    // Constructors
    public InvoiceResponse() {}
    
    public InvoiceResponse(Invoice invoice) {
        this.id = invoice.getId();
        this.user = new UserResponse(invoice.getUser());
        this.station = new StationResponse(invoice.getStation());
        this.bikeQuantity = invoice.getBikeQuantity();
        this.totalPrice = invoice.getTotalPrice();
        this.rentalStartTime = invoice.getRentalStartTime();
        this.rentalEndTime = invoice.getRentalEndTime();
        this.totalTime = invoice.getTotalTime();
    }
    
    // Nested DTOs
    public static class UserResponse {
        private Long id;
        private String name;
        private String email;
        private String phone;
        
        public UserResponse(User user) {
            this.id = user.getId();
            this.name = user.getFullName();
            this.email = user.getEmail();
            this.phone = user.getPhone();
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
    
    public static class StationResponse {
        private String name;
        
        public StationResponse(Station station) {
            this.name = station.getLocation();
        }
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
    
    // Main InvoiceResponse Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public UserResponse getUser() { return user; }
    public void setUser(UserResponse user) { this.user = user; }
    
    public StationResponse getStation() { return station; }
    public void setStation(StationResponse station) { this.station = station; }
    
    public Integer getBikeQuantity() { return bikeQuantity; }
    public void setBikeQuantity(Integer bikeQuantity) { this.bikeQuantity = bikeQuantity; }
    
    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
    
    public LocalDateTime getRentalStartTime() { return rentalStartTime; }
    public void setRentalStartTime(LocalDateTime rentalStartTime) { this.rentalStartTime = rentalStartTime; }
    
    public LocalDateTime getRentalEndTime() { return rentalEndTime; }
    public void setRentalEndTime(LocalDateTime rentalEndTime) { this.rentalEndTime = rentalEndTime; }
    
    public long getTotalTime() { return totalTime; }
    public void setTotalTime(long totalTime) { this.totalTime = totalTime; }
}

