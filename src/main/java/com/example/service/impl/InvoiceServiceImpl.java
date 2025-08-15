package com.example.service.impl;

import com.example.service.InvoiceService;
import com.example.entity.Booking;
import com.example.entity.Invoice;
import com.example.entity.User;
import com.example.repository.BookingRepository;
import com.example.repository.InvoiceRepository;
import com.example.service.QRCodeService;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

@Service
public class InvoiceServiceImpl implements InvoiceService {
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private QRCodeService qrCodeService;
    
    @Override
    public Invoice createInvoiceFromBooking(Long bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy booking");
        }
        
        Booking booking = bookingOpt.get();
        
        // Kiểm tra xem đã có invoice cho booking này chưa
        Optional<Invoice> existingInvoice = invoiceRepository.findByBookingId(bookingId);
        if (existingInvoice.isPresent()) {
            return existingInvoice.get();
        }
        
        // Tạo invoice mới
        Invoice invoice = new Invoice();
        invoice.setUser(booking.getUser());
        invoice.setBooking(booking);
        invoice.setStation(booking.getStation());
        invoice.setBikeQuantity(booking.getBikeQuantity());
        invoice.setTotalPrice(booking.getEstimatedPrice());
        invoice.setBikeStatus("NOT_PICKED_UP");
        
        // Lưu invoice TRƯỚC để có ID
        invoice = invoiceRepository.save(invoice);
        
        // Tạo QR code đơn giản chỉ chứa invoice ID
        String qrCode = generateQRCode(invoice);
        
        // Tạo QR code trong database thông qua QRCodeService
        qrCodeService.createQRCode(qrCode, invoice.getId());
        
        // Lưu lại invoice
        return invoiceRepository.save(invoice);
    }
    
    // Tạo QR code đơn giản chỉ chứa invoice ID
    private String generateQRCode(Invoice invoice) {
        return String.format("INVOICE:%d", invoice.getId());
    }
    



    
    @Override
    public Optional<Invoice> getInvoiceById(Long invoiceId) {
        return invoiceRepository.findById(invoiceId);
    }
    
    @Override
    public Optional<Invoice> getInvoiceByBookingId(Long bookingId) {
        return invoiceRepository.findByBookingId(bookingId);
    }
    
    @Override
    public List<Invoice> getUserInvoices(String userEmail) {
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            return List.of();
        }
        
        return invoiceRepository.findByUserOrderByCreatedAtDesc(userOpt.get());
    }
    

    
    @Override
    public Invoice returnBike(String qrCode) {
        // Tìm invoice dựa trên QR code
        Optional<Invoice> invoiceOpt = qrCodeService.findInvoiceByQrCode(qrCode);
        if (invoiceOpt.isEmpty()) {
            throw new RuntimeException("QR code không hợp lệ");
        }
        
        Invoice invoice = invoiceOpt.get();
        
        if (!"PICKED_UP".equals(invoice.getBikeStatus())) {
            throw new RuntimeException("Xe chưa được lấy");
        }
        
        LocalDateTime now = LocalDateTime.now();
        invoice.setRentalEndTime(now);
        invoice.setBikeStatus("RETURNED");
        
        // Tính thời gian thuê xe (giờ và phút)
        if (invoice.getRentalStartTime() != null) {
            long totalMinutes = ChronoUnit.MINUTES.between(invoice.getRentalStartTime(), now);
            long hours = totalMinutes / 60;
            long remainingMinutes = totalMinutes % 60;
            
            invoice.setTotalTime(hours);
            
            // Tính giá tiền cơ bản (theo giờ)
            double basePrice = invoice.getBikeQuantity() * 500.0 * Math.max(1, hours);
            
            // Tính phí phạt nếu trả xe quá giờ thuê
            double penaltyFee = 0.0;
            if (remainingMinutes > 0) {
                // Mỗi phút quá giờ = 9.0đ
                penaltyFee = remainingMinutes * 9.0;
            }
            
            // Tổng giá tiền = giá cơ bản + phí phạt
            double actualPrice = basePrice + penaltyFee;
            invoice.setTotalPrice(actualPrice);
        }
        
        return invoiceRepository.save(invoice);
    }
    
    @Override
    public Invoice markBikeForReturn(Long invoiceId) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
        if (invoiceOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy invoice");
        }
        
        Invoice invoice = invoiceOpt.get();
        
        // Kiểm tra trạng thái xe - chỉ cho phép đánh dấu trả khi đã lấy xe
        if (!"PICKED_UP".equals(invoice.getBikeStatus())) {
            throw new RuntimeException("Xe chưa được lấy. Không thể đánh dấu trả.");
        }
        
        // Cập nhật trạng thái xe thành RETURNED (đã đánh dấu trả)
        invoice.setBikeStatus("RETURNED");
        
        return invoiceRepository.save(invoice);
    }
    
    @Override
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }
    

    
    @Override
    public List<Invoice> getInvoicesByStation(Long stationId) {
        return invoiceRepository.findByStationIdOrderByCreatedAtDesc(stationId);
    }
    
    @Override
    public double getTotalRevenue() {
        List<Invoice> allInvoices = invoiceRepository.findAll();
        return allInvoices.stream()
                .mapToDouble(Invoice::getTotalPrice)
                .sum();
    }
    
    @Override
    public double getRevenueByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        List<Invoice> invoices = invoiceRepository.findByCreatedAtBetween(startTime, endTime);
        return invoices.stream()
                .mapToDouble(Invoice::getTotalPrice)
                .sum();
    }
    
    @Override
    public Map<String, Object> checkBikePickup(String qrCode) {
        // Tìm invoice dựa trên QR code
        Optional<Invoice> invoiceOpt = qrCodeService.findInvoiceByQrCode(qrCode);
        if (invoiceOpt.isEmpty()) {
            throw new RuntimeException("QR code không hợp lệ");
        }
        
        Invoice invoice = invoiceOpt.get();
        

        
        // Kiểm tra trạng thái xe
        if (!"NOT_PICKED_UP".equals(invoice.getBikeStatus())) {
            throw new RuntimeException("Xe đã được lấy hoặc đã trả");
        }
        
        // Trả về thông tin cần thiết
        Map<String, Object> result = new HashMap<>();
        result.put("invoiceId", invoice.getId());
        result.put("bikeQuantity", invoice.getBikeQuantity());
        result.put("stationName", invoice.getStation().getLocation());
        result.put("canPickup", true);
        
        return result;
    }
    
    @Override
    public Invoice pickupBike(String qrCode) {
        // Tìm invoice dựa trên QR code
        Optional<Invoice> invoiceOpt = qrCodeService.findInvoiceByQrCode(qrCode);
        if (invoiceOpt.isEmpty()) {
            throw new RuntimeException("QR code không hợp lệ");
        }
        
        Invoice invoice = invoiceOpt.get();
        

        
        if (!"NOT_PICKED_UP".equals(invoice.getBikeStatus())) {
            throw new RuntimeException("Xe đã được lấy hoặc đã trả");
        }
        
        // Cập nhật trạng thái xe
        invoice.setBikeStatus("PICKED_UP");
        invoice.setRentalStartTime(LocalDateTime.now());
            
        return invoiceRepository.save(invoice);
    }
}