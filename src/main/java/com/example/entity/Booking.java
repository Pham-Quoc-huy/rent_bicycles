package com.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;
    
    @Column(nullable = false)
    private Integer bikeQuantity;
    
    @Column(nullable = false)
    private LocalDateTime bookingTime; // Thời gian đặt xe (hiện tại)
    
    @Column
    private LocalDateTime actualPickupTime; // Thời gian thực tế lấy xe (khi quét QR)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;
    
    @Column
    private String notes;
    
    @Column(columnDefinition = "DECIMAL(10,2)")
    private Double estimatedPrice; // Giá ước tính
    
    // Constructors
    public Booking() {
        this.bookingTime = LocalDateTime.now();
        this.status = BookingStatus.PENDING;
    }
    
    public Booking(User user, Station station, Integer bikeQuantity) {
        this();
        this.user = user;
        this.station = station;
        this.bikeQuantity = bikeQuantity;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
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
    
    public BookingStatus getStatus() {
        return status;
    }
    
    public void setStatus(BookingStatus status) {
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
    
    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", user=" + (user != null ? user.getFullName() : "null") +
                ", station=" + (station != null ? station.getLocation() : "null") +
                ", bikeQuantity=" + bikeQuantity +
                ", status=" + status +
                '}';
    }
    
    public enum BookingStatus {
        PENDING,        // Đang chờ xác nhận
        CONFIRMED,      // Đã xác nhận
        CANCELLED,      // Đã hủy
        EXPIRED,        // Hết hạn
        COMPLETED       // Hoàn thành (đã lấy xe)
    }
} 