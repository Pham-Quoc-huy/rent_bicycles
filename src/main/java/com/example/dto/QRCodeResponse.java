package com.example.dto;

import com.example.entity.Invoice;
import com.example.entity.User;
import com.example.entity.Booking;
import com.example.entity.Station;

import java.time.LocalDateTime;

public class QRCodeResponse {
    
    private Long qrCodeId;
    private String qrCode;
    private Long invoiceId;
    
    private User user;
    private Booking booking;
    private Station station;
    private Integer bikeQuantity;
    private Double totalPrice;
    private String bikeStatus;
    private LocalDateTime rentalStartTime;
    private LocalDateTime rentalEndTime;
    private long totalTime;
    private LocalDateTime createdAt;
    
    public QRCodeResponse() {}
    
    public QRCodeResponse(Long qrCodeId, String qrCode, Long invoiceId, Invoice invoice) {
        this.qrCodeId = qrCodeId;
        this.qrCode = qrCode;
        this.invoiceId = invoiceId;
        
        if (invoice != null) {
            this.user = invoice.getUser();
            this.booking = invoice.getBooking();
            this.station = invoice.getStation();
            this.bikeQuantity = invoice.getBikeQuantity();
            this.totalPrice = invoice.getTotalPrice();
            this.bikeStatus = invoice.getBikeStatus();
            this.rentalStartTime = invoice.getRentalStartTime();
            this.rentalEndTime = invoice.getRentalEndTime();
            this.totalTime = invoice.getTotalTime();
            this.createdAt = invoice.getCreatedAt();
        }
    }
    
    // Getters and Setters
    public Long getQrCodeId() {
        return qrCodeId;
    }
    
    public void setQrCodeId(Long qrCodeId) {
        this.qrCodeId = qrCodeId;
    }
    
    public String getQrCode() {
        return qrCode;
    }
    
    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }
    
    public Long getInvoiceId() {
        return invoiceId;
    }
    
    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Booking getBooking() {
        return booking;
    }
    
    public void setBooking(Booking booking) {
        this.booking = booking;
    }
    
    public Station getStation() {
        return station;
    }
    
    public void setStation(Station station) {
        this.station = station;
    }
    
    public Integer getBikeQuantity() {
        return bikeQuantity;
    }
    
    public void setBikeQuantity(Integer bikeQuantity) {
        this.bikeQuantity = bikeQuantity;
    }
    
    public Double getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public String getBikeStatus() {
        return bikeStatus;
    }
    
    public void setBikeStatus(String bikeStatus) {
        this.bikeStatus = bikeStatus;
    }
    
    public LocalDateTime getRentalStartTime() {
        return rentalStartTime;
    }
    
    public void setRentalStartTime(LocalDateTime rentalStartTime) {
        this.rentalStartTime = rentalStartTime;
    }
    
    public LocalDateTime getRentalEndTime() {
        return rentalEndTime;
    }
    
    public void setRentalEndTime(LocalDateTime rentalEndTime) {
        this.rentalEndTime = rentalEndTime;
    }
    
    public long getTotalTime() {
        return totalTime;
    }
    
    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

