package com.example.service;

import com.example.entity.Booking;
import com.example.entity.Invoice;
import com.example.entity.User;
import com.example.repository.BookingRepository;
import com.example.repository.InvoiceRepository;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class InvoiceService {
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Tạo invoice từ booking
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
        // rentalStartTime sẽ được set khi user quét QR và lấy xe
        invoice.setPaymentStatus("NOT_PAID");
        invoice.setBikeStatus("NOT_PICKED_UP");
        
        // Tạo QR code thanh toán
        String qrCode = generateQRCode(invoice);
        invoice.setQrCode(qrCode);
        
        return invoiceRepository.save(invoice);
    }
    
    // Tạo QR code thanh toán
    private String generateQRCode(Invoice invoice) {
        // Tạo QR code với thông tin thanh toán
        String qrData = String.format(
            "INVOICE:%d|AMOUNT:%.0f|USER:%s|STATION:%s|TIME:%s",
            invoice.getId(),
            invoice.getTotalPrice(),
            invoice.getUser().getEmail(),
            invoice.getStation().getLocation(),
            LocalDateTime.now().toString()
        );
        
        // Trong thực tế, bạn sẽ sử dụng thư viện QR code như ZXing
        // Ở đây tôi tạo một mã giả để demo
        return "QR_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    // Lấy invoice theo ID
    public Optional<Invoice> getInvoiceById(Long invoiceId) {
        return invoiceRepository.findById(invoiceId);
    }
    
    // Lấy invoice theo booking ID
    public Optional<Invoice> getInvoiceByBookingId(Long bookingId) {
        return invoiceRepository.findByBookingId(bookingId);
    }
    
    // Lấy tất cả invoice của user
    public List<Invoice> getUserInvoices(String userEmail) {
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            return List.of();
        }
        
        return invoiceRepository.findByUserOrderByCreatedAtDesc(userOpt.get());
    }
    
    // Lấy invoice chưa thanh toán của user
    public List<Invoice> getUserUnpaidInvoices(String userEmail) {
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            return List.of();
        }
        
        return invoiceRepository.findByUserAndPaymentStatus(userOpt.get(), "NOT_PAID");
    }
    
    // Thanh toán invoice (khi user quét QR và lấy xe)
    public Invoice payInvoice(Long invoiceId) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
        if (invoiceOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy invoice");
        }
        
        Invoice invoice = invoiceOpt.get();
        
        if ("PAID".equals(invoice.getPaymentStatus())) {
            throw new RuntimeException("Invoice đã được thanh toán");
        }
        
        // Cập nhật trạng thái thanh toán
        invoice.setPaymentStatus("PAID");
        invoice.setBikeStatus("PICKED_UP");
        invoice.setRentalStartTime(LocalDateTime.now()); // Thời gian thực tế lấy xe
        
        // Cập nhật booking status
        Booking booking = invoice.getBooking();
        booking.setStatus(Booking.BookingStatus.COMPLETED);
        booking.setActualPickupTime(LocalDateTime.now());
        bookingRepository.save(booking);
        
        return invoiceRepository.save(invoice);
    }
    
    // Trả xe (khi user trả xe về trạm)
    public Invoice returnBike(Long invoiceId) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
        if (invoiceOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy invoice");
        }
        
        Invoice invoice = invoiceOpt.get();
        
        if (!"PICKED_UP".equals(invoice.getBikeStatus())) {
            throw new RuntimeException("Xe chưa được lấy");
        }
        
        LocalDateTime now = LocalDateTime.now();
        invoice.setRentalEndTime(now);
        invoice.setBikeStatus("RETURNED");
        
        // Tính thời gian thuê xe (giờ)
        if (invoice.getRentalStartTime() != null) {
            long hours = ChronoUnit.HOURS.between(invoice.getRentalStartTime(), now);
            invoice.setTotalTime(hours);
            
            // Tính lại giá tiền dựa trên thời gian thực tế
            double actualPrice = invoice.getBikeQuantity() * 50000.0 * Math.max(1, hours);
            invoice.setTotalPrice(actualPrice);
        }
        
        return invoiceRepository.save(invoice);
    }
    
    // Lấy tất cả invoice (cho admin)
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }
    
    // Lấy invoice theo trạng thái thanh toán
    public List<Invoice> getInvoicesByPaymentStatus(String paymentStatus) {
        return invoiceRepository.findByPaymentStatus(paymentStatus);
    }
    
    // Lấy invoice theo station
    public List<Invoice> getInvoicesByStation(Long stationId) {
        return invoiceRepository.findByStationIdOrderByCreatedAtDesc(stationId);
    }
    
    // Thống kê doanh thu
    public double getTotalRevenue() {
        List<Invoice> paidInvoices = invoiceRepository.findByPaymentStatus("PAID");
        return paidInvoices.stream()
                .mapToDouble(Invoice::getTotalPrice)
                .sum();
    }
    
    // Thống kê doanh thu theo thời gian
    public double getRevenueByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        List<Invoice> invoices = invoiceRepository.findByCreatedAtBetween(startTime, endTime);
        return invoices.stream()
                .filter(invoice -> "PAID".equals(invoice.getPaymentStatus()))
                .mapToDouble(Invoice::getTotalPrice)
                .sum();
    }
} 