package com.example.repository;

import com.example.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {
    
    // Tìm station theo thành phố
    List<Station> findByCity(String city);
    
    // Tìm station có xe khả dụng
    List<Station> findByAvailableBikesGreaterThan(Integer minBikes);
    
    // Tìm station theo địa điểm
    List<Station> findByLocationContainingIgnoreCase(String location);
} 