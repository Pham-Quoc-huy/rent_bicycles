package com.example.controller;

import com.example.service.PaymentService;
import com.example.service.PaymentGatewayService;
import com.example.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {

    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private PaymentGatewayService vnpayGatewayService;

    /**
     * Test VNPay webhook signature verification
     */
    @PostMapping("/webhook-signature")
    public ResponseEntity<?> testWebhookSignature(@RequestBody String payload,
                                                @RequestParam(value = "vnp_SecureHash", required = false) String signature) {
        try {
            // Test signature verification với VNPay
            boolean isValid = vnpayGatewayService.verifyWebhookSignature(payload, signature);
            
            return ResponseEntity.ok(Map.of(
                "valid", isValid,
                "payload", payload,
                "signature", signature,
                "message", isValid ? "VNPay signature hợp lệ!" : "VNPay signature không hợp lệ!"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi xác thực VNPay signature: " + e.getMessage()
            ));
        }
    }

    /**
     * Demo tạo VNPay signature test
     */
    @PostMapping("/create-test-signature")
    public ResponseEntity<?> createTestSignature(@RequestBody Map<String, Object> request) {
        try {
            String payload = (String) request.get("payload");
            String amount = (String) request.get("amount");
            String txnRef = (String) request.get("txnRef");
            
            // Tạo test VNPay signature (chỉ để demo, không dùng trong production)
            String testSignature = createVNPayTestSignature(payload, amount, txnRef);
            
            return ResponseEntity.ok(Map.of(
                "payload", payload,
                "amount", amount,
                "txnRef", txnRef,
                "signature", testSignature,
                "message", "VNPay test signature đã được tạo"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi tạo VNPay test signature: " + e.getMessage()
            ));
        }
    }

    /**
     * Test đầy đủ VNPay webhook flow
     */
    @PostMapping("/test-webhook-flow")
    public ResponseEntity<?> testWebhookFlow(@RequestBody Map<String, Object> request) {
        try {
            String payload = (String) request.get("payload");
            String signature = (String) request.get("signature");
            
            // Bước 1: Verify VNPay signature
            boolean isValidSignature = vnpayGatewayService.verifyWebhookSignature(payload, signature);
            
            if (!isValidSignature) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "VNPay webhook signature không hợp lệ",
                    "valid", false
                ));
            }
            
            // Bước 2: Process webhook data
            var webhookData = vnpayGatewayService.processWebhookData(payload);
            
            // Bước 3: Process payment status
            String gatewayId = (String) webhookData.get("gatewayId");
            String status = (String) webhookData.get("status");
            
            if ("SUCCESS".equals(status)) {
                // Xử lý payment success
                var payment = paymentService.processPaymentSuccess(gatewayId, (String) webhookData.get("transactionId"));
                return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "status", status,
                    "payment", payment,
                    "message", "VNPay webhook xử lý thành công!"
                ));
            } else {
                // Xử lý payment failure
                var payment = paymentService.processPaymentFailure(gatewayId, (String) webhookData.get("message"));
                return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "status", status,
                    "payment", payment,
                    "message", "VNPay webhook xử lý thành công!"
                ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi xử lý VNPay webhook: " + e.getMessage(),
                "valid", false
            ));
        }
    }

    /**
     * Tạo VNPay test signature (chỉ để demo)
     */
    private String createVNPayTestSignature(String payload, String amount, String txnRef) {
        try {
            String testSecret = "test_hash_secret_12345";
            
            // Tạo params giống VNPay
            java.util.Map<String, String> params = new java.util.HashMap<>();
            params.put("vnp_Amount", amount);
            params.put("vnp_CurrCode", "VND");
            params.put("vnp_TxnRef", txnRef);
            params.put("vnp_OrderInfo", "Test payment");
            params.put("vnp_OrderType", "other");
            params.put("vnp_Locale", "vn");
            params.put("vnp_ReturnUrl", "http://localhost:3000/return");
            params.put("vnp_IpAddr", "127.0.0.1");
            params.put("vnp_CreateDate", "20250113230000");
            
            // Sắp xếp params theo key
            String[] sortedKeys = params.keySet().toArray(new String[0]);
            java.util.Arrays.sort(sortedKeys);
            
            StringBuilder hashData = new StringBuilder();
            for (String key : sortedKeys) {
                hashData.append(key).append("=").append(params.get(key)).append("&");
            }
            
            // Loại bỏ dấu & cuối cùng
            if (hashData.length() > 0) {
                hashData.setLength(hashData.length() - 1);
            }
            
            // Tạo HMAC-SHA512
            String data = hashData.toString();
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA512");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                testSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKeySpec);
            
            byte[] hash = mac.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (Exception e) {
            throw new PaymentFailedException("Lỗi tạo VNPay test signature: " + e.getMessage());
        }
    }

    /**
     * Test tạo payment mới
     */
    @PostMapping("/create-payment")
    public ResponseEntity<?> createTestPayment(@RequestBody Map<String, Object> request) {
        try {
            // Tạo test payment request
            var paymentRequest = new com.example.dto.PaymentRequest();
            paymentRequest.setInvoiceId(Long.valueOf(request.get("invoiceId").toString()));
            paymentRequest.setAmount(new java.math.BigDecimal(request.get("amount").toString()));
            paymentRequest.setPaymentMethod((String) request.get("paymentMethod"));
            paymentRequest.setCustomerEmail((String) request.get("customerEmail"));
            paymentRequest.setDescription("Test payment");
            
            // Tạo payment
            var response = paymentService.createPayment(paymentRequest);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "payment", response,
                "message", "Test payment đã được tạo thành công!"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi tạo test payment: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Test VNPay gateway
     */
    @PostMapping("/test-vnpay-gateway")
    public ResponseEntity<?> testVNPayGateway(@RequestBody Map<String, Object> request) {
        try {
            // Tạo test payment request
            var paymentRequest = new com.example.dto.PaymentRequest();
            paymentRequest.setInvoiceId(Long.valueOf(request.get("invoiceId").toString()));
            paymentRequest.setAmount(new java.math.BigDecimal(request.get("amount").toString()));
            paymentRequest.setPaymentMethod("VNPAY");
            paymentRequest.setCustomerEmail((String) request.get("customerEmail"));
            paymentRequest.setDescription("Test VNPay payment");
            
            // Tạo VNPay session
            var session = vnpayGatewayService.createPaymentSession(paymentRequest);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "gatewayInfo", vnpayGatewayService.getGatewayInfo(),
                "session", session,
                "message", "VNPay gateway test thành công!"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi test VNPay gateway: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/test-payment-success")
    public ResponseEntity<?> testPaymentSuccess(@RequestBody Map<String, Object> request) {
        try {
            Long paymentId = Long.valueOf(request.get("paymentId").toString());
            
            // Simulate successful payment
            var payment = paymentService.getPaymentById(paymentId);
            payment.setStatus("SUCCESS");
            payment.setGatewayId("TEST_SUCCESS_" + System.currentTimeMillis());
            payment.setTransactionId("TEST_TXN_" + System.currentTimeMillis());
            payment.setUpdatedAt(LocalDateTime.now());
            
            var savedPayment = paymentService.updatePayment(payment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment marked as successful for testing");
            response.put("payment", savedPayment);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/test-payment-failed")
    public ResponseEntity<?> testPaymentFailed(@RequestBody Map<String, Object> request) {
        try {
            Long paymentId = Long.valueOf(request.get("paymentId").toString());
            
            // Simulate failed payment
            var payment = paymentService.getPaymentById(paymentId);
            payment.setStatus("FAILED");
            payment.setGatewayId("TEST_FAILED_" + System.currentTimeMillis());
            payment.setUpdatedAt(LocalDateTime.now());
            
            var savedPayment = paymentService.updatePayment(payment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment marked as failed for testing");
            response.put("payment", savedPayment);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

