# Hướng dẫn tích hợp thanh toán với Stripe

## Tổng quan

Hệ thống thanh toán đã được tích hợp với Stripe để xử lý thanh toán thẻ tín dụng/ghi nợ, cùng với các kênh thanh toán khác như MOMO, VNPAY, và chuyển khoản ngân hàng.

## Luồng hoạt động

### 1. Tạo Invoice từ Booking

```bash
POST /api/invoices/create-from-booking/{bookingId}
```

- Tạo invoice với trạng thái `NOT_PAID`
- Tạo QR code chứa invoice ID

### 2. Khởi tạo thanh toán

```bash
POST /api/payments/initiate/{invoiceId}?paymentMethod={METHOD}
```

**Các phương thức thanh toán hỗ trợ:**

- `CARD` - Thanh toán thẻ qua Stripe
- `MOMO` - Ví điện tử MOMO
- `VNPAY` - Cổng thanh toán VNPAY
- `BANK_TRANSFER` - Chuyển khoản ngân hàng

**Response:**

```json
{
  "paymentId": 1,
  "invoiceId": 1,
  "amount": 50000.0,
  "currency": "VND",
  "description": "Thanh toán thuê xe - Invoice #1",
  "status": "PENDING",
  "paymentMethod": "CARD",
  "qrCode": "STRIPE:cs_test_...",
  "paymentUrl": "https://checkout.stripe.com/...",
  "stripeSessionId": "cs_test_..."
}
```

### 3. Xử lý thanh toán

#### Thanh toán thẻ (Stripe):

- User được redirect đến Stripe Checkout
- Sau khi thanh toán thành công, Stripe gửi webhook về `/api/payments/stripe/webhook`
- Hệ thống cập nhật trạng thái payment thành `SUCCESS`
- Cập nhật trạng thái invoice thành `PAID`

#### Thanh toán khác (MOMO, VNPAY, Bank):

- User quét QR code hoặc truy cập payment URL
- Kênh thanh toán gửi callback về `/api/payments/callback`
- Hệ thống cập nhật trạng thái tương ứng

### 4. Kiểm tra trạng thái

```bash
GET /api/payments/status/{invoiceId}
GET /api/payments/info/{invoiceId}
```

## Cấu hình Stripe

### 1. Thêm vào `application.properties`:

```properties
stripe.secret.key=sk_test_your_stripe_secret_key_here
stripe.publishable.key=pk_test_your_stripe_publishable_key_here
stripe.webhook.secret=whsec_your_webhook_secret_here
```

### 2. Thiết lập webhook trong Stripe Dashboard:

- URL: `https://your-domain.com/api/payments/stripe/webhook`
- Events: `checkout.session.completed`, `payment_intent.succeeded`, `payment_intent.payment_failed`

## Cấu trúc Database

### Bảng `payments`:

- `id` - ID payment
- `invoice_id` - Liên kết với invoice
- `amount` - Số tiền thanh toán
- `status` - Trạng thái (PENDING, SUCCESS, FAILED, CANCELLED)
- `payment_method` - Phương thức thanh toán
- `stripe_session_id` - Stripe session ID (nếu dùng thẻ)
- `stripe_payment_intent_id` - Stripe payment intent ID
- `qr_code` - QR code thanh toán
- `payment_url` - URL thanh toán

### Bảng `invoices`:

- `payment_status` - Trạng thái thanh toán (NOT_PAID, PAID)

## Security

### 1. Webhook Signature Verification

- Stripe gửi webhook với chữ ký HMAC-SHA256
- Hệ thống xác thực chữ ký trước khi xử lý

### 2. Payment Validation

- Kiểm tra trạng thái invoice trước khi thanh toán
- Ngăn chặn thanh toán trùng lặp

## Testing

### 1. Stripe Test Mode

- Sử dụng test keys: `sk_test_...`, `pk_test_...`
- Test card: `4242 4242 4242 4242`

### 2. Webhook Testing

- Sử dụng Stripe CLI để test webhook locally
- Command: `stripe listen --forward-to localhost:8080/api/payments/stripe/webhook`

## Lưu ý quan trọng

1. **Luôn xác thực webhook signature** từ Stripe
2. **Xử lý lỗi thanh toán** một cách graceful
3. **Lưu log** tất cả các giao dịch thanh toán
4. **Implement retry mechanism** cho webhook processing
5. **Test thoroughly** trước khi deploy production

## Troubleshooting

### Lỗi thường gặp:

1. **Webhook signature không hợp lệ**: Kiểm tra webhook secret
2. **Stripe session expired**: Tạo session mới
3. **Payment intent failed**: Kiểm tra card details và balance

### Debug:

- Kiểm tra logs trong console
- Sử dụng Stripe Dashboard để theo dõi transactions
- Verify webhook delivery trong Stripe Dashboard

