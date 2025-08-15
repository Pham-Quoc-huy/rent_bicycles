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

@Service("vnpayGatewayService")
public class VNPayGatewayServiceImpl implements PaymentGatewayService {
    
    @Value("${vnpay.tmn-code}")
    private String tmnCode;
    
    @Value("${vnpay.hash-secret}")
    private String hashSecret;
    
    @Value("${vnpay.payment-url}")
    private String paymentUrl;
    
    @Value("${vnpay.return-url}")
    private String returnUrl;
    
    @Value("${vnpay.callback-url}")
    private String callbackUrl;
    
    @Override
    public Map<String, Object> createPaymentSession(PaymentRequest request) {
        try {
            String vnp_Version = "2.1.0";
            String vnp_Command = "pay";
            String vnp_TxnRef = generateTxnRef();
            String vnp_IpAddr = "127.0.0.1";
            String vnp_TmnCode = tmnCode;
            
            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(request.getAmount().multiply(BigDecimal.valueOf(100)).longValue()));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_BankCode", "");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", request.getDescription() != null ? request.getDescription() : "Thanh toan thue xe");
            vnp_Params.put("vnp_OrderType", "other");
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", returnUrl);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
            vnp_Params.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            
            // Tạo chuỗi hash
            String vnp_SecureHash = createSecureHash(vnp_Params);
            vnp_Params.put("vnp_SecureHash", vnp_SecureHash);
            
            // Tạo payment URL
            String paymentUrlWithParams = buildPaymentUrl(vnp_Params);
            
            // Tạo QR code URL cho VNPay (sử dụng QR code có sẵn từ VNPay)
            String qrCodeUrl = generateVNPayQRCodeUrl(vnp_Params);
            
            Map<String, Object> result = new HashMap<>();
            result.put("gatewayId", vnp_TxnRef);
            result.put("paymentUrl", paymentUrlWithParams);
            result.put("qrCode", qrCodeUrl);
            result.put("gatewayName", "VNPAY");
            result.put("expiresAt", LocalDateTime.now().plusMinutes(15));
            
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo VNPay session: " + e.getMessage());
        }
    }
    
    @Override
    public boolean verifyWebhookSignature(String payload, String signature) {
        try {
            // Parse payload và tạo lại signature để so sánh
            Map<String, String> params = parseQueryString(payload);
            
            // Loại bỏ vnp_SecureHash và vnp_SecureHashType
            params.remove("vnp_SecureHash");
            params.remove("vnp_SecureHashType");
            
            String calculatedSignature = createSecureHash(params);
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
            result.put("gatewayId", params.get("vnp_TxnRef"));
            result.put("transactionId", params.get("vnp_TransactionNo"));
            result.put("amount", new BigDecimal(params.get("vnp_Amount")).divide(BigDecimal.valueOf(100)));
            result.put("status", "00".equals(params.get("vnp_ResponseCode")) ? "SUCCESS" : "FAILED");
            result.put("message", params.get("vnp_Message"));
            result.put("bankCode", params.get("vnp_BankCode"));
            result.put("payDate", params.get("vnp_PayDate"));
            
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xử lý VNPay webhook: " + e.getMessage());
        }
    }
    
    @Override
    public String checkPaymentStatus(String gatewayId) {
        // VNPay không có API check status, chỉ dựa vào webhook
        return "UNKNOWN";
    }
    
    @Override
    public boolean cancelPayment(String gatewayId) {
        // VNPay không hỗ trợ cancel payment
        return false;
    }
    
    @Override
    public boolean refundPayment(String gatewayId, BigDecimal amount, String reason) {
        // Cần implement VNPay refund API
        return false;
    }
    
    @Override
    public String getGatewayName() {
        return "VNPAY";
    }
    
    @Override
    public boolean supportsPaymentMethod(String paymentMethod) {
        return "VNPAY".equalsIgnoreCase(paymentMethod);
    }
    
    @Override
    public BigDecimal calculateTransactionFee(BigDecimal amount) {
        // VNPay: 1.1% + 1,100 VND
        return amount.multiply(BigDecimal.valueOf(0.011)).add(BigDecimal.valueOf(1100));
    }
    
    @Override
    public Map<String, Object> getGatewayInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "VNPAY");
        info.put("supportedMethods", new String[]{"VNPAY"});
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
    private String generateTxnRef() {
        return "VNPAY" + System.currentTimeMillis() + new Random().nextInt(1000);
    }
    
    private String generateVNPayQRCodeUrl(Map<String, String> params) {
        try {
            // VNPay cung cấp QR code API
            // URL: https://sandbox.vnpayment.vn/paymentv2/qrcode.html
            StringBuilder qrUrl = new StringBuilder();
            qrUrl.append("https://sandbox.vnpayment.vn/paymentv2/qrcode.html?");
            
            // Thêm các tham số cần thiết cho QR code
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (!entry.getKey().equals("vnp_SecureHash")) {
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
            return buildPaymentUrl(params);
        }
    }
    
    private String createSecureHash(Map<String, String> params) {
        try {
            // Sắp xếp params theo key
            String[] sortedKeys = params.keySet().toArray(new String[0]);
            java.util.Arrays.sort(sortedKeys);
            
            StringBuilder hashData = new StringBuilder();
            for (String key : sortedKeys) {
                if (params.get(key) != null && !params.get(key).isEmpty()) {
                    hashData.append(key).append("=").append(params.get(key)).append("&");
                }
            }
            
            // Loại bỏ dấu & cuối cùng
            if (hashData.length() > 0) {
                hashData.setLength(hashData.length() - 1);
            }
            
            // Tạo HMAC-SHA512
            String data = hashData.toString();
            String secret = hashSecret;
            
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA512");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKeySpec);
            
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
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
            throw new RuntimeException("Lỗi tạo secure hash: " + e.getMessage());
        }
    }
    
    private String buildPaymentUrl(Map<String, String> params) {
        StringBuilder url = new StringBuilder(paymentUrl);
        url.append("?");
        
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                url.append("&");
            }
            url.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            first = false;
        }
        
        return url.toString();
    }
    
    private Map<String, String> parseQueryString(String queryString) {
        Map<String, String> params = new HashMap<>();
        String[] pairs = queryString.split("&");
        
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                params.put(keyValue[0], keyValue[1]);
            }
        }
        
        return params;
    }
}
