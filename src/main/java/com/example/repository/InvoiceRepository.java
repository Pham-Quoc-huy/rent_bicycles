package com.example.repository;

import com.example.entity.Invoice;
import com.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    // Tìm invoice theo user
    List<Invoice> findByUserOrderByCreatedAtDesc(User user);
    

    

    
    // Tìm invoice theo booking
    Optional<Invoice> findByBookingId(Long bookingId);
    

    
    // Tìm invoice trong khoảng thời gian
    @Query("SELECT i FROM Invoice i WHERE i.createdAt BETWEEN :startTime AND :endTime")
    List<Invoice> findByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, 
                                       @Param("endTime") LocalDateTime endTime);
    

    
    // Tìm invoice theo station
    List<Invoice> findByStationIdOrderByCreatedAtDesc(Long stationId);
} 