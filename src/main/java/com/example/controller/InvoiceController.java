package com.example.controller;

import com.example.entity.Invoice;
import com.example.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = "*")
public class InvoiceController {
    
    @Autowired
    private InvoiceService invoiceService;
    
    // Lấy hóa đơn theo booking ID
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<Invoice> getInvoiceByBookingId(@PathVariable Long bookingId) {
        Optional<Invoice> invoice = invoiceService.getInvoiceByBookingId(bookingId);
        if (invoice.isPresent()) {
            return ResponseEntity.ok(invoice.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Lấy hóa đơn theo ID
    @GetMapping("/{invoiceId}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable Long invoiceId) {
        Optional<Invoice> invoice = invoiceService.getInvoiceById(invoiceId);
        if (invoice.isPresent()) {
            return ResponseEntity.ok(invoice.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Lấy tất cả hóa đơn của user hiện tại
    @GetMapping("/my-invoices")
    public ResponseEntity<List<Invoice>> getMyInvoices() {
        String userEmail = getCurrentUserEmail();
        if (userEmail == null) {
            return ResponseEntity.badRequest().build();
        }
        
        List<Invoice> invoices = invoiceService.getUserInvoices(userEmail);
        return ResponseEntity.ok(invoices);
    }
    
    // Lấy hóa đơn chưa thanh toán của user
    @GetMapping("/my-unpaid-invoices")
    public ResponseEntity<List<Invoice>> getMyUnpaidInvoices() {
        String userEmail = getCurrentUserEmail();
        if (userEmail == null) {
            return ResponseEntity.badRequest().build();
        }
        
        List<Invoice> invoices = invoiceService.getUserUnpaidInvoices(userEmail);
        return ResponseEntity.ok(invoices);
    }
    
    // Thanh toán hóa đơn (quét QR và lấy xe)
    @PostMapping("/{invoiceId}/pay")
    public ResponseEntity<Invoice> payInvoice(@PathVariable Long invoiceId) {
        try {
            Invoice paidInvoice = invoiceService.payInvoice(invoiceId);
            return ResponseEntity.ok(paidInvoice);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Trả xe về trạm
    @PostMapping("/{invoiceId}/return")
    public ResponseEntity<Invoice> returnBike(@PathVariable Long invoiceId) {
        try {
            Invoice returnedInvoice = invoiceService.returnBike(invoiceId);
            return ResponseEntity.ok(returnedInvoice);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Admin: Lấy tất cả hóa đơn
    @GetMapping("/admin/all")
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        List<Invoice> invoices = invoiceService.getAllInvoices();
        return ResponseEntity.ok(invoices);
    }
    
    // Admin: Lấy hóa đơn theo trạng thái thanh toán
    @GetMapping("/admin/status/{paymentStatus}")
    public ResponseEntity<List<Invoice>> getInvoicesByPaymentStatus(@PathVariable String paymentStatus) {
        List<Invoice> invoices = invoiceService.getInvoicesByPaymentStatus(paymentStatus);
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