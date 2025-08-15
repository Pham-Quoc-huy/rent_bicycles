# Hướng dẫn Test Payment Success

## 🎯 **Tổng quan**

Hướng dẫn test thanh toán thành công cho invoice trong hệ thống Rent Bicycles.

## 📋 **Các cách test Payment Success**

### **Cách 1: Sử dụng VNPay Test Environment**

#### **Bước 1: Truy cập Payment URL**

Từ response của API `POST /api/payments/create`, copy URL từ field `paymentUrl`:

```
https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_BankCode=&vnp_CurrCode=VND&vnp_OrderType=other&vnp_ReturnUrl=http%3A%2F%2Flocalhost%3A3000%2Fpayment%2Freturn&vnp_TmnCode=your_tmn_code_here&vnp_TxnRef=VNPAY175526440270273&vnp_OrderInfo=Thanh+to%C3%A1n+thu%C3%AA+xe+%C4%91%E1%BA%A1p&vnp_Amount=4000000&vnp_IpAddr=127.0.0.1&vnp_Locale=vn&vnp_Command=pay&vnp_CreateDate=20250815202642&vnp_Version=2.1.0&vnp_SecureHash=6c99045530a65d8a1161c9676a950a92a829acc8639d3ac60950dfc75449fb1ddefc20f462c99612a53a1e8b1b63773d955844dbfdf8c091a1abd26edd8d7724
```

#### **Bước 2: Sử dụng VNPay Test Cards**

Khi vào trang thanh toán VNPay, sử dụng thông tin test:

**Thông tin thẻ test:**

- **Card Number**: `9704000000000018`
- **Card Holder**: `NGUYEN VAN A`
- **Issue Date**: `07/15`
- **OTP**: `123456`

**Hoặc sử dụng QR Code:**

- Copy URL từ field `qrCode` trong response
- Scan QR code bằng app VNPay

#### **Bước 3: Kiểm tra kết quả**

Sau khi thanh toán thành công:

1. VNPay sẽ redirect về `returnUrl`
2. Payment status sẽ được cập nhật thành `SUCCESS`
3. Có thể scan QR code để lấy xe

### **Cách 2: Sử dụng Postman Collection**

#### **Bước 1: Import Collection**

Import file `TEST_PAYMENT_SUCCESS.postman_collection.json` vào Postman.

#### **Bước 2: Cấu hình Variables**

- **baseUrl**: `http://localhost:8080`
- **authToken**: Token sau khi đăng nhập
- **invoiceId**: ID của invoice cần thanh toán
- **paymentId**: ID của payment đã tạo

#### **Bước 3: Chạy test sequence**

1. **1. Tạo Payment Success** - Tạo payment mới
2. **2. Simulate VNPay Success Webhook** - Giả lập webhook thành công
3. **3. Kiểm tra Payment Status** - Xác nhận payment status
4. **4. Kiểm tra Invoice Status** - Xác nhận invoice status
5. **5. Test QR Code Scan** - Test scan QR code

### **Cách 3: Sử dụng Test Controller**

#### **Bước 1: Mark Payment as Success**

```bash
POST {{baseUrl}}/api/test/test-payment-success
Content-Type: application/json
Authorization: Bearer {{authToken}}

{
  "paymentId": 1
}
```

**Expected Response:**

```json
{
  "success": true,
  "message": "Payment marked as successful for testing",
  "payment": {
    "id": 1,
    "status": "SUCCESS",
    "gatewayId": "TEST_SUCCESS_175526440270273",
    "transactionId": "TEST_TXN_175526440270273",
    "updatedAt": "2025-08-15T20:30:00"
  }
}
```

#### **Bước 2: Mark Payment as Failed (Optional)**

```bash
POST {{baseUrl}}/api/test/test-payment-failed
Content-Type: application/json
Authorization: Bearer {{authToken}}

{
  "paymentId": 1
}
```

### **Cách 4: Simulate VNPay Webhook**

#### **Bước 1: Tạo VNPay Success Webhook**

```bash
POST {{baseUrl}}/api/payments/webhook/vnpay
Content-Type: application/x-www-form-urlencoded

vnp_TxnRef=VNPAY175526440270273&vnp_Amount=4000000&vnp_OrderInfo=Thanh toán thuê xe đạp&vnp_ResponseCode=00&vnp_TransactionNo=12345678&vnp_BankCode=NCB&vnp_PayDate=20250815203000&vnp_Message=Giao dịch thành công&vnp_SecureHash=test_signature
```

**Parameters:**

- `vnp_TxnRef`: Transaction reference (từ payment)
- `vnp_Amount`: Amount in VND (amount \* 100)
- `vnp_ResponseCode`: `00` = Success
- `vnp_TransactionNo`: VNPay transaction number
- `vnp_BankCode`: Bank code (NCB, VISA, etc.)
- `vnp_PayDate`: Payment date (YYYYMMDDHHMMSS)
- `vnp_Message`: Success message
- `vnp_SecureHash`: Test signature

## 🧪 **Test Cases**

### **Test Case 1: Payment Success Flow**

1. ✅ Tạo payment với status `PENDING`
2. ✅ Simulate VNPay success webhook
3. ✅ Kiểm tra payment status = `SUCCESS`
4. ✅ Kiểm tra invoice có thể scan QR code
5. ✅ Test scan QR code thành công

### **Test Case 2: Payment Failed Flow**

1. ✅ Tạo payment với status `PENDING`
2. ✅ Simulate VNPay failed webhook
3. ✅ Kiểm tra payment status = `FAILED`
4. ✅ Kiểm tra invoice không thể scan QR code
5. ✅ Test scan QR code thất bại

### **Test Case 3: QR Code After Payment Success**

1. ✅ Payment status = `SUCCESS`
2. ✅ Scan QR code → Success response
3. ✅ Pickup bike → Success
4. ✅ Return bike → Success

## 📊 **Expected Results**

### **Payment Success Response:**

```json
{
  "id": 1,
  "invoiceId": 1,
  "amount": 40000,
  "currency": "VND",
  "paymentMethod": "VNPAY",
  "status": "SUCCESS",
  "gatewayId": "VNPAY175526440270273",
  "transactionId": "12345678",
  "createdAt": "2025-08-15T20:26:42",
  "updatedAt": "2025-08-15T20:30:00"
}
```

### **Invoice After Payment Success:**

```json
{
  "id": 1,
  "totalPrice": 40000,
  "bikeQuantity": 2,
  "bikeStatus": "NOT_PICKED_UP",
  "rentalStartTime": null,
  "rentalEndTime": null,
  "totalTime": 0,
  "station": {
    "id": 1,
    "location": "Trạm xe số 1"
  },
  "payments": [
    {
      "id": 1,
      "status": "SUCCESS",
      "amount": 40000,
      "paymentMethod": "VNPAY"
    }
  ]
}
```

### **QR Code Scan After Payment Success:**

```json
{
  "invoiceId": 1,
  "bikeQuantity": 2,
  "stationName": "Trạm xe số 1",
  "bikeStatus": "NOT_PICKED_UP",
  "totalPrice": 40000,
  "canProceed": true,
  "message": "Có thể tiến hành lấy xe"
}
```

## 🔧 **Troubleshooting**

### **1. Payment không được cập nhật status:**

- Kiểm tra webhook URL có đúng không
- Kiểm tra webhook signature có hợp lệ không
- Kiểm tra payment ID có tồn tại không

### **2. QR code scan thất bại:**

- Kiểm tra payment status có = `SUCCESS` không
- Kiểm tra invoice có tồn tại không
- Kiểm tra QR code có hợp lệ không

### **3. Webhook không được gọi:**

- Kiểm tra VNPay configuration
- Kiểm tra callback URL có đúng không
- Kiểm tra network connectivity

## 🎉 **Kết luận**

Sau khi test payment success thành công:

- ✅ **Payment status** = `SUCCESS`
- ✅ **Invoice** có thể scan QR code
- ✅ **QR code scan** trả về `canProceed: true`
- ✅ **Pickup bike** hoạt động bình thường
- ✅ **Return bike** hoạt động bình thường

**Payment flow đã hoạt động chính xác!** 🚀
