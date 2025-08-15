# Hướng dẫn Test Webhook Signature Verification

## Cách viết và test webhook signature verification

### 1. **Unit Test với JUnit**

```java
@Test
void testVerifyWebhookSignature_ValidSignature() {
    // Arrange
    String payload = "{\"id\":\"evt_test\",\"type\":\"checkout.session.completed\"}";
    String timestamp = "1234567890";
    String signature = createValidSignature(payload, timestamp);

    // Act
    boolean result = stripeService.verifyWebhookSignature(payload, signature);

    // Assert
    assertTrue(result, "Signature hợp lệ phải trả về true");
}
```

### 2. **Test với Postman**

#### **Bước 1: Tạo test signature**

```bash
POST http://localhost:8080/api/test/create-test-signature
Content-Type: application/json

{
  "payload": "{\"id\":\"evt_test\",\"type\":\"checkout.session.completed\"}",
  "timestamp": "1234567890"
}
```

**Response:**

```json
{
  "payload": "{\"id\":\"evt_test\",\"type\":\"checkout.session.completed\"}",
  "timestamp": "1234567890",
  "signature": "t=1234567890,v1=abc123def456...",
  "message": "Test signature đã được tạo"
}
```

#### **Bước 2: Test signature verification**

```bash
POST http://localhost:8080/api/test/webhook-signature
Content-Type: application/json
Stripe-Signature: t=1234567890,v1=abc123def456...

{
  "id": "evt_test",
  "type": "checkout.session.completed"
}
```

**Response:**

```json
{
  "valid": true,
  "payload": "{\"id\":\"evt_test\",\"type\":\"checkout.session.completed\"}",
  "signature": "t=1234567890,v1=abc123def456...",
  "message": "Signature hợp lệ!"
}
```

#### **Bước 3: Test webhook flow đầy đủ**

```bash
POST http://localhost:8080/api/test/test-webhook-flow
Content-Type: application/json

{
  "payload": "{\"id\":\"evt_test\",\"type\":\"checkout.session.completed\",\"data\":{\"object\":{\"id\":\"cs_test_123\",\"metadata\":{\"invoice_id\":\"1\"}}}}",
  "signature": "t=1234567890,v1=abc123def456..."
}
```

### 3. **Test với cURL**

#### **Test signature verification:**

```bash
# Tạo test signature
curl -X POST http://localhost:8080/api/test/create-test-signature \
  -H "Content-Type: application/json" \
  -d '{
    "payload": "{\"id\":\"evt_test\",\"type\":\"checkout.session.completed\"}",
    "timestamp": "1234567890"
  }'

# Test signature verification
curl -X POST http://localhost:8080/api/test/webhook-signature \
  -H "Content-Type: application/json" \
  -H "Stripe-Signature: t=1234567890,v1=abc123def456..." \
  -d '{
    "id": "evt_test",
    "type": "checkout.session.completed"
  }'
```

### 4. **Test Cases chi tiết**

#### **Test Case 1: Valid Signature**

```java
@Test
void testValidSignature() {
    String payload = "{\"id\":\"evt_test\",\"type\":\"checkout.session.completed\"}";
    String signature = createValidSignature(payload, "1234567890");

    boolean result = stripeService.verifyWebhookSignature(payload, signature);
    assertTrue(result);
}
```

#### **Test Case 2: Invalid Signature**

```java
@Test
void testInvalidSignature() {
    String payload = "{\"id\":\"evt_test\"}";
    String invalidSignature = "t=1234567890,v1=invalid_signature";

    boolean result = stripeService.verifyWebhookSignature(payload, invalidSignature);
    assertFalse(result);
}
```

#### **Test Case 3: Null Signature**

```java
@Test
void testNullSignature() {
    String payload = "{\"id\":\"evt_test\"}";

    boolean result = stripeService.verifyWebhookSignature(payload, null);
    assertFalse(result);
}
```

#### **Test Case 4: Invalid Format**

```java
@Test
void testInvalidFormat() {
    String payload = "{\"id\":\"evt_test\"}";
    String invalidFormat = "invalid_format";

    boolean result = stripeService.verifyWebhookSignature(payload, invalidFormat);
    assertFalse(result);
}
```

### 5. **Test với Stripe CLI (Production-like)**

#### **Cài đặt Stripe CLI:**

```bash
# macOS
brew install stripe/stripe-cli/stripe

# Windows
# Download từ https://github.com/stripe/stripe-cli/releases

# Linux
# Download từ https://github.com/stripe/stripe-cli/releases
```

#### **Login và test:**

```bash
# Login với Stripe
stripe login

# Listen webhook events
stripe listen --forward-to localhost:8080/api/payments/stripe/webhook

# Trigger test event
stripe trigger checkout.session.completed
```

### 6. **Debug và Troubleshooting**

#### **Thêm debug logging:**

```java
@Override
public boolean verifyWebhookSignature(String payload, String signature) {
    try {
        // Debug logging
        System.out.println("=== Webhook Signature Debug ===");
        System.out.println("Payload: " + payload);
        System.out.println("Signature: " + signature);

        if (signature == null || !signature.startsWith("t=")) {
            System.out.println("❌ Invalid signature format");
            return false;
        }

        String[] parts = signature.split(",");
        String timestamp = parts[0].substring(2);
        String receivedSignature = parts[1].substring(3);

        System.out.println("Timestamp: " + timestamp);
        System.out.println("Received signature: " + receivedSignature);

        String signedPayload = timestamp + "." + payload;
        String expectedSignature = calculateHmacSha256(signedPayload, webhookSecret);

        System.out.println("Signed payload: " + signedPayload);
        System.out.println("Expected signature: " + expectedSignature);
        System.out.println("Match: " + receivedSignature.equals(expectedSignature));

        return receivedSignature.equals(expectedSignature);

    } catch (Exception e) {
        System.err.println("❌ Error: " + e.getMessage());
        return false;
    }
}
```

### 7. **Test với Real Stripe Webhook**

#### **Thiết lập webhook endpoint:**

1. Đăng nhập Stripe Dashboard
2. Vào Webhooks → Endpoints
3. Tạo endpoint: `https://your-domain.com/api/payments/stripe/webhook`
4. Chọn events: `checkout.session.completed`, `payment_intent.succeeded`
5. Copy webhook secret

#### **Test với real payment:**

```bash
# Tạo test payment
curl -X POST http://localhost:8080/api/payments/initiate/1?paymentMethod=CARD \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Sử dụng payment URL để thanh toán
# Stripe sẽ gửi webhook về endpoint của bạn
```

### 8. **Validation Checklist**

- ✅ **Valid signature format**: `t=timestamp,v1=signature`
- ✅ **HMAC-SHA256 calculation**: Đúng thuật toán
- ✅ **Webhook secret**: Đúng secret key
- ✅ **Payload integrity**: Không bị modify
- ✅ **Error handling**: Xử lý lỗi gracefully
- ✅ **Logging**: Log an toàn, không expose secret
- ✅ **Test coverage**: Test tất cả cases

### 9. **Performance Testing**

```java
@Test
void testPerformance() {
    String payload = "{\"id\":\"evt_test\",\"type\":\"checkout.session.completed\"}";
    String signature = createValidSignature(payload, "1234567890");

    long startTime = System.currentTimeMillis();

    for (int i = 0; i < 1000; i++) {
        stripeService.verifyWebhookSignature(payload, signature);
    }

    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;

    System.out.println("1000 verifications took: " + duration + "ms");
    assertTrue(duration < 1000, "Verification should be fast");
}
```

### 10. **Security Testing**

```java
@Test
void testSecurity() {
    // Test với malicious payload
    String maliciousPayload = "{\"id\":\"evt_malicious\",\"type\":\"injection\"}";
    String fakeSignature = "t=1234567890,v1=fake_signature";

    boolean result = stripeService.verifyWebhookSignature(maliciousPayload, fakeSignature);
    assertFalse(result, "Should reject malicious payload");
}
```

**Với các test cases này, bạn có thể đảm bảo webhook signature verification hoạt động chính xác và an toàn!** 🎯
