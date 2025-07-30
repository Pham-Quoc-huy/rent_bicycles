package com.example.repository;

import com.example.entity.Booking;
import com.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    // Tìm tất cả booking của một user
    List<Booking> findByUserOrderByBookingTimeDesc(User user);
    
    // Tìm booking theo trạng thái
    List<Booking> findByStatus(Booking.BookingStatus status);
    
    // Tìm booking theo user và trạng thái
    List<Booking> findByUserAndStatus(User user, Booking.BookingStatus status);
    
    // Đếm số booking theo trạng thái
    long countByStatus(Booking.BookingStatus status);
    
    // Tìm booking theo station
    List<Booking> findByStationIdOrderByBookingTimeDesc(Long stationId);
    
    // Tìm booking theo station và trạng thái
    List<Booking> findByStationIdAndStatus(Long stationId, Booking.BookingStatus status);
    
    // Tìm booking trong khoảng thời gian
    @Query("SELECT b FROM Booking b WHERE b.bookingTime BETWEEN :startTime AND :endTime")
    List<Booking> findByBookingTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);
    
    // Tìm booking đang chờ xác nhận tại một station
    @Query("SELECT b FROM Booking b WHERE b.station.id = :stationId AND b.status = 'PENDING'")
    List<Booking> findPendingBookingsByStation(@Param("stationId") Long stationId);
    
    // Đếm số booking đang chờ xác nhận tại một station
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.station.id = :stationId AND b.status = 'PENDING'")
    long countPendingBookingsByStation(@Param("stationId") Long stationId);
} 