package com.example.service;

import com.example.entity.Invoice;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface InvoiceService {
    
    // Tạo invoice từ booking
    Invoice createInvoiceFromBooking(Long bookingId);
    
    // Lấy invoice theo ID
    Optional<Invoice> getInvoiceById(Long invoiceId);
    
    // Lấy invoice theo booking ID
    Optional<Invoice> getInvoiceByBookingId(Long bookingId);
    
    // Lấy tất cả invoice của user
    List<Invoice> getUserInvoices(String userEmail);
    

    
    // Trả xe (khi user trả xe về trạm)
    Invoice returnBike(String qrCode, Long returnStationId);
    
    // Đánh dấu xe để trả (admin function)
    Invoice markBikeForReturn(Long invoiceId);
    
    // Lấy tất cả invoice (cho admin)
    List<Invoice> getAllInvoices();
    

    
    // Lấy invoice theo station
    List<Invoice> getInvoicesByStation(Long stationId);
    
    // Thống kê doanh thu
    double getTotalRevenue();
    
    // Thống kê doanh thu theo thời gian
    double getRevenueByDateRange(LocalDateTime startTime, LocalDateTime endTime);
        // Kiểm tra có thể lấy xe không (dựa vào QR code)
    Map<String, Object> checkBikePickup(String qrCode);
        // Lấy xe (sau khi kiểm tra thành công)
    Invoice pickupBike(String qrCode);
}