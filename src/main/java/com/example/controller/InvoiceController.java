package com.example.controller;

import com.example.entity.Invoice;
import com.example.dto.InvoiceResponse;
import com.example.service.InvoiceService;
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
        try {
            Invoice invoice = invoiceService.createInvoiceFromBooking(bookingId);
            InvoiceResponse response = new InvoiceResponse(invoice);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
    
    // Lấy hóa đơn theo booking ID
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<?> getInvoiceByBookingId(@PathVariable Long bookingId) {
        Optional<Invoice> invoice = invoiceService.getInvoiceByBookingId(bookingId);
        if (invoice.isPresent()) {
            InvoiceResponse response = new InvoiceResponse(invoice.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
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
            return ResponseEntity.notFound().build();
        }
    }
    
    // Lấy tất cả hóa đơn của user hiện tại
    @GetMapping("/my-invoices")
    public ResponseEntity<?> getMyInvoices() {
        String userEmail = getCurrentUserEmail();
        if (userEmail == null) {
            return ResponseEntity.badRequest().build();
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
        try {
            var result = invoiceService.checkBikePickup(qrCode);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
    
    // Lấy xe (quét QR code)
    @PostMapping("/pickup-by-qr")
    public ResponseEntity<?> pickupBikeByQR(@RequestParam String qrCode) {
        try {
            Invoice invoice = invoiceService.pickupBike(qrCode);
            return ResponseEntity.ok(invoice);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
    
    // User bấm nút RETURNED (đánh dấu muốn trả xe)
    @PostMapping("/{invoiceId}/mark-return")
    public ResponseEntity<?> markBikeForReturn(@PathVariable Long invoiceId) {
        try {
            Invoice invoice = invoiceService.markBikeForReturn(invoiceId);
            return ResponseEntity.ok(invoice);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
    
    // Trả xe về trạm (quét QR code)
    @PostMapping("/return-by-qr")
    public ResponseEntity<?> returnBikeByQR(@RequestParam String qrCode) {
        try {
            Invoice returnedInvoice = invoiceService.returnBike(qrCode);
            return ResponseEntity.ok(returnedInvoice);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
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