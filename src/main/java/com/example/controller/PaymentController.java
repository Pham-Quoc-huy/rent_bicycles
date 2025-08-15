package com.example.controller;

import com.example.service.PaymentService;
import com.example.service.PaymentGatewayService;
import com.example.dto.PaymentRequest;
import com.example.dto.PaymentStatusUpdateRequest;
import com.example.entity.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    
    // ==================== PAYMENT MANAGEMENT ====================
    
    /**
     * Tạo payment mới
     */
    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody PaymentRequest request) {
        try {
            var response = paymentService.createPayment(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi tạo payment: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Tạo payment cho invoice cụ thể
     */
    @PostMapping("/invoice/{invoiceId}/create")
    public ResponseEntity<?> createPaymentForInvoice(@PathVariable Long invoiceId,
                                                   @RequestBody PaymentRequest request) {
        try {
            // Đảm bảo invoiceId trong request khớp với path
            request.setInvoiceId(invoiceId);
            var response = paymentService.createPayment(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi tạo payment: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Lấy payment theo ID
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getPaymentById(@PathVariable Long paymentId) {
        try {
            Payment payment = paymentService.getPaymentById(paymentId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi lấy payment: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Lấy payment theo gateway ID
     */
    @GetMapping("/gateway/{gatewayId}")
    public ResponseEntity<?> getPaymentByGatewayId(@PathVariable String gatewayId) {
        try {
            Payment payment = paymentService.getPaymentByGatewayId(gatewayId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi lấy payment: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Lấy payment theo transaction ID
     */
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<?> getPaymentByTransactionId(@PathVariable String transactionId) {
        try {
            Payment payment = paymentService.getPaymentByTransactionId(transactionId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi lấy payment: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Lấy tất cả payment của invoice
     */
    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<?> getPaymentsByInvoiceId(@PathVariable Long invoiceId) {
        try {
            List<Payment> payments = paymentService.getPaymentsByInvoiceId(invoiceId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi lấy payments: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Lấy payment theo status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getPaymentsByStatus(@PathVariable String status) {
        try {
            List<Payment> payments = paymentService.getPaymentsByStatus(status);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi lấy payments: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Lấy payment theo payment method
     */
    @GetMapping("/method/{paymentMethod}")
    public ResponseEntity<?> getPaymentsByMethod(@PathVariable String paymentMethod) {
        try {
            List<Payment> payments = paymentService.getPaymentsByMethod(paymentMethod);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi lấy payments: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Lấy payment theo user ID
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getPaymentsByUserId(@PathVariable Long userId) {
        try {
            List<Payment> payments = paymentService.getPaymentsByUserId(userId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi lấy payments: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Lấy payment theo khoảng thời gian
     */
    @GetMapping("/date-range")
    public ResponseEntity<?> getPaymentsByDateRange(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime start = LocalDateTime.parse(startTime, formatter);
            LocalDateTime end = LocalDateTime.parse(endTime, formatter);
            
            List<Payment> payments = paymentService.getPaymentsByDateRange(start, end);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi lấy payments: " + e.getMessage()
            ));
        }
    }
    
    // ==================== PAYMENT STATUS MANAGEMENT ====================
    
    /**
     * Cập nhật payment status
     */
    @PutMapping("/{paymentId}/status")
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable Long paymentId,
            @RequestBody PaymentStatusUpdateRequest request) {
        try {
            Payment payment = paymentService.updatePaymentStatus(paymentId, request);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi cập nhật payment status: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Hủy payment
     */
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<?> cancelPayment(
            @PathVariable Long paymentId,
            @RequestParam String reason) {
        try {
            Payment payment = paymentService.cancelPayment(paymentId, reason);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi hủy payment: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Refund payment
     */
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<?> refundPayment(
            @PathVariable Long paymentId,
            @RequestParam BigDecimal amount,
            @RequestParam String reason) {
        try {
            Payment payment = paymentService.refundPayment(paymentId, amount, reason);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi refund payment: " + e.getMessage()
            ));
        }
    }
    
    // ==================== WEBHOOK HANDLERS ====================
    
    /**
     * VNPay webhook
     */
    @PostMapping("/webhook/vnpay")
    public ResponseEntity<?> handleVNPayWebhook(@RequestBody String payload,
                                              @RequestParam String vnp_SecureHash) {
        try {
            // Verify signature
            if (!vnpayGatewayService.verifyWebhookSignature(payload, vnp_SecureHash)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "VNPay webhook signature không hợp lệ"
                ));
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
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi xử lý VNPay webhook: " + e.getMessage()
            ));
        }
    }
    
    /**
     * MOMO webhook
     */
    @PostMapping("/webhook/momo")
    public ResponseEntity<?> handleMOMOWebhook(@RequestBody String payload,
                                             @RequestParam String signature) {
        try {
            // Verify signature
            if (!momoGatewayService.verifyWebhookSignature(payload, signature)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "MOMO webhook signature không hợp lệ"
                ));
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
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi xử lý MOMO webhook: " + e.getMessage()
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
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime start = LocalDateTime.parse(startTime, formatter);
            LocalDateTime end = LocalDateTime.parse(endTime, formatter);
            
            Map<String, Object> stats = paymentService.getPaymentStatistics(start, end);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi lấy thống kê: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Payment summary cho admin
     */
    @GetMapping("/admin/summary")
    public ResponseEntity<?> getAdminPaymentSummary() {
        try {
            Map<String, Object> summary = paymentService.getAdminPaymentSummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi lấy summary: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Xử lý payment expired
     */
    @PostMapping("/admin/process-expired")
    public ResponseEntity<?> processExpiredPayments() {
        try {
            paymentService.processExpiredPayments();
            return ResponseEntity.ok(Map.of(
                "message", "Đã xử lý payment expired"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi xử lý payment expired: " + e.getMessage()
            ));
        }
    }
    
    // ==================== UTILITY ENDPOINTS ====================
    
    /**
     * Kiểm tra payment có thể refund không
     */
    @GetMapping("/{paymentId}/can-refund")
    public ResponseEntity<?> canRefund(@PathVariable Long paymentId) {
        try {
            Payment payment = paymentService.getPaymentById(paymentId);
            boolean canRefund = paymentService.canRefund(payment);
            
            return ResponseEntity.ok(Map.of(
                "paymentId", paymentId,
                "canRefund", canRefund,
                "reason", canRefund ? "Có thể refund" : "Không thể refund"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi kiểm tra refund: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Tính phí giao dịch
     */
    @GetMapping("/calculate-fee")
    public ResponseEntity<?> calculateTransactionFee(
            @RequestParam BigDecimal amount,
            @RequestParam String paymentMethod) {
        try {
            BigDecimal fee = paymentService.calculateTransactionFee(amount, paymentMethod);
            
            return ResponseEntity.ok(Map.of(
                "amount", amount,
                "paymentMethod", paymentMethod,
                "transactionFee", fee,
                "totalAmount", amount.add(fee)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi tính phí: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Lấy thông tin gateway
     */
    @GetMapping("/gateway-info")
    public ResponseEntity<?> getGatewayInfo() {
        try {
            Map<String, Object> info = vnpayGatewayService.getGatewayInfo();
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi lấy gateway info: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Lấy QR code lấy xe sau khi thanh toán thành công
     */
    @GetMapping("/invoice/{invoiceId}/pickup-qr")
    public ResponseEntity<?> getPickupQRCode(@PathVariable Long invoiceId) {
        try {
            // Kiểm tra payment đã thành công chưa
            List<Payment> payments = paymentService.getPaymentsByInvoiceId(invoiceId);
            boolean hasSuccessfulPayment = payments.stream()
                    .anyMatch(p -> "SUCCESS".equals(p.getStatus()));
            
            if (!hasSuccessfulPayment) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Chưa thanh toán thành công"
                ));
            }
            
            // Tìm QR code lấy xe
            // TODO: Implement logic tìm QR code lấy xe
            return ResponseEntity.ok(Map.of(
                "message", "QR code lấy xe đã được tạo",
                "invoiceId", invoiceId
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi lấy QR code lấy xe: " + e.getMessage()
            ));
        }
    }
}
