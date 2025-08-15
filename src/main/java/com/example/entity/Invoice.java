package com.example.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // Liên kết với khách hàng

    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;  // Liên kết với booking

    @ManyToOne
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;  // Trạm lấy xe

    private Integer bikeQuantity;  // Số lượng xe đã thuê
    private Double totalPrice;  // Tổng số tiền thanh toán
    private String bikeStatus;    // Trạng thái xe (NOT_PICKED_UP, PICKED_UP, RETURNED)

    private LocalDateTime rentalStartTime;  // Thời gian bắt đầu thuê xe (khi quét QR)
    private LocalDateTime rentalEndTime;  // Thời gian trả xe
    private long totalTime;  // Thời gian thuê xe (tính theo giờ)
    
    @Column(nullable = false)
    private LocalDateTime createdAt;  // Thời gian tạo hóa đơn

    // Constructors
    public Invoice() {
        this.createdAt = LocalDateTime.now();
        this.bikeStatus = "NOT_PICKED_UP";
    }
    
    public Invoice(User user, Booking booking, Station station) {
        this();
        this.user = user;
        this.booking = booking;
        this.station = station;
        this.bikeQuantity = booking.getBikeQuantity();
        this.totalPrice = booking.getEstimatedPrice();
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
    
    @Override
    public String toString() {
        return "Invoice{" +
                "id=" + id +
                ", user=" + (user != null ? user.getFullName() : "null") +
                ", station=" + (station != null ? station.getLocation() : "null") +
                ", bikeQuantity=" + bikeQuantity +
                ", totalPrice=" + totalPrice +
                ", bikeStatus='" + bikeStatus + '\'' +
                '}';
    }
}