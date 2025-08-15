package com.example.controller;

import com.example.entity.Invoice;
import com.example.dto.InvoiceResponse;
import com.example.service.InvoiceService;
import com.example.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = "*")
public class InvoiceController {
    
    @Autowired
    private InvoiceService invoiceService;
    
    // Tạo invoice từ booking
    @PostMapping("/create-from-booking/{bookingId}")
    public ResponseEntity<?> createInvoiceFromBooking(@PathVariable Long bookingId) {
        Invoice invoice = invoiceService.createInvoiceFromBooking(bookingId);
        InvoiceResponse response = new InvoiceResponse(invoice);
        return ResponseEntity.ok(response);
    }
    
    // Lấy hóa đơn theo booking ID
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<?> getInvoiceByBookingId(@PathVariable Long bookingId) {
        Optional<Invoice> invoice = invoiceService.getInvoiceByBookingId(bookingId);
        if (invoice.isPresent()) {
            InvoiceResponse response = new InvoiceResponse(invoice.get());
            return ResponseEntity.ok(response);
        } else {
            throw new InvoiceNotFoundException("Không tìm thấy hóa đơn cho booking ID: " + bookingId);
        }
    }
    
    // Lấy hóa đơn theo ID
    @GetMapping("/{invoiceId}")
    public ResponseEntity<?> getInvoiceById(@PathVariable Long invoiceId) {
        Optional<Invoice> invoice = invoiceService.getInvoiceById(invoiceId);
        if (invoice.isPresent()) {
            InvoiceResponse response = new InvoiceResponse(invoice.get());
            return ResponseEntity.ok(response);
        } else {
            throw new InvoiceNotFoundException("Không tìm thấy hóa đơn với ID: " + invoiceId);
        }
    }
    
    // Lấy tất cả hóa đơn của user hiện tại
    @GetMapping("/my-invoices")
    public ResponseEntity<?> getMyInvoices() {
        String userEmail = getCurrentUserEmail();
        if (userEmail == null) {
            throw new UnauthorizedAccessException("Không tìm thấy thông tin người dùng");
        }
        
        List<Invoice> invoices = invoiceService.getUserInvoices(userEmail);
        List<InvoiceResponse> responses = invoices.stream()
                .map(InvoiceResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    

    
    // Kiểm tra có thể lấy xe không (dựa vào QR code)
    @GetMapping("/check-pickup")
    public ResponseEntity<?> checkBikePickup(@RequestParam String qrCode) {
        var result = invoiceService.checkBikePickup(qrCode);
        return ResponseEntity.ok(result);
    }
    
    // Lấy xe (quét QR code)
    @PostMapping("/pickup-by-qr")
    public ResponseEntity<?> pickupBikeByQR(@RequestParam String qrCode) {
        Invoice invoice = invoiceService.pickupBike(qrCode);
        return ResponseEntity.ok(invoice);
    }
    
    // User bấm nút RETURNED (đánh dấu muốn trả xe)
    @PostMapping("/{invoiceId}/mark-return")
    public ResponseEntity<?> markBikeForReturn(@PathVariable Long invoiceId) {
        Invoice invoice = invoiceService.markBikeForReturn(invoiceId);
        return ResponseEntity.ok(invoice);
    }
    
    // Trả xe về trạm (quét QR code)
    @PostMapping("/return-by-qr")
    public ResponseEntity<?> returnBikeByQR(@RequestParam String qrCode, 
                                          @RequestParam(required = false) Long returnStationId) {
        Invoice returnedInvoice = invoiceService.returnBike(qrCode, returnStationId);
        return ResponseEntity.ok(returnedInvoice);
    }
    
    // Admin: Lấy tất cả hóa đơn
    @GetMapping("/admin/all")
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        List<Invoice> invoices = invoiceService.getAllInvoices();
        return ResponseEntity.ok(invoices);
    }
    

    
    // Admin: Lấy hóa đơn theo station
    @GetMapping("/admin/station/{stationId}")
    public ResponseEntity<List<Invoice>> getInvoicesByStation(@PathVariable Long stationId) {
        List<Invoice> invoices = invoiceService.getInvoicesByStation(stationId);
        return ResponseEntity.ok(invoices);
    }
    
    // Admin: Thống kê doanh thu
    @GetMapping("/admin/revenue")
    public ResponseEntity<Double> getTotalRevenue() {
        double revenue = invoiceService.getTotalRevenue();
        return ResponseEntity.ok(revenue);
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