package com.example.service;

import com.example.entity.Payment;
import com.example.dto.PaymentRequest;
import java.math.BigDecimal;
import java.util.Map;

public interface PaymentGatewayService {
    
    // Tạo payment session
    Map<String, Object> createPaymentSession(PaymentRequest request);
    
    // Xác thực webhook signature
    boolean verifyWebhookSignature(String payload, String signature);
    
    // Xử lý webhook data
    Map<String, Object> processWebhookData(String payload);
    
    // Kiểm tra payment status
    String checkPaymentStatus(String gatewayId);
    
    // Hủy payment
    boolean cancelPayment(String gatewayId);
    
    // Refund payment
    boolean refundPayment(String gatewayId, BigDecimal amount, String reason);
    
    // Lấy tên gateway
    String getGatewayName();
    
    // Kiểm tra gateway có hỗ trợ payment method không
    boolean supportsPaymentMethod(String paymentMethod);
    
    // Tính phí giao dịch
    BigDecimal calculateTransactionFee(BigDecimal amount);
    
    // Lấy thông tin gateway
    Map<String, Object> getGatewayInfo();
    
    // Validate payment request
    boolean validatePaymentRequest(PaymentRequest request);
}

