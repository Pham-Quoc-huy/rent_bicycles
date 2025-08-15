package com.example.service;

import com.example.dto.BookingRequest;
import com.example.dto.BookingResponse;
import com.example.entity.Booking;
import java.util.List;

public interface BookingService {
    
    // Đặt xe
    BookingResponse createBooking(String userEmail, BookingRequest request);
    
    // Lấy danh sách booking của user
    List<BookingResponse> getUserBookings(String userEmail);
    
    // Lấy booking theo ID
    BookingResponse getBookingById(Long bookingId, String userEmail);
    
    // Hủy booking
    BookingResponse cancelBooking(Long bookingId, String userEmail);
    
    // Xác nhận booking (cho admin)
    BookingResponse confirmBooking(Long bookingId);
    
    // Hoàn thành booking (khi user lấy xe)
    BookingResponse completeBooking(Long bookingId, String userEmail);
    
    // Lấy danh sách booking cho admin
    List<BookingResponse> getAllBookings();
    
    // Lấy booking theo trạng thái
    List<BookingResponse> getBookingsByStatus(Booking.BookingStatus status);
    
    // Lấy booking theo station
    List<BookingResponse> getBookingsByStation(Long stationId);
}