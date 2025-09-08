package com.example.controller;

import com.example.service.PaymentService;
import com.example.service.PaymentGatewayService;
import com.example.service.QRCodeService;
import com.example.dto.PaymentRequest;
import com.example.dto.PaymentStatusUpdateRequest;
import com.example.entity.Payment;
import com.example.entity.QRCode;
import com.example.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private PaymentGatewayService vnpayGatewayService;
    
    @Autowired
    private PaymentGatewayService momoGatewayService;
    
    @Autowired
    private QRCodeService qrCodeService;
    
    // ==================== PAYMENT MANAGEMENT ====================
    
    /**
     * Tạo payment mới
     */
    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@Valid @RequestBody PaymentRequest request) {
        var response = paymentService.createPayment(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Tạo payment cho invoice cụ thể
     */
    @PostMapping("/invoice/{invoiceId}/create")
    public ResponseEntity<?> createPaymentForInvoice(@PathVariable Long invoiceId,
                                                   @Valid @RequestBody PaymentRequest request) {
        // Đảm bảo invoiceId trong request khớp với path
        request.setInvoiceId(invoiceId);
        var response = paymentService.createPayment(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Tạo payment thành công ngay lập tức (cho demo/testing)
     */
    @PostMapping("/invoice/{invoiceId}/create-success")
    public ResponseEntity<?> createSuccessfulPayment(@PathVariable Long invoiceId,
                                                   @Valid @RequestBody PaymentRequest request) {
        // Đảm bảo invoiceId trong request khớp với path
        request.setInvoiceId(invoiceId);
        var response = paymentService.createSuccessfulPayment(request);
        
                // Tạo QR code sau khi thanh toán thành công
                try {
                    String qrCodeText = String.format("http://localhost:3000/invoiceDetail.html?id=%d&type=PICKUP", invoiceId);
                    QRCode qrCode = qrCodeService.createQRCode(qrCodeText, invoiceId, "PICKUP");
            
            // Thêm thông tin QR code vào response
            Map<String, Object> result = new HashMap<>();
            result.put("payment", response);
            result.put("qrCode", Map.of(
                "qrCodeId", qrCode.getId(),
                "qrCode", qrCode.getQrCode(),
                "invoiceId", qrCode.getInvoiceId(),
                "type", qrCode.getType(),
                "imageUrl", "/api/qr/image/" + qrCode.getQrCode()
            ));
            result.put("message", "Thanh toán thành công và QR code đã được tạo");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Nếu tạo QR code thất bại, vẫn trả về payment response
            Map<String, Object> result = new HashMap<>();
            result.put("payment", response);
            result.put("message", "Thanh toán thành công nhưng không thể tạo QR code: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }
    
    /**
     * Lấy payment theo ID
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getPaymentById(@PathVariable Long paymentId) {
        Payment payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(payment);
    }
    
    /**
     * Lấy payment theo gateway ID
     */
    @GetMapping("/gateway/{gatewayId}")
    public ResponseEntity<?> getPaymentByGatewayId(@PathVariable String gatewayId) {
        Payment payment = paymentService.getPaymentByGatewayId(gatewayId);
        return ResponseEntity.ok(payment);
    }
    
    /**
     * Lấy payment theo transaction ID
     */
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<?> getPaymentByTransactionId(@PathVariable String transactionId) {
        Payment payment = paymentService.getPaymentByTransactionId(transactionId);
        return ResponseEntity.ok(payment);
    }
    
    /**
     * Lấy tất cả payment của invoice
     */
    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<?> getPaymentsByInvoiceId(@PathVariable Long invoiceId) {
        List<Payment> payments = paymentService.getPaymentsByInvoiceId(invoiceId);
        return ResponseEntity.ok(payments);
    }
    
    /**
     * Lấy payment theo status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getPaymentsByStatus(@PathVariable String status) {
        List<Payment> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(payments);
    }
    
    /**
     * Lấy payment theo payment method
     */
    @GetMapping("/method/{paymentMethod}")
    public ResponseEntity<?> getPaymentsByMethod(@PathVariable String paymentMethod) {
        List<Payment> payments = paymentService.getPaymentsByMethod(paymentMethod);
        return ResponseEntity.ok(payments);
    }
    
    /**
     * Lấy payment theo user ID
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getPaymentsByUserId(@PathVariable Long userId) {
        List<Payment> payments = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }
    
    /**
     * Lấy payment theo khoảng thời gian
     */
    @GetMapping("/date-range")
    public ResponseEntity<?> getPaymentsByDateRange(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(startTime, formatter);
        LocalDateTime end = LocalDateTime.parse(endTime, formatter);
        
        List<Payment> payments = paymentService.getPaymentsByDateRange(start, end);
        return ResponseEntity.ok(payments);
    }
    
    // ==================== PAYMENT STATUS MANAGEMENT ====================
    
    /**
     * Cập nhật payment status
     */
    @PutMapping("/{paymentId}/status")
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable Long paymentId,
            @Valid @RequestBody PaymentStatusUpdateRequest request) {
        Payment payment = paymentService.updatePaymentStatus(paymentId, request);
        return ResponseEntity.ok(payment);
    }
    
    /**
     * Hủy payment
     */
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<?> cancelPayment(
            @PathVariable Long paymentId,
            @RequestParam String reason) {
        Payment payment = paymentService.cancelPayment(paymentId, reason);
        return ResponseEntity.ok(payment);
    }
    
    /**
     * Refund payment
     */
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<?> refundPayment(
            @PathVariable Long paymentId,
            @RequestParam BigDecimal amount,
            @RequestParam String reason) {
        Payment payment = paymentService.refundPayment(paymentId, amount, reason);
        return ResponseEntity.ok(payment);
    }
    
    // ==================== WEBHOOK HANDLERS ====================
    
    /**
     * VNPay webhook
     */
    @PostMapping("/webhook/vnpay")
    public ResponseEntity<?> handleVNPayWebhook(@RequestBody String payload,
                                              @RequestParam String vnp_SecureHash) {
        // Verify signature
        if (!vnpayGatewayService.verifyWebhookSignature(payload, vnp_SecureHash)) {
            throw new IllegalArgumentException("VNPay webhook signature không hợp lệ");
        }
        
        // Process webhook data
        Map<String, Object> webhookData = vnpayGatewayService.processWebhookData(payload);
        
        // Process payment status
        String gatewayId = (String) webhookData.get("gatewayId");
        String status = (String) webhookData.get("status");
        
        if ("SUCCESS".equals(status)) {
            var payment = paymentService.processPaymentSuccess(gatewayId, (String) webhookData.get("transactionId"));
            return ResponseEntity.ok(Map.of(
                "message", "VNPay webhook xử lý thành công",
                "payment", payment
            ));
        } else {
            var payment = paymentService.processPaymentFailure(gatewayId, (String) webhookData.get("message"));
            return ResponseEntity.ok(Map.of(
                "message", "VNPay webhook xử lý thành công",
                "payment", payment
            ));
        }
    }
    
    /**
     * MOMO webhook
     */
    @PostMapping("/webhook/momo")
    public ResponseEntity<?> handleMOMOWebhook(@RequestBody String payload,
                                             @RequestParam String signature) {
        // Verify signature
        if (!momoGatewayService.verifyWebhookSignature(payload, signature)) {
            throw new IllegalArgumentException("MOMO webhook signature không hợp lệ");
        }
        
        // Process webhook data
        Map<String, Object> webhookData = momoGatewayService.processWebhookData(payload);
        
        // Process payment status
        String gatewayId = (String) webhookData.get("gatewayId");
        String status = (String) webhookData.get("status");
        
        if ("SUCCESS".equals(status)) {
            var payment = paymentService.processPaymentSuccess(gatewayId, (String) webhookData.get("transactionId"));
            return ResponseEntity.ok(Map.of(
                "message", "MOMO webhook xử lý thành công",
                "payment", payment
            ));
        } else {
            var payment = paymentService.processPaymentFailure(gatewayId, (String) webhookData.get("message"));
            return ResponseEntity.ok(Map.of(
                "message", "MOMO webhook xử lý thành công",
                "payment", payment
            ));
        }
    }
    
    // ==================== ADMIN ENDPOINTS ====================
    
    /**
     * Thống kê payment
     */
    @GetMapping("/admin/statistics")
    public ResponseEntity<?> getPaymentStatistics(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(startTime, formatter);
        LocalDateTime end = LocalDateTime.parse(endTime, formatter);
        
        Map<String, Object> stats = paymentService.getPaymentStatistics(start, end);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Payment summary cho admin
     */
    @GetMapping("/admin/summary")
    public ResponseEntity<?> getAdminPaymentSummary() {
        Map<String, Object> summary = paymentService.getAdminPaymentSummary();
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Xử lý payment expired
     */
    @PostMapping("/admin/process-expired")
    public ResponseEntity<?> processExpiredPayments() {
        paymentService.processExpiredPayments();
        return ResponseEntity.ok(Map.of(
            "message", "Đã xử lý payment expired"
        ));
    }
    
    // ==================== UTILITY ENDPOINTS ====================
    
    /**
     * Kiểm tra payment có thể refund không
     */
    @GetMapping("/{paymentId}/can-refund")
    public ResponseEntity<?> canRefund(@PathVariable Long paymentId) {
        Payment payment = paymentService.getPaymentById(paymentId);
        boolean canRefund = paymentService.canRefund(payment);
        
        return ResponseEntity.ok(Map.of(
            "paymentId", paymentId,
            "canRefund", canRefund,
            "reason", canRefund ? "Có thể refund" : "Không thể refund"
        ));
    }
    
    /**
     * Tính phí giao dịch
     */
    @GetMapping("/calculate-fee")
    public ResponseEntity<?> calculateTransactionFee(
            @RequestParam BigDecimal amount,
            @RequestParam String paymentMethod) {
        BigDecimal fee = paymentService.calculateTransactionFee(amount, paymentMethod);
        
        return ResponseEntity.ok(Map.of(
            "amount", amount,
            "paymentMethod", paymentMethod,
            "transactionFee", fee,
            "totalAmount", amount.add(fee)
        ));
    }
    
    /**
     * Lấy thông tin gateway
     */
    @GetMapping("/gateway-info")
    public ResponseEntity<?> getGatewayInfo() {
        Map<String, Object> info = vnpayGatewayService.getGatewayInfo();
        return ResponseEntity.ok(info);
    }
    
    /**
     * Lấy QR code lấy xe sau khi thanh toán thành công
     */
    @GetMapping("/invoice/{invoiceId}/pickup-qr")
    public ResponseEntity<?> getPickupQRCode(@PathVariable Long invoiceId) {
        // Kiểm tra payment đã thành công chưa
        List<Payment> payments = paymentService.getPaymentsByInvoiceId(invoiceId);
        boolean hasSuccessfulPayment = payments.stream()
                .anyMatch(p -> "SUCCESS".equals(p.getStatus()));
        
        if (!hasSuccessfulPayment) {
            throw new PaymentRequiredException("Chưa thanh toán thành công");
        }
        
        // Tìm QR code lấy xe
        String qrCodeText = String.format("INVOICE:%d:PICKUP", invoiceId);
        var qrCodeOpt = qrCodeService.findQRCodeByQrCode(qrCodeText);
        
        if (qrCodeOpt.isEmpty()) {
            // Tạo QR code mới nếu chưa có
            QRCode qrCode = qrCodeService.createQRCode(qrCodeText, invoiceId, "PICKUP");
            return ResponseEntity.ok(Map.of(
                "qrCodeId", qrCode.getId(),
                "qrCode", qrCode.getQrCode(),
                "invoiceId", qrCode.getInvoiceId(),
                "type", qrCode.getType(),
                "imageUrl", "/api/qr/image/" + qrCode.getQrCode(),
                "message", "QR code lấy xe đã được tạo"
            ));
        } else {
            QRCode qrCode = qrCodeOpt.get();
            return ResponseEntity.ok(Map.of(
                "qrCodeId", qrCode.getId(),
                "qrCode", qrCode.getQrCode(),
                "invoiceId", qrCode.getInvoiceId(),
                "type", qrCode.getType(),
                "imageUrl", "/api/qr/image/" + qrCode.getQrCode(),
                "message", "QR code lấy xe đã tồn tại"
            ));
        }
    }
}
