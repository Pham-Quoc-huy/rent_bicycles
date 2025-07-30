package com.example.controller;

import com.example.dto.BookingRequest;
import com.example.dto.BookingResponse;
import com.example.entity.Booking;
import com.example.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {
    
    @Autowired
    private BookingService bookingService;
    
    // Đặt xe
    @PostMapping("/create")
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest request) {
        String userEmail = getCurrentUserEmail();
        if (userEmail == null) {
            return ResponseEntity.badRequest().body(BookingResponse.error("Không tìm thấy thông tin người dùng"));
        }
        
        BookingResponse response = bookingService.createBooking(userEmail, request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Lấy danh sách booking của user hiện tại
    @GetMapping("/my-bookings")
    public ResponseEntity<List<BookingResponse>> getMyBookings() {
        String userEmail = getCurrentUserEmail();
        if (userEmail == null) {
            return ResponseEntity.badRequest().body(List.of());
        }
        
        List<BookingResponse> bookings = bookingService.getUserBookings(userEmail);
        return ResponseEntity.ok(bookings);
    }
    
    // Lấy booking theo ID
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long bookingId) {
        String userEmail = getCurrentUserEmail();
        if (userEmail == null) {
            return ResponseEntity.badRequest().body(BookingResponse.error("Không tìm thấy thông tin người dùng"));
        }
        
        BookingResponse response = bookingService.getBookingById(bookingId, userEmail);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Hủy booking
    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long bookingId) {
        String userEmail = getCurrentUserEmail();
        if (userEmail == null) {
            return ResponseEntity.badRequest().body(BookingResponse.error("Không tìm thấy thông tin người dùng"));
        }
        
        BookingResponse response = bookingService.cancelBooking(bookingId, userEmail);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Hoàn thành booking (lấy xe)
    @PostMapping("/{bookingId}/complete")
    public ResponseEntity<BookingResponse> completeBooking(@PathVariable Long bookingId) {
        String userEmail = getCurrentUserEmail();
        if (userEmail == null) {
            return ResponseEntity.badRequest().body(BookingResponse.error("Không tìm thấy thông tin người dùng"));
        }
        
        BookingResponse response = bookingService.completeBooking(bookingId, userEmail);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Admin: Lấy tất cả booking
    @GetMapping("/admin/all")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        List<BookingResponse> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }
    
    // Admin: Lấy booking theo trạng thái
    @GetMapping("/admin/status/{status}")
    public ResponseEntity<List<BookingResponse>> getBookingsByStatus(@PathVariable Booking.BookingStatus status) {
        List<BookingResponse> bookings = bookingService.getBookingsByStatus(status);
        return ResponseEntity.ok(bookings);
    }
    
    // Admin: Lấy booking theo station
    @GetMapping("/admin/station/{stationId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByStation(@PathVariable Long stationId) {
        List<BookingResponse> bookings = bookingService.getBookingsByStation(stationId);
        return ResponseEntity.ok(bookings);
    }
    
    // Admin: Xác nhận booking
    @PostMapping("/admin/{bookingId}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable Long bookingId) {
        BookingResponse response = bookingService.confirmBooking(bookingId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Helper method để lấy email của user hiện tại
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }
} 