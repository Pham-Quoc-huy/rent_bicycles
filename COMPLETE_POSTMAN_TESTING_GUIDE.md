# Hướng dẫn Test Postman - Tất cả chức năng của Project

## 📋 **Tổng quan**

Hướng dẫn test toàn bộ API của hệ thống thuê xe đạp bằng Postman, bao gồm: Authentication, User Management, Booking, Invoice, Payment, và Admin functions.

---

## 🚀 **Thiết lập Postman**

### **1. Import Collection**

1. Mở Postman
2. Click **Import** → Chọn file `Rent_Bicycles_Complete_API.postman_collection.json`
3. Collection sẽ được import với tất cả requests

### **2. Thiết lập Environment**

1. Tạo Environment mới: **Rent Bicycles API**
2. Thêm variables:
   - `baseUrl`: `http://localhost:8080`
   - `jwtToken`: (để trống, sẽ tự động điền)
   - `adminToken`: (để trống, sẽ tự động điền)
   - `userId`: (để trống, sẽ tự động điền)
   - `bookingId`: (để trống, sẽ tự động điền)
   - `invoiceId`: (để trống, sẽ tự động điền)

---

## 🔐 **1. Authentication & User Management**

### **1.1 Đăng ký tài khoản**

```bash
POST {{baseUrl}}/api/auth/register
Content-Type: application/json

{
  "fullName": "Nguyễn Văn A",
  "email": "user@example.com",
  "password": "123456",
  "phone": "0123456789"
}
```

**Response:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "Nguyễn Văn A"
  }
}
```

### **1.2 Đăng nhập**

```bash
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "123456"
}
```

**Test Script (tự động lưu token):**

```javascript
if (pm.response.code === 200) {
  const response = pm.response.json();
  pm.collectionVariables.set("jwtToken", response.token);
  pm.collectionVariables.set("userId", response.user.id);
  console.log("JWT Token saved:", response.token);
}
```

### **1.3 Google Login**

```bash
POST {{baseUrl}}/api/auth/google-login
Content-Type: application/json

{
  "idToken": "google_id_token_here"
}
```

### **1.4 Lấy thông tin user hiện tại**

```bash
GET {{baseUrl}}/api/auth/me
Authorization: Bearer {{jwtToken}}
```

---

## 🏪 **2. Station Management**

### **2.1 Lấy danh sách tất cả stations**

```bash
GET {{baseUrl}}/api/stations
Authorization: Bearer {{jwtToken}}
```

### **2.2 Lấy thông tin station theo ID**

```bash
GET {{baseUrl}}/api/stations/1
Authorization: Bearer {{jwtToken}}
```

### **2.3 Tìm stations gần đây**

```bash
GET {{baseUrl}}/api/stations/nearby?latitude=10.762622&longitude=106.660172&radius=5000
Authorization: Bearer {{jwtToken}}
```

---

## 📅 **3. Booking Management**

### **3.1 Tạo booking mới**

```bash
POST {{baseUrl}}/api/bookings/create
Content-Type: application/json
Authorization: Bearer {{jwtToken}}

{
  "stationId": 1,
  "bikeQuantity": 2,
  "notes": "Thuê xe cho cuối tuần"
}
```

**Test Script (lưu booking ID):**

```javascript
if (pm.response.code === 200) {
  const response = pm.response.json();
  if (response.booking && response.booking.id) {
    pm.collectionVariables.set("bookingId", response.booking.id);
    console.log("Booking ID saved:", response.booking.id);
  }
}
```

### **3.2 Lấy danh sách booking của user**

```bash
GET {{baseUrl}}/api/bookings/my-bookings
Authorization: Bearer {{jwtToken}}
```

### **3.3 Lấy thông tin booking theo ID**

```bash
GET {{baseUrl}}/api/bookings/{{bookingId}}
Authorization: Bearer {{jwtToken}}
```

### **3.4 Hủy booking**

```bash
POST {{baseUrl}}/api/bookings/{{bookingId}}/cancel
Authorization: Bearer {{jwtToken}}
```

---

## 🧾 **4. Invoice Management**

### **4.1 Tạo invoice từ booking**

```bash
POST {{baseUrl}}/api/invoices/create-from-booking/{{bookingId}}
Authorization: Bearer {{jwtToken}}
```

**Test Script (lưu invoice ID):**

```javascript
if (pm.response.code === 200) {
  const response = pm.response.json();
  pm.collectionVariables.set("invoiceId", response.id);
  console.log("Invoice ID saved:", response.id);
}
```

### **4.2 Lấy invoice theo booking ID**

```bash
GET {{baseUrl}}/api/invoices/booking/{{bookingId}}
Authorization: Bearer {{jwtToken}}
```

### **4.3 Lấy invoice theo ID**

```bash
GET {{baseUrl}}/api/invoices/{{invoiceId}}
Authorization: Bearer {{jwtToken}}
```

### **4.4 Lấy danh sách invoice của user**

```bash
GET {{baseUrl}}/api/invoices/my-invoices
Authorization: Bearer {{jwtToken}}
```

### **4.5 Lấy invoice chưa thanh toán**

```bash
GET {{baseUrl}}/api/invoices/unpaid
Authorization: Bearer {{jwtToken}}
```

---

## 💳 **5. Payment Management**

### **5.1 Khởi tạo thanh toán - Thẻ (Stripe)**

```bash
POST {{baseUrl}}/api/payments/initiate/{{invoiceId}}?paymentMethod=CARD
Authorization: Bearer {{jwtToken}}
```

### **5.2 Khởi tạo thanh toán - MOMO**

```bash
POST {{baseUrl}}/api/payments/initiate/{{invoiceId}}?paymentMethod=MOMO
Authorization: Bearer {{jwtToken}}
```

### **5.3 Khởi tạo thanh toán - VNPAY**

```bash
POST {{baseUrl}}/api/payments/initiate/{{invoiceId}}?paymentMethod=VNPAY
Authorization: Bearer {{jwtToken}}
```

### **5.4 Khởi tạo thanh toán - Bank Transfer**

```bash
POST {{baseUrl}}/api/payments/initiate/{{invoiceId}}?paymentMethod=BANK_TRANSFER
Authorization: Bearer {{jwtToken}}
```

### **5.5 Kiểm tra trạng thái thanh toán**

```bash
GET {{baseUrl}}/api/payments/check-status/{{invoiceId}}
Authorization: Bearer {{jwtToken}}
```

### **5.6 Lấy thông tin thanh toán**

```bash
GET {{baseUrl}}/api/payments/info/{{invoiceId}}
Authorization: Bearer {{jwtToken}}
```

### **5.7 Hủy thanh toán**

```bash
POST {{baseUrl}}/api/payments/cancel/{{invoiceId}}
Authorization: Bearer {{jwtToken}}
```

### **5.8 Lấy danh sách payment của invoice**

```bash
GET {{baseUrl}}/api/payments/invoice/{{invoiceId}}/payments
Authorization: Bearer {{jwtToken}}
```

---

## 🔄 **6. QR Code & Bike Operations**

### **6.1 Kiểm tra QR code để lấy xe**

```bash
POST {{baseUrl}}/api/invoices/check-pickup
Content-Type: application/json
Authorization: Bearer {{jwtToken}}

{
  "qrCode": "INVOICE:{{invoiceId}}"
}
```

### **6.2 Lấy xe (pickup)**

```bash
POST {{baseUrl}}/api/invoices/{{invoiceId}}/pickup
Authorization: Bearer {{jwtToken}}
```

### **6.3 Trả xe (return)**

```bash
POST {{baseUrl}}/api/invoices/{{invoiceId}}/return
Authorization: Bearer {{jwtToken}}
```

---

## 🧪 **7. Test & Development**

### **7.1 Test webhook signature**

```bash
POST {{baseUrl}}/api/test/webhook-signature
Content-Type: application/json
Stripe-Signature: t=1234567890,v1=abc123def456...

{
  "id": "evt_test",
  "type": "checkout.session.completed"
}
```

### **7.2 Tạo test signature**

```bash
POST {{baseUrl}}/api/test/create-test-signature
Content-Type: application/json

{
  "payload": "{\"id\":\"evt_test\",\"type\":\"checkout.session.completed\"}",
  "timestamp": "1234567890"
}
```

### **7.3 Test webhook flow đầy đủ**

```bash
POST {{baseUrl}}/api/test/test-webhook-flow
Content-Type: application/json

{
  "payload": "{\"id\":\"evt_test\",\"type\":\"checkout.session.completed\",\"data\":{\"object\":{\"id\":\"cs_test_123\",\"metadata\":{\"invoice_id\":\"{{invoiceId}}\"}}}}",
  "signature": "t=1234567890,v1=abc123def456..."
}
```

---

## 👨‍💼 **8. Admin Functions**

### **8.1 Tạo admin account**

```bash
POST {{baseUrl}}/api/admin/create-admin
Content-Type: application/json

{
  "fullName": "Admin User",
  "email": "admin@example.com",
  "password": "admin123"
}
```

### **8.2 Admin login**

```bash
POST {{baseUrl}}/api/admin/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "admin123"
}
```

**Test Script (lưu admin token):**

```javascript
if (pm.response.code === 200) {
  const response = pm.response.json();
  pm.collectionVariables.set("adminToken", response.token);
  console.log("Admin Token saved:", response.token);
}
```

### **8.3 Lấy tất cả bookings (Admin)**

```bash
GET {{baseUrl}}/api/admin/bookings
Authorization: Bearer {{adminToken}}
```

### **8.4 Lấy tất cả invoices (Admin)**

```bash
GET {{baseUrl}}/api/admin/invoices
Authorization: Bearer {{adminToken}}
```

### **8.5 Lấy thống kê doanh thu (Admin)**

```bash
GET {{baseUrl}}/api/admin/revenue
Authorization: Bearer {{adminToken}}
```

### **8.6 Lấy thống kê theo thời gian (Admin)**

```bash
GET {{baseUrl}}/api/admin/revenue/range?startDate=2024-01-01&endDate=2024-12-31
Authorization: Bearer {{adminToken}}
```

---

## 🔧 **9. Stripe Webhook Testing**

### **9.1 Test Stripe webhook**

```bash
POST {{baseUrl}}/api/payments/stripe/webhook
Content-Type: application/json
Stripe-Signature: t=1234567890,v1=abc123def456...

{
  "id": "evt_test_webhook",
  "object": "event",
  "type": "checkout.session.completed",
  "data": {
    "object": {
      "id": "cs_test_...",
      "object": "checkout.session",
      "metadata": {
        "invoice_id": "{{invoiceId}}",
        "user_id": "{{userId}}"
      },
      "payment_status": "paid",
      "amount_total": 100000
    }
  }
}
```

---

## 📊 **10. Thứ tự test hoàn chỉnh**

### **Flow 1: User Registration & Booking**

1. **Register** → Lưu JWT token
2. **Get Stations** → Chọn station
3. **Create Booking** → Lưu booking ID
4. **Create Invoice** → Lưu invoice ID
5. **Initiate Payment** → Chọn phương thức thanh toán
6. **Check Payment Status** → Kiểm tra trạng thái

### **Flow 2: Payment Processing**

1. **Test Webhook Signature** → Verify signature
2. **Test Stripe Webhook** → Simulate payment success
3. **Check Payment Status** → Verify payment completed
4. **Check Invoice Status** → Verify invoice marked as PAID

### **Flow 3: Bike Operations**

1. **Check Pickup** → Verify QR code
2. **Pickup Bike** → Mark bike as picked up
3. **Return Bike** → Mark bike as returned

### **Flow 4: Admin Operations**

1. **Create Admin** → Tạo admin account
2. **Admin Login** → Lưu admin token
3. **Get All Bookings** → View all bookings
4. **Get Revenue Stats** → View revenue statistics

---

## 🚨 **11. Error Handling Tests**

### **11.1 Test invalid JWT**

```bash
GET {{baseUrl}}/api/auth/me
Authorization: Bearer invalid_token_here
```

### **11.2 Test unauthorized access**

```bash
GET {{baseUrl}}/api/admin/bookings
# Không có Authorization header
```

### **11.3 Test invalid payment method**

```bash
POST {{baseUrl}}/api/payments/initiate/{{invoiceId}}?paymentMethod=INVALID
Authorization: Bearer {{jwtToken}}
```

### **11.4 Test invalid webhook signature**

```bash
POST {{baseUrl}}/api/payments/stripe/webhook
Content-Type: application/json
Stripe-Signature: invalid_signature

{
  "id": "evt_test"
}
```

---

## 📝 **12. Pre-request Scripts**

### **12.1 Auto-generate test data**

```javascript
// Pre-request script cho test
if (!pm.collectionVariables.get("testEmail")) {
  const timestamp = Date.now();
  pm.collectionVariables.set("testEmail", `test${timestamp}@example.com`);
}
```

### **12.2 Validate response**

```javascript
// Test script cho validation
pm.test("Status code is 200", function () {
  pm.response.to.have.status(200);
});

pm.test("Response has required fields", function () {
  const response = pm.response.json();
  pm.expect(response).to.have.property("id");
  pm.expect(response).to.have.property("status");
});
```

---

## 🎯 **13. Collection Variables**

### **13.1 Global Variables**

- `baseUrl`: `http://localhost:8080`
- `jwtToken`: JWT token của user
- `adminToken`: JWT token của admin
- `userId`: ID của user hiện tại
- `bookingId`: ID của booking hiện tại
- `invoiceId`: ID của invoice hiện tại

### **13.2 Environment Variables**

- `testEmail`: Email test tự động generate
- `testPhone`: Phone test
- `testStationId`: Station ID để test

---

## ✅ **14. Test Checklist**

### **Authentication**

- [ ] Register new user
- [ ] Login with valid credentials
- [ ] Login with invalid credentials
- [ ] Get current user info
- [ ] Google login (if configured)

### **Booking**

- [ ] Create booking
- [ ] Get user bookings
- [ ] Get booking by ID
- [ ] Cancel booking
- [ ] Create booking with invalid data

### **Invoice**

- [ ] Create invoice from booking
- [ ] Get invoice by ID
- [ ] Get user invoices
- [ ] Get unpaid invoices

### **Payment**

- [ ] Initiate payment with CARD
- [ ] Initiate payment with MOMO
- [ ] Initiate payment with VNPAY
- [ ] Check payment status
- [ ] Cancel payment
- [ ] Test webhook signature

### **Bike Operations**

- [ ] Check pickup with valid QR
- [ ] Check pickup with invalid QR
- [ ] Pickup bike
- [ ] Return bike

### **Admin Functions**

- [ ] Create admin account
- [ ] Admin login
- [ ] Get all bookings
- [ ] Get revenue stats

### **Error Handling**

- [ ] Test invalid JWT
- [ ] Test unauthorized access
- [ ] Test invalid data
- [ ] Test webhook signature validation

---

## 🚀 **15. Performance Testing**

### **15.1 Load Testing**

```bash
# Test multiple concurrent requests
# Sử dụng Postman Runner với iterations = 100
```

### **15.2 Response Time Testing**

```javascript
pm.test("Response time is less than 2000ms", function () {
  pm.expect(pm.response.responseTime).to.be.below(2000);
});
```

---

**Với hướng dẫn này, bạn có thể test toàn bộ chức năng của project một cách có hệ thống và đầy đủ!** 🎯

