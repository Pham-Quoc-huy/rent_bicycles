package com.example.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.dto.PaymentRequest;
import com.example.dto.PaymentResponse;
import com.example.dto.PaymentStatusUpdateRequest;
import com.example.entity.Payment;
import com.example.repository.InvoiceRepository;
import com.example.repository.PaymentRepository;
import com.example.service.InvoiceService;
import com.example.service.PaymentGatewayService;
import com.example.service.PaymentService;
import com.example.service.QRCodeService;
import com.example.exception.*;

@Service
public class PaymentServiceImpl implements PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private InvoiceService invoiceService;
    
    @Autowired
    @Qualifier("vnpayGatewayService")
    private PaymentGatewayService vnpayGatewayService;
    
    @Autowired
    @Qualifier("momoGatewayService")
    private PaymentGatewayService momoGatewayService;
    
    @Autowired
    private QRCodeService qrCodeService;
    
    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        // Validate request
        if (!validatePaymentRequest(request)) {
            throw new IllegalArgumentException("Payment request không hợp lệ");
        }
        
        // Kiểm tra invoice có tồn tại không
        Optional<com.example.entity.Invoice> invoiceOpt = invoiceService.getInvoiceById(request.getInvoiceId());
        if (invoiceOpt.isEmpty()) {
            throw new InvoiceNotFoundException("Không tìm thấy invoice với ID: " + request.getInvoiceId());
        }
            
            var invoice = invoiceOpt.get();
            
            // Tạo payment record
            Payment payment = new Payment();
            payment.setInvoiceId(request.getInvoiceId());
            payment.setAmount(request.getAmount());
            payment.setPaymentMethod(request.getPaymentMethod());
            payment.setCustomerEmail(request.getCustomerEmail());
            payment.setCustomerPhone(request.getCustomerPhone());
            payment.setDescription(request.getDescription() != null ? request.getDescription() : "Thanh toán thuê xe");
            payment.setStatus("PENDING");
            payment.setExpiredAt(LocalDateTime.now().plusMinutes(15)); // Hết hạn sau 15 phút
            
            // Lưu payment
            payment = paymentRepository.save(payment);
            
            // Tạo payment session với gateway
            Map<String, Object> gatewaySession = null;
            if ("VNPAY".equalsIgnoreCase(request.getPaymentMethod())) {
                gatewaySession = vnpayGatewayService.createPaymentSession(request);
                payment.setGatewayId((String) gatewaySession.get("gatewayId"));
                payment.setExpiredAt((LocalDateTime) gatewaySession.get("expiresAt"));
            } else if ("MOMO".equalsIgnoreCase(request.getPaymentMethod())) {
                gatewaySession = momoGatewayService.createPaymentSession(request);
                payment.setGatewayId((String) gatewaySession.get("gatewayId"));
                payment.setExpiredAt((LocalDateTime) gatewaySession.get("expiresAt"));
            }
            
            // Lưu lại payment với gateway ID
            payment = paymentRepository.save(payment);
            
            // Tạo response
            PaymentResponse response = new PaymentResponse();
            response.setPaymentId(payment.getId());
            response.setInvoiceId(payment.getInvoiceId());
            response.setAmount(payment.getAmount());
            response.setCurrency(payment.getCurrency());
            response.setPaymentMethod(payment.getPaymentMethod());
            response.setStatus(payment.getStatus());
            response.setGatewayId(payment.getGatewayId());
            response.setCreatedAt(payment.getCreatedAt());
            response.setExpiredAt(payment.getExpiredAt());
            response.setDescription(payment.getDescription());
            
            // Thêm payment URL và QR code nếu có
            if (gatewaySession != null) {
                response.setPaymentUrl((String) gatewaySession.get("paymentUrl"));
                if (gatewaySession.containsKey("qrCode")) {
                    response.setQrCode((String) gatewaySession.get("qrCode"));
                }
            }
            
            return response;
    }
    
    @Override
    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Không tìm thấy payment với ID: " + paymentId));
    }
    
    @Override
    public Payment getPaymentByGatewayId(String gatewayId) {
        return paymentRepository.findByGatewayId(gatewayId)
                .orElseThrow(() -> new PaymentNotFoundException("Không tìm thấy payment với gateway ID: " + gatewayId));
    }
    
    @Override
    public Payment getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new PaymentNotFoundException("Không tìm thấy payment với transaction ID: " + transactionId));
    }
    
    @Override
    public List<Payment> getPaymentsByInvoiceId(Long invoiceId) {
        return paymentRepository.findByInvoiceIdOrderByCreatedAtDesc(invoiceId);
    }
    
    @Override
    public List<Payment> getPaymentsByStatus(String status) {
        return paymentRepository.findByStatus(status);
    }
    
    @Override
    public List<Payment> getPaymentsByMethod(String paymentMethod) {
        return paymentRepository.findByPaymentMethod(paymentMethod);
    }
    
    @Override
    public List<Payment> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserId(userId);
    }
    
    @Override
    public List<Payment> getPaymentsByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        return paymentRepository.findByCreatedAtBetween(startTime, endTime);
    }
    
    @Override
    @Transactional
    public Payment updatePaymentStatus(Long paymentId, PaymentStatusUpdateRequest request) {
        Payment payment = getPaymentById(paymentId);
        
        // Cập nhật status
        payment.setStatus(request.getStatus());
        payment.setUpdatedAt(LocalDateTime.now());
        
        // Cập nhật các trường khác nếu có
        if (request.getTransactionId() != null) {
            payment.setTransactionId(request.getTransactionId());
        }
        if (request.getGatewayResponse() != null) {
            payment.setGatewayResponse(request.getGatewayResponse());
        }
        if (request.getFailureReason() != null) {
            payment.setFailureReason(request.getFailureReason());
        }
        
        // Cập nhật completedAt nếu thành công
        if ("SUCCESS".equals(request.getStatus())) {
            payment.setCompletedAt(LocalDateTime.now());
        }
        
        return paymentRepository.save(payment);
    }
    
    @Override
    @Transactional
    public Payment updatePayment(Payment payment) {
        if (payment.getId() == null) {
            throw new IllegalArgumentException("Payment ID không được null");
        }
        
        // Kiểm tra payment có tồn tại không
        if (!paymentRepository.existsById(payment.getId())) {
            throw new PaymentNotFoundException("Không tìm thấy payment với ID: " + payment.getId());
        }
        
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }
    
    @Override
    public void processWebhook(String gatewayName, Map<String, Object> webhookData) {
        String gatewayId = (String) webhookData.get("gatewayId");
        String status = (String) webhookData.get("status");
        
        if ("SUCCESS".equals(status)) {
            processPaymentSuccess(gatewayId, (String) webhookData.get("transactionId"));
        } else if ("FAILED".equals(status)) {
            processPaymentFailure(gatewayId, (String) webhookData.get("message"));
        }
    }
    
    @Override
    @Transactional
    public Payment processPaymentSuccess(String gatewayId, String transactionId) {
        Payment payment = getPaymentByGatewayId(gatewayId);
        
        // Cập nhật payment status
        payment.setStatus("SUCCESS");
        payment.setTransactionId(transactionId);
        payment.setCompletedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        
        payment = paymentRepository.save(payment);
        
        // Tạo QR code để lấy xe sau khi thanh toán thành công
        createPickupQRCode(payment.getInvoiceId());
        
        // TODO: Gửi notification
        // TODO: Cập nhật invoice status
        
        return payment;
    }
    
    @Override
    @Transactional
    public Payment processPaymentFailure(String gatewayId, String failureReason) {
        Payment payment = getPaymentByGatewayId(gatewayId);
        
        // Cập nhật payment status
        payment.setStatus("FAILED");
        payment.setFailureReason(failureReason);
        payment.setUpdatedAt(LocalDateTime.now());
        
        payment = paymentRepository.save(payment);
        
        // TODO: Gửi notification
        
        return payment;
    }
    
    @Override
    @Transactional
    public Payment cancelPayment(Long paymentId, String reason) {
        Payment payment = getPaymentById(paymentId);
        
        if (!"PENDING".equals(payment.getStatus())) {
            throw new PaymentStatusException("Chỉ có thể hủy payment đang pending");
        }
        
        payment.setStatus("CANCELLED");
        payment.setFailureReason(reason);
        payment.setUpdatedAt(LocalDateTime.now());
        
        return paymentRepository.save(payment);
    }
    
    @Override
    @Transactional
    public Payment refundPayment(Long paymentId, BigDecimal amount, String reason) {
        Payment payment = getPaymentById(paymentId);
        
        if (!"SUCCESS".equals(payment.getStatus())) {
            throw new PaymentStatusException("Chỉ có thể refund payment đã thành công");
        }
        
        if (amount.compareTo(payment.getAmount()) > 0) {
            throw new IllegalArgumentException("Số tiền refund không được lớn hơn số tiền đã thanh toán");
        }
        
        // TODO: Implement refund logic với gateway
        payment.setStatus("REFUNDED");
        payment.setUpdatedAt(LocalDateTime.now());
        
        return paymentRepository.save(payment);
    }
    
    @Override
    public String checkPaymentStatusFromGateway(String gatewayId) {
        // TODO: Implement check status với từng gateway
        return "UNKNOWN";
    }
    
    @Override
    public String generatePaymentUrl(Payment payment) {
        // TODO: Implement generate URL cho từng gateway
        return null;
    }
    
    @Override
    public String generatePaymentQRCode(Payment payment) {
        // TODO: Implement generate QR code cho từng gateway
        return null;
    }
    
    @Override
    @Transactional
    public void processExpiredPayments() {
        List<Payment> expiredPayments = paymentRepository.findExpiredPayments(LocalDateTime.now());
        
        for (Payment payment : expiredPayments) {
            if ("PENDING".equals(payment.getStatus())) {
                payment.setStatus("EXPIRED");
                payment.setUpdatedAt(LocalDateTime.now());
                paymentRepository.save(payment);
            }
        }
    }
    
    @Override
    public Map<String, Object> getPaymentStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        List<Payment> payments = getPaymentsByDateRange(startTime, endTime);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPayments", payments.size());
        stats.put("totalAmount", payments.stream()
                .mapToDouble(p -> p.getAmount().doubleValue())
                .sum());
        stats.put("successfulPayments", payments.stream()
                .filter(p -> "SUCCESS".equals(p.getStatus()))
                .count());
        stats.put("failedPayments", payments.stream()
                .filter(p -> "FAILED".equals(p.getStatus()))
                .count());
        stats.put("pendingPayments", payments.stream()
                .filter(p -> "PENDING".equals(p.getStatus()))
                .count());
        
        return stats;
    }
    
    @Override
    public List<Payment> getUserPaymentHistory(Long userId, int page, int size) {
        // TODO: Implement pagination
        return getPaymentsByUserId(userId);
    }
    
    @Override
    public boolean validatePaymentRequest(PaymentRequest request) {
        return request != null && 
               request.getInvoiceId() != null && 
               request.getAmount() != null && 
               request.getAmount().compareTo(BigDecimal.valueOf(1000)) >= 0 &&
               request.getPaymentMethod() != null && 
               !request.getPaymentMethod().trim().isEmpty() &&
               request.getCustomerEmail() != null && 
               !request.getCustomerEmail().trim().isEmpty();
    }
    
    @Override
    public BigDecimal calculateTransactionFee(BigDecimal amount, String paymentMethod) {
        if ("VNPAY".equalsIgnoreCase(paymentMethod)) {
            return vnpayGatewayService.calculateTransactionFee(amount);
        }
        // Default fee: 2%
        return amount.multiply(BigDecimal.valueOf(0.02));
    }
    
    @Override
    public boolean canRefund(Payment payment) {
        return "SUCCESS".equals(payment.getStatus()) && 
               payment.getCompletedAt() != null &&
               payment.getCompletedAt().isAfter(LocalDateTime.now().minusDays(30)); // Chỉ refund trong 30 ngày
    }
    
    @Override
    public Map<String, Object> getAdminPaymentSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // Tổng số payment
        summary.put("totalPayments", paymentRepository.count());
        summary.put("totalAmount", paymentRepository.findAll().stream()
                .mapToDouble(p -> p.getAmount().doubleValue())
                .sum());
        
        // Payment theo status
        summary.put("pendingPayments", paymentRepository.countByStatus("PENDING"));
        summary.put("successfulPayments", paymentRepository.countByStatus("SUCCESS"));
        summary.put("failedPayments", paymentRepository.countByStatus("FAILED"));
        summary.put("cancelledPayments", paymentRepository.countByStatus("CANCELLED"));
        
        // Payment theo method
        summary.put("vnpayPayments", paymentRepository.countByPaymentMethod("VNPAY"));
        summary.put("stripePayments", paymentRepository.countByPaymentMethod("STRIPE"));
        summary.put("momoPayments", paymentRepository.countByPaymentMethod("MOMO"));
        
        return summary;
    }
    
    // Helper method để tạo QR code lấy xe
    private void createPickupQRCode(Long invoiceId) {
        try {
            // Tạo QR code data cho lấy xe
            String qrData = "PICKUP:" + invoiceId + ":" + System.currentTimeMillis();
            
            // Tạo QR code record
            qrCodeService.createQRCode(qrData, invoiceId, "PICKUP");
            
        } catch (Exception e) {
            // Log error nhưng không throw exception để không ảnh hưởng đến payment
            System.err.println("Lỗi tạo QR code lấy xe: " + e.getMessage());
        }
    }
}
