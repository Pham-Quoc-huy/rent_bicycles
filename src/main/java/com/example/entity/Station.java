package com.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stations")
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String location;  // Địa chỉ trạm
    
    @Column(nullable = false)
    private String city;      // Thành phố
    
    @Column(nullable = false)
    private Integer totalBikes;  // Tổng số xe tại trạm
    
    @Column(nullable = false)
    private Integer availableBikes; // Số lượng xe có sẵn

    // Constructors
    public Station() {}
    
    public Station(String location, String city, Integer totalBikes, Integer availableBikes) {
        this.location = location;
        this.city = city;
        this.totalBikes = totalBikes;
        this.availableBikes = availableBikes;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getTotalBikes() {
        return totalBikes;
    }

    public void setTotalBikes(Integer totalBikes) {
        this.totalBikes = totalBikes;
    }

    public Integer getAvailableBikes() {
        return availableBikes;
    }

    public void setAvailableBikes(Integer availableBikes) {
        this.availableBikes = availableBikes;
    }
    
    @Override
    public String toString() {
        return "Station{" +
                "id=" + id +
                ", location='" + location + '\'' +
                ", city='" + city + '\'' +
                ", totalBikes=" + totalBikes +
                ", availableBikes=" + availableBikes +
                '}';
    }
}