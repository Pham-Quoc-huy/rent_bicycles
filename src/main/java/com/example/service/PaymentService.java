package com.example.service;

import com.example.entity.Payment;
import com.example.dto.PaymentRequest;
import com.example.dto.PaymentResponse;
import com.example.dto.PaymentStatusUpdateRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface PaymentService {
    
    // Tạo payment mới
    PaymentResponse createPayment(PaymentRequest request);
    
    // Lấy payment theo ID
    Payment getPaymentById(Long paymentId);
    
    // Lấy payment theo gateway ID
    Payment getPaymentByGatewayId(String gatewayId);
    
    // Lấy payment theo transaction ID
    Payment getPaymentByTransactionId(String transactionId);
    
    // Lấy tất cả payment của invoice
    List<Payment> getPaymentsByInvoiceId(Long invoiceId);
    
    // Lấy payment theo status
    List<Payment> getPaymentsByStatus(String status);
    
    // Lấy payment theo payment method
    List<Payment> getPaymentsByMethod(String paymentMethod);
    
    // Lấy payment theo user ID
    List<Payment> getPaymentsByUserId(Long userId);
    
    // Lấy payment theo khoảng thời gian
    List<Payment> getPaymentsByDateRange(LocalDateTime startTime, LocalDateTime endTime);
    
    // Cập nhật payment status
    Payment updatePaymentStatus(Long paymentId, PaymentStatusUpdateRequest request);
    
    // Cập nhật payment
    Payment updatePayment(Payment payment);
    
    // Xử lý webhook từ payment gateway
    void processWebhook(String gatewayName, Map<String, Object> webhookData);
    
    // Xử lý payment success
    Payment processPaymentSuccess(String gatewayId, String transactionId);
    
    // Xử lý payment failure
    Payment processPaymentFailure(String gatewayId, String failureReason);
    
    // Hủy payment
    Payment cancelPayment(Long paymentId, String reason);
    
    // Refund payment
    Payment refundPayment(Long paymentId, BigDecimal amount, String reason);
    
    // Kiểm tra payment status từ gateway
    String checkPaymentStatusFromGateway(String gatewayId);
    
    // Tạo payment URL (cho VNPay, MOMO)
    String generatePaymentUrl(Payment payment);
    
    // Tạo QR code (cho các kênh thanh toán hỗ trợ QR)
    String generatePaymentQRCode(Payment payment);
    
    // Xử lý payment expired
    void processExpiredPayments();
    
    // Thống kê payment
    Map<String, Object> getPaymentStatistics(LocalDateTime startTime, LocalDateTime endTime);
    
    // Lấy payment history của user
    List<Payment> getUserPaymentHistory(Long userId, int page, int size);
    
    // Validate payment request
    boolean validatePaymentRequest(PaymentRequest request);
    
    // Tính toán phí giao dịch
    BigDecimal calculateTransactionFee(BigDecimal amount, String paymentMethod);
    
    // Kiểm tra payment có thể refund không
    boolean canRefund(Payment payment);
    
    // Lấy payment summary cho admin
    Map<String, Object> getAdminPaymentSummary();
}

