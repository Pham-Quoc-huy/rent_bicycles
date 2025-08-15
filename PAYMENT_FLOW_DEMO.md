# Demo Luồng Thanh Toán - Cập Nhật Status Invoice

## Luồng hoạt động chi tiết

### 1. Tạo Booking và Invoice

```bash
# Tạo booking
POST /api/bookings/create
{
  "stationId": 1,
  "bikeQuantity": 2,
  "notes": "Thuê xe cho cuối tuần"
}

# Response sẽ tự động tạo invoice
{
  "success": true,
  "message": "Đặt xe thành công! Vui lòng đến trạm và quét mã QR để lấy xe.",
  "booking": {
    "id": 1,
    "status": "PENDING",
    "estimatedPrice": 1000.0
  }
}
```

### 2. Kiểm tra Invoice được tạo

```bash
GET /api/invoices/booking/1

# Response
{
  "id": 1,
  "paymentStatus": "NOT_PAID",  // ← Chưa thanh toán
  "bikeStatus": "NOT_PICKED_UP",
  "totalPrice": 1000.0,
  "qrCode": "INVOICE:1"
}
```

### 3. Khởi tạo thanh toán

```bash
# Thanh toán bằng thẻ (Stripe)
POST /api/payments/initiate/1?paymentMethod=CARD

# Response
{
  "paymentId": 1,
  "invoiceId": 1,
  "amount": 1000.0,
  "status": "PENDING",
  "paymentMethod": "CARD",
  "paymentUrl": "https://checkout.stripe.com/...",
  "stripeSessionId": "cs_test_..."
}

# Hoặc thanh toán bằng MOMO
POST /api/payments/initiate/1?paymentMethod=MOMO

# Response
{
  "paymentId": 1,
  "invoiceId": 1,
  "amount": 1000.0,
  "status": "PENDING",
  "paymentMethod": "MOMO",
  "qrCode": "MOMO:INVOICE:1|METHOD:MOMO|AMOUNT:1000.0|abc12345",
  "paymentUrl": "https://api.momo.vn/..."
}
```

### 4. Thanh toán thành công

#### Với Stripe (Thẻ):

1. User click vào `paymentUrl` → Redirect đến Stripe Checkout
2. User nhập thông tin thẻ và thanh toán
3. Stripe gửi webhook về `/api/payments/stripe/webhook`
4. Hệ thống xử lý webhook và cập nhật status

#### Với MOMO/VNPAY:

1. User quét QR code hoặc truy cập `paymentUrl`
2. Thanh toán trên app MOMO/VNPAY
3. Kênh thanh toán gửi callback về `/api/payments/callback`
4. Hệ thống xử lý callback và cập nhật status

### 5. Kiểm tra trạng thái sau thanh toán

```bash
# Kiểm tra trạng thái chi tiết
GET /api/payments/check-status/1

# Response sau khi thanh toán thành công
{
  "invoiceId": 1,
  "invoiceStatus": "PAID",  // ← Đã thanh toán!
  "totalAmount": 1000.0,
  "isPaid": true,
  "canPickup": true,  // ← Có thể lấy xe
  "payments": [
    {
      "id": 1,
      "status": "SUCCESS",
      "paymentMethod": "CARD",
      "amount": 1000.0,
      "paymentDate": "2024-01-15T10:30:00",
      "stripeSessionId": "cs_test_...",
      "transactionId": "pi_..."
    }
  ],
  "latestPayment": {
    "id": 1,
    "status": "SUCCESS",
    "paymentMethod": "CARD",
    "amount": 1000.0,
    "paymentDate": "2024-01-15T10:30:00"
  }
}
```

### 6. Kiểm tra Invoice sau thanh toán

```bash
GET /api/invoices/1

# Response
{
  "id": 1,
  "paymentStatus": "PAID",  // ← Đã thanh toán!
  "bikeStatus": "NOT_PICKED_UP",
  "totalPrice": 1000.0,
  "qrCode": "INVOICE:1"
}
```

## Các trạng thái quan trọng

### Invoice Status:

- `NOT_PAID` - Chưa thanh toán
- `PAID` - Đã thanh toán thành công

### Payment Status:

- `PENDING` - Đang chờ thanh toán
- `SUCCESS` - Thanh toán thành công
- `FAILED` - Thanh toán thất bại
- `CANCELLED` - Đã hủy thanh toán

### Bike Status:

- `NOT_PICKED_UP` - Chưa lấy xe
- `PICKED_UP` - Đã lấy xe
- `RETURNED` - Đã trả xe

## Logic cập nhật status

### Trong `PaymentServiceImpl.confirmPayment()`:

```java
// Cập nhật payment
payment.setStatus("SUCCESS");
payment.setTransactionId(transactionId);
payment.setPaymentDate(LocalDateTime.now());

// Cập nhật invoice ← ĐÂY LÀ ĐIỂM QUAN TRỌNG!
invoice.setPaymentStatus("PAID");
invoiceRepository.save(invoice);

// Lưu payment
return paymentRepository.save(payment);
```

### Trong `StripeServiceImpl.handleWebhookEvent()`:

```java
case "checkout.session.completed":
    return handleCheckoutSessionCompleted(event);
case "payment_intent.succeeded":
    return handlePaymentIntentSucceeded(event);
```

## Kiểm tra nhanh

```bash
# 1. Tạo booking
curl -X POST http://localhost:8080/api/bookings/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"stationId": 1, "bikeQuantity": 1}'

# 2. Lấy invoice ID từ response
# Giả sử invoice ID = 1

# 3. Khởi tạo thanh toán
curl -X POST "http://localhost:8080/api/payments/initiate/1?paymentMethod=CARD"

# 4. Kiểm tra trạng thái
curl http://localhost:8080/api/payments/check-status/1

# 5. Sau khi thanh toán thành công, kiểm tra lại
curl http://localhost:8080/api/payments/check-status/1
# Sẽ thấy invoiceStatus: "PAID" và isPaid: true
```

## Lưu ý quan trọng

1. **Invoice status chỉ được cập nhật khi thanh toán thành công**
2. **Payment record được tạo trước, sau đó mới cập nhật invoice**
3. **Webhook từ Stripe phải được xử lý đúng để cập nhật status**
4. **Luôn kiểm tra trạng thái trước khi cho phép lấy xe**

