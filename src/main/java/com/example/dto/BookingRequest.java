package com.example.dto;

public class BookingRequest {
    private Long stationId;
    private Integer bikeQuantity = 1; // Mặc định 1 xe
    private String notes;
    
    // Constructors
    public BookingRequest() {}
    
    public BookingRequest(Long stationId) {
        this.stationId = stationId;
        this.bikeQuantity = 1; // Mặc định 1 xe
    }
    
    // Getters and Setters
    public Long getStationId() {
        return stationId;
    }
    
    public void setStationId(Long stationId) {
        this.stationId = stationId;
    }
    
    public Integer getBikeQuantity() {
        return bikeQuantity;
    }
    
    public void setBikeQuantity(Integer bikeQuantity) {
        this.bikeQuantity = bikeQuantity != null ? bikeQuantity : 1; // Đảm bảo mặc định 1
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    @Override
    public String toString() {
        return "BookingRequest{" +
                "stationId=" + stationId +
                ", bikeQuantity=" + bikeQuantity +
                ", notes='" + notes + '\'' +
                '}';
    }
} 