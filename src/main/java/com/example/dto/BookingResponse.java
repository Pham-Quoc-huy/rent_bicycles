package com.example.dto;

import com.example.entity.Booking;
import java.time.LocalDateTime;

public class BookingResponse {
    private Long id;
    private String userFullName;
    private String stationLocation;
    private Integer bikeQuantity;
    private LocalDateTime bookingTime;
    private LocalDateTime actualPickupTime;
    private Booking.BookingStatus status;
    private String notes;
    private Double estimatedPrice;
    private String message;
    private boolean success;
    
    // Constructors
    public BookingResponse() {}
    
    public BookingResponse(Booking booking, String message, boolean success) {
        this.id = booking.getId();
        this.userFullName = booking.getUser().getFullName();
        this.stationLocation = booking.getStation().getLocation();
        this.bikeQuantity = booking.getBikeQuantity();
        this.bookingTime = booking.getBookingTime();
        this.actualPickupTime = booking.getActualPickupTime();
        this.status = booking.getStatus();
        this.notes = booking.getNotes();
        this.estimatedPrice = booking.getEstimatedPrice();
        this.message = message;
        this.success = success;
    }
    
    // Success response
    public static BookingResponse success(Booking booking, String message) {
        return new BookingResponse(booking, message, true);
    }
    
    // Error response
    public static BookingResponse error(String message) {
        BookingResponse response = new BookingResponse();
        response.setMessage(message);
        response.setSuccess(false);
        return response;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUserFullName() {
        return userFullName;
    }
    
    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }
    
    public String getStationLocation() {
        return stationLocation;
    }
    
    public void setStationLocation(String stationLocation) {
        this.stationLocation = stationLocation;
    }
    
    public Integer getBikeQuantity() {
        return bikeQuantity;
    }
    
    public void setBikeQuantity(Integer bikeQuantity) {
        this.bikeQuantity = bikeQuantity;
    }
    
    public LocalDateTime getBookingTime() {
        return bookingTime;
    }
    
    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }
    
    public LocalDateTime getActualPickupTime() {
        return actualPickupTime;
    }
    
    public void setActualPickupTime(LocalDateTime actualPickupTime) {
        this.actualPickupTime = actualPickupTime;
    }
    
    public Booking.BookingStatus getStatus() {
        return status;
    }
    
    public void setStatus(Booking.BookingStatus status) {
        this.status = status;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public Double getEstimatedPrice() {
        return estimatedPrice;
    }
    
    public void setEstimatedPrice(Double estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    @Override
    public String toString() {
        return "BookingResponse{" +
                "id=" + id +
                ", userFullName='" + userFullName + '\'' +
                ", stationLocation='" + stationLocation + '\'' +
                ", bikeQuantity=" + bikeQuantity +
                ", status=" + status +
                ", success=" + success +
                '}';
    }
} 