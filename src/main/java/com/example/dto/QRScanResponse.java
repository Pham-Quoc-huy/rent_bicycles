package com.example.dto;

import java.time.LocalDateTime;

public class QRScanResponse {
    private Long invoiceId;
    private Integer bikeQuantity;
    private String stationName;
    private String bikeStatus;
    private Double totalPrice;
    private Boolean canProceed;
    private String message;
    private LocalDateTime rentalStartTime;
    private LocalDateTime rentalEndTime;
    private Long totalTime;
    
    // Constructors
    public QRScanResponse() {}
    
    public QRScanResponse(Long invoiceId, Integer bikeQuantity, String stationName, 
                         String bikeStatus, Double totalPrice, Boolean canProceed) {
        this.invoiceId = invoiceId;
        this.bikeQuantity = bikeQuantity;
        this.stationName = stationName;
        this.bikeStatus = bikeStatus;
        this.totalPrice = totalPrice;
        this.canProceed = canProceed;
    }
    
    // Success response
    public static QRScanResponse success(Long invoiceId, Integer bikeQuantity, 
                                       String stationName, String bikeStatus, Double totalPrice) {
        return new QRScanResponse(invoiceId, bikeQuantity, stationName, bikeStatus, totalPrice, true);
    }
    
    // Error response
    public static QRScanResponse error(String message) {
        QRScanResponse response = new QRScanResponse();
        response.setMessage(message);
        response.setCanProceed(false);
        return response;
    }
    
    // Getters and Setters
    public Long getInvoiceId() {
        return invoiceId;
    }
    
    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }
    
    public Integer getBikeQuantity() {
        return bikeQuantity;
    }
    
    public void setBikeQuantity(Integer bikeQuantity) {
        this.bikeQuantity = bikeQuantity;
    }
    
    public String getStationName() {
        return stationName;
    }
    
    public void setStationName(String stationName) {
        this.stationName = stationName;
    }
    
    public String getBikeStatus() {
        return bikeStatus;
    }
    
    public void setBikeStatus(String bikeStatus) {
        this.bikeStatus = bikeStatus;
    }
    
    public Double getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public Boolean getCanProceed() {
        return canProceed;
    }
    
    public void setCanProceed(Boolean canProceed) {
        this.canProceed = canProceed;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
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
    
    public Long getTotalTime() {
        return totalTime;
    }
    
    public void setTotalTime(Long totalTime) {
        this.totalTime = totalTime;
    }
}





