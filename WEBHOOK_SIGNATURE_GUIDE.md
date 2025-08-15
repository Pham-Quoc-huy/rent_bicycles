# Hướng dẫn Webhook Signature Verification

## Tổng quan

Webhook signature verification là một tính năng bảo mật quan trọng để đảm bảo rằng các webhook events thực sự đến từ Stripe, không phải từ kẻ tấn công.

## Cách hoạt động

### 1. Stripe gửi webhook với signature

```
POST /api/payments/stripe/webhook
Headers:
  Stripe-Signature: t=1234567890,v1=abc123def456...

Body:
{
  "id": "evt_...",
  "type": "checkout.session.completed",
  "data": {...}
}
```

### 2. Format của Stripe Signature

```
t=1234567890,v1=abc123def456...
```

- `t=1234567890` - Timestamp khi Stripe tạo signature
- `v1=abc123def456...` - HMAC-SHA256 signature

### 3. Quá trình xác thực

#### Bước 1: Parse signature

```java
String[] parts = signature.split(",");
String timestamp = parts[0].substring(2);  // "1234567890"
String receivedSignature = parts[1].substring(3);  // "abc123def456..."
```

#### Bước 2: Tạo signed payload

```java
String signedPayload = timestamp + "." + payload;
// Ví dụ: "1234567890.{\"id\":\"evt_...\",\"type\":\"checkout.session.completed\"}"
```

#### Bước 3: Tính toán expected signature

```java
String expectedSignature = calculateHmacSha256(signedPayload, webhookSecret);
```

#### Bước 4: So sánh signature

```java
return receivedSignature.equals(expectedSignature);
```

## Implementation chi tiết

### Phương thức verifyWebhookSignature()

```java
@Override
public boolean verifyWebhookSignature(String payload, String signature) {
    try {
        // 1. Kiểm tra format signature
        if (signature == null || !signature.startsWith("t=")) {
            return false;
        }

        // 2. Parse signature
        String[] parts = signature.split(",");
        if (parts.length != 2) {
            return false;
        }

        String timestampPart = parts[0];
        String signaturePart = parts[1];

        if (!timestampPart.startsWith("t=") || !signaturePart.startsWith("v1=")) {
            return false;
        }

        String timestamp = timestampPart.substring(2);
        String receivedSignature = signaturePart.substring(3);

        // 3. Tạo signed payload
        String signedPayload = timestamp + "." + payload;

        // 4. Tính toán expected signature
        String expectedSignature = calculateHmacSha256(signedPayload, webhookSecret);

        // 5. So sánh signature
        return receivedSignature.equals(expectedSignature);

    } catch (Exception e) {
        System.err.println("Lỗi xác thực webhook signature: " + e.getMessage());
        return false;
    }
}
```

### Phương thức calculateHmacSha256()

```java
private String calculateHmacSha256(String data, String secret) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    mac.init(secretKey);
    byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

    // Convert byte array to hex string
    StringBuilder hexString = new StringBuilder();
    for (byte b : hash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) {
            hexString.append('0');
        }
        hexString.append(hex);
    }

    return hexString.toString();
}
```

## Cấu hình

### 1. Trong application.properties

```properties
stripe.webhook.secret=whsec_your_webhook_secret_here
```

### 2. Lấy webhook secret từ Stripe Dashboard

1. Đăng nhập Stripe Dashboard
2. Vào Webhooks → Endpoints
3. Tạo endpoint mới hoặc chọn endpoint hiện có
4. Copy "Signing secret"

## Testing

### 1. Test với Stripe CLI

```bash
# Install Stripe CLI
stripe listen --forward-to localhost:8080/api/payments/stripe/webhook

# Test webhook
stripe trigger checkout.session.completed
```

### 2. Test với Postman

```bash
POST http://localhost:8080/api/payments/stripe/webhook
Headers:
  Stripe-Signature: t=1234567890,v1=abc123def456...
  Content-Type: application/json

Body:
{
  "id": "evt_test_webhook",
  "object": "event",
  "type": "checkout.session.completed",
  "data": {
    "object": {
      "id": "cs_test_...",
      "metadata": {
        "invoice_id": "1"
      }
    }
  }
}
```

### 3. Test signature verification

```java
// Test case 1: Valid signature
String payload = "{\"test\":\"data\"}";
String signature = "t=1234567890,v1=valid_signature";
boolean isValid = stripeService.verifyWebhookSignature(payload, signature);

// Test case 2: Invalid signature
String invalidSignature = "t=1234567890,v1=invalid_signature";
boolean isInvalid = stripeService.verifyWebhookSignature(payload, invalidSignature);
```

## Security Best Practices

### 1. Luôn verify signature

```java
@PostMapping("/stripe/webhook")
public ResponseEntity<?> stripeWebhook(@RequestBody String payload,
                                     @RequestHeader("Stripe-Signature") String signature) {
    // LUÔN verify signature trước khi xử lý
    if (!stripeService.verifyWebhookSignature(payload, signature)) {
        return ResponseEntity.badRequest().body("Webhook signature không hợp lệ");
    }

    // Xử lý webhook
    Payment payment = stripeService.handleWebhookEvent("checkout.session.completed", payload);
    return ResponseEntity.ok(payment);
}
```

### 2. Sử dụng webhook secret an toàn

- Không commit webhook secret vào code
- Sử dụng environment variables
- Rotate webhook secret định kỳ

### 3. Logging an toàn

```java
// Tốt: Log thông tin cơ bản
System.err.println("Lỗi xác thực webhook signature");

// Không tốt: Log payload hoặc secret
System.err.println("Payload: " + payload);
System.err.println("Secret: " + webhookSecret);
```

### 4. Error handling

```java
try {
    return stripeService.verifyWebhookSignature(payload, signature);
} catch (Exception e) {
    // Log lỗi nhưng không expose thông tin nhạy cảm
    logger.error("Webhook signature verification failed", e);
    return false;
}
```

## Troubleshooting

### Lỗi thường gặp:

1. **"Webhook signature không hợp lệ"**

   - Kiểm tra webhook secret có đúng không
   - Kiểm tra format signature
   - Kiểm tra payload có bị modify không

2. **"Signature format không đúng"**

   - Kiểm tra header Stripe-Signature
   - Đảm bảo format: `t=timestamp,v1=signature`

3. **"HMAC calculation failed"**
   - Kiểm tra webhook secret encoding
   - Kiểm tra algorithm (HmacSHA256)

### Debug:

```java
// Thêm debug logging (chỉ trong development)
System.out.println("Timestamp: " + timestamp);
System.out.println("Received signature: " + receivedSignature);
System.out.println("Expected signature: " + expectedSignature);
System.out.println("Signed payload: " + signedPayload);
```

## Lưu ý quan trọng

1. **Luôn verify signature** trước khi xử lý webhook
2. **Không trust webhook** nếu signature không hợp lệ
3. **Sử dụng HTTPS** cho webhook endpoint
4. **Test thoroughly** trước khi deploy production
5. **Monitor webhook failures** để phát hiện vấn đề sớm

