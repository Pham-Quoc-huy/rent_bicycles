package com.example.service.impl;

import com.example.service.PaymentGatewayService;
import com.example.dto.PaymentRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service("momoGatewayService")
public class MOMOGatewayServiceImpl implements PaymentGatewayService {
    
    @Value("${momo.partner-code:test}")
    private String partnerCode;
    
    @Value("${momo.access-key:test}")
    private String accessKey;
    
    @Value("${momo.secret-key:test}")
    private String secretKey;
    
    @Value("${momo.payment-url:https://test-payment.momo.vn/v2/gateway/api/create}")
    private String paymentUrl;
    
    @Value("${momo.return-url:http://localhost:3000/payment/return}")
    private String returnUrl;
    
    @Value("${momo.callback-url:http://localhost:8080/api/payments/webhook/momo}")
    private String callbackUrl;
    
    @Override
    public Map<String, Object> createPaymentSession(PaymentRequest request) {
        try {
            // Tạo request ID
            String requestId = generateRequestId();
            
            // Tạo order ID
            String orderId = generateOrderId();
            
            // Tạo MOMO params
            Map<String, String> momoParams = new HashMap<>();
            momoParams.put("partnerCode", partnerCode);
            momoParams.put("orderId", orderId);
            momoParams.put("requestId", requestId);
            momoParams.put("amount", request.getAmount().multiply(BigDecimal.valueOf(100)).intValue() + "");
            momoParams.put("orderInfo", request.getDescription() != null ? request.getDescription() : "Thanh toán thuê xe");
            momoParams.put("returnUrl", returnUrl);
            momoParams.put("ipnUrl", callbackUrl);
            momoParams.put("lang", "vi");
            momoParams.put("extraData", "");
            
            // Tạo signature
            String signature = createMOMOSignature(momoParams);
            momoParams.put("signature", signature);
            
            // Tạo payment URL
            String paymentUrlWithParams = buildMOMOPaymentUrl(momoParams);
            
            // Tạo QR code URL cho MOMO
            String qrCodeUrl = generateMOMOQRCodeUrl(momoParams);
            
            Map<String, Object> result = new HashMap<>();
            result.put("gatewayId", orderId);
            result.put("paymentUrl", paymentUrlWithParams);
            result.put("qrCode", qrCodeUrl);
            result.put("gatewayName", "MOMO");
            result.put("expiresAt", LocalDateTime.now().plusMinutes(15));
            
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo MOMO payment session: " + e.getMessage());
        }
    }
    
    @Override
    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            // Parse payload và tạo lại signature để so sánh
            Map<String, String> params = parseQueryString(payload);
            
            // Loại bỏ signature
            params.remove("signature");
            
            String calculatedSignature = createMOMOSignature(params);
            return calculatedSignature.equals(signature);
            
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public Map<String, Object> processWebhookData(String payload) {
        try {
            Map<String, String> params = parseQueryString(payload);
            
            Map<String, Object> result = new HashMap<>();
            result.put("gatewayId", params.get("orderId"));
            result.put("transactionId", params.get("transId"));
            result.put("amount", new BigDecimal(params.get("amount")).divide(BigDecimal.valueOf(100)));
            result.put("status", "0".equals(params.get("resultCode")) ? "SUCCESS" : "FAILED");
            result.put("message", params.get("message"));
            result.put("payType", params.get("payType"));
            
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xử lý MOMO webhook: " + e.getMessage());
        }
    }
    
    @Override
    public String checkPaymentStatus(String gatewayId) {
        // MOMO có API check status
        return "UNKNOWN";
    }
    
    @Override
    public boolean cancelPayment(String gatewayId) {
        // MOMO không hỗ trợ cancel payment
        return false;
    }
    
    @Override
    public boolean refundPayment(String gatewayId, BigDecimal amount, String reason) {
        // Cần implement MOMO refund API
        return false;
    }
    
    @Override
    public String getGatewayName() {
        return "MOMO";
    }
    
    @Override
    public boolean supportsPaymentMethod(String paymentMethod) {
        return "MOMO".equalsIgnoreCase(paymentMethod);
    }
    
    @Override
    public BigDecimal calculateTransactionFee(BigDecimal amount) {
        // MOMO: 1.1% + 1,100 VND
        return amount.multiply(BigDecimal.valueOf(0.011)).add(BigDecimal.valueOf(1100));
    }
    
    @Override
    public Map<String, Object> getGatewayInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "MOMO");
        info.put("supportedMethods", new String[]{"MOMO"});
        info.put("feeStructure", "1.1% + 1,100 VND");
        info.put("processingTime", "Real-time");
        return info;
    }
    
    @Override
    public boolean validatePaymentRequest(PaymentRequest request) {
        return request != null && 
               request.getAmount() != null && 
               request.getAmount().compareTo(BigDecimal.valueOf(1000)) >= 0;
    }
    
    // Helper methods
    private String generateRequestId() {
        return "MOMO" + System.currentTimeMillis() + new Random().nextInt(1000);
    }
    
    private String generateOrderId() {
        return "ORDER" + System.currentTimeMillis() + new Random().nextInt(1000);
    }
    
    private String createMOMOSignature(Map<String, String> params) {
        try {
            // Tạo chuỗi để ký
            StringBuilder data = new StringBuilder();
            data.append("accessKey=").append(accessKey);
            data.append("&amount=").append(params.get("amount"));
            data.append("&extraData=").append(params.get("extraData"));
            data.append("&ipnUrl=").append(params.get("ipnUrl"));
            data.append("&orderId=").append(params.get("orderId"));
            data.append("&orderInfo=").append(params.get("orderInfo"));
            data.append("&partnerCode=").append(params.get("partnerCode"));
            data.append("&redirectUrl=").append(params.get("returnUrl"));
            data.append("&requestId=").append(params.get("requestId"));
            data.append("&requestType=").append("captureWallet");
            
            // Tạo HMAC-SHA256
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            
            byte[] hash = mac.doFinal(data.toString().getBytes(StandardCharsets.UTF_8));
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
            throw new RuntimeException("Lỗi tạo MOMO signature", e);
        }
    }
    
    private String buildMOMOPaymentUrl(Map<String, String> params) {
        StringBuilder url = new StringBuilder();
        url.append(paymentUrl).append("?");
        
        for (Map.Entry<String, String> entry : params.entrySet()) {
            url.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        
        String result = url.toString();
        if (result.endsWith("&")) {
            result = result.substring(0, result.length() - 1);
        }
        
        return result;
    }
    
    private String generateMOMOQRCodeUrl(Map<String, String> params) {
        try {
            // MOMO cung cấp QR code API
            // URL: https://test-payment.momo.vn/v2/gateway/api/qrcode
            StringBuilder qrUrl = new StringBuilder();
            qrUrl.append("https://test-payment.momo.vn/v2/gateway/api/qrcode?");
            
            // Thêm các tham số cần thiết cho QR code
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (!entry.getKey().equals("signature")) {
                    qrUrl.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }
            }
            
            // Loại bỏ dấu & cuối cùng
            String result = qrUrl.toString();
            if (result.endsWith("&")) {
                result = result.substring(0, result.length() - 1);
            }
            
            return result;
            
        } catch (Exception e) {
            // Fallback: trả về payment URL
            return buildMOMOPaymentUrl(params);
        }
    }
    
    private Map<String, String> parseQueryString(String queryString) {
        Map<String, String> params = new HashMap<>();
        if (queryString != null && !queryString.isEmpty()) {
            String[] pairs = queryString.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return params;
    }
}
