# Hướng dẫn Test toàn bộ chức năng Rent Bicycles

## 🎯 Tổng quan

Hướng dẫn test toàn bộ flow từ đăng ký, đăng nhập, đặt xe, thanh toán đến lấy xe bằng Postman.

## 📋 **Cài đặt và chuẩn bị**

### **1. Import Postman Collection:**

1. Mở Postman
2. Click "Import"
3. Chọn file `RENT_BICYCLES_FULL_FLOW.postman_collection.json`
4. Collection sẽ được import với tất cả các test cases

### **2. Cấu hình Environment:**

- **baseUrl**: `http://localhost:8080` (hoặc URL server của bạn)
- **authToken**: Sẽ được tự động set sau khi đăng nhập
- **userId**: Sẽ được tự động set sau khi đăng nhập
- **bookingId**: Sẽ được tự động set sau khi tạo booking
- **invoiceId**: Sẽ được tự động set sau khi tạo booking
- **paymentId**: Sẽ được tự động set sau khi tạo payment

## 🚀 **Test Flow chính**

### **Bước 1: Authentication Flow**

#### **1.1. Đăng ký tài khoản**

```bash
POST {{baseUrl}}/api/auth/register
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "123456",
  "fullName": "Nguyễn Văn Test",
  "phone": "0123456789"
}
```

**Expected Response:**

```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "email": "test@example.com",
      "fullName": "Nguyễn Văn Test"
    }
  },
  "message": "Đăng ký thành công"
}
```

#### **1.2. Đăng nhập**

```bash
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "123456"
}
```

**Expected Response:**

```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "email": "test@example.com",
      "fullName": "Nguyễn Văn Test"
    }
  },
  "message": "Đăng nhập thành công"
}
```

#### **1.3. Validate Token**

```bash
GET {{baseUrl}}/api/auth/validate
Authorization: Bearer {{authToken}}
```

**Expected Response:**

```json
{
  "success": true,
  "data": {
    "user": {
      "id": 1,
      "email": "test@example.com",
      "fullName": "Nguyễn Văn Test"
    }
  },
  "message": "Token hợp lệ"
}
```

### **Bước 2: Station Management**

#### **2.1. Lấy danh sách trạm xe**

```bash
GET {{baseUrl}}/api/stations
Authorization: Bearer {{authToken}}
```

**Expected Response:**

```json
[
  {
    "id": 1,
    "location": "Trạm xe số 1",
    "city": "Hà Nội",
    "address": "123 Đường ABC",
    "totalBikes": 10,
    "availableBikes": 8
  },
  {
    "id": 2,
    "location": "Trạm xe số 2",
    "city": "Hà Nội",
    "address": "456 Đường XYZ",
    "totalBikes": 15,
    "availableBikes": 12
  }
]
```

#### **2.2. Tìm kiếm trạm theo thành phố**

```bash
GET {{baseUrl}}/api/stations/search?city=Hà Nội
Authorization: Bearer {{authToken}}
```

### **Bước 3: Booking Flow**

#### **3.1. Tạo booking**

```bash
POST {{baseUrl}}/api/bookings/create
Content-Type: application/json
Authorization: Bearer {{authToken}}

{
  "stationId": 1,
  "bikeQuantity": 2,
  "notes": "Test booking"
}
```

**Expected Response:**

```json
{
  "success": true,
  "data": {
    "booking": {
      "id": 1,
      "stationId": 1,
      "bikeQuantity": 2,
      "estimatedPrice": 1000.0,
      "status": "PENDING"
    },
    "invoice": {
      "id": 1,
      "totalPrice": 1000.0,
      "bikeStatus": "NOT_PICKED_UP"
    }
  },
  "message": "Tạo booking thành công"
}
```

#### **3.2. Lấy thông tin booking**

```bash
GET {{baseUrl}}/api/bookings/{{bookingId}}
Authorization: Bearer {{authToken}}
```

#### **3.3. Lấy danh sách booking của user**

```bash
GET {{baseUrl}}/api/bookings/my-bookings
Authorization: Bearer {{authToken}}
```

### **Bước 4: Invoice Management**

#### **4.1. Lấy thông tin invoice**

```bash
GET {{baseUrl}}/api/invoices/{{invoiceId}}
Authorization: Bearer {{authToken}}
```

**Expected Response:**

```json
{
  "id": 1,
  "totalPrice": 1000.0,
  "bikeQuantity": 2,
  "bikeStatus": "NOT_PICKED_UP",
  "rentalStartTime": null,
  "rentalEndTime": null,
  "totalTime": 0,
  "station": {
    "id": 1,
    "location": "Trạm xe số 1"
  }
}
```

### **Bước 5: Payment Flow**

#### **5.1. Tạo payment**

```bash
POST {{baseUrl}}/api/payments/create
Content-Type: application/json
Authorization: Bearer {{authToken}}

{
  "invoiceId": {{invoiceId}},
  "amount": 100000,
  "paymentMethod": "VNPAY",
  "customerEmail": "test@example.com",
  "description": "Thanh toán thuê xe"
}
```

**Expected Response:**

```json
{
  "success": true,
  "data": {
    "payment": {
      "id": 1,
      "amount": 100000,
      "status": "PENDING",
      "paymentMethod": "VNPAY",
      "gatewayId": "VNPAY123456789"
    },
    "gatewayInfo": {
      "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?...",
      "qrCode": "https://sandbox.vnpayment.vn/paymentv2/qrcode.html?..."
    }
  },
  "message": "Tạo payment thành công"
}
```

#### **5.2. Test VNPay Gateway**

```bash
POST {{baseUrl}}/api/test/test-vnpay-gateway
Content-Type: application/json
Authorization: Bearer {{authToken}}

{
  "invoiceId": {{invoiceId}},
  "amount": 100000,
  "customerEmail": "test@example.com"
}
```

### **Bước 6: QR Code Flow**

#### **6.1. Lấy danh sách trạm có thể trả xe**

```bash
GET {{baseUrl}}/api/qr/available-return-stations
Authorization: Bearer {{authToken}}
```

**Expected Response:**

```json
{
  "stations": [
    {
      "id": 1,
      "location": "Trạm xe số 1",
      "city": "Hà Nội",
      "availableBikes": 8,
      "totalBikes": 10
    },
    {
      "id": 2,
      "location": "Trạm xe số 2",
      "city": "Hà Nội",
      "availableBikes": 12,
      "totalBikes": 15
    }
  ],
  "message": "Danh sách trạm có thể trả xe"
}
```

#### **6.2. Scan QR Code (Test với QR code hợp lệ)**

```bash
GET {{baseUrl}}/api/qr/scan/VALID_QR_CODE
Authorization: Bearer {{authToken}}
```

**Expected Response:**

```json
{
  "invoiceId": 1,
  "bikeQuantity": 2,
  "stationName": "Trạm xe số 1",
  "bikeStatus": "NOT_PICKED_UP",
  "totalPrice": 100000,
  "canProceed": true
}
```

#### **6.3. Xác nhận lấy xe**

```bash
POST {{baseUrl}}/api/qr/pickup/VALID_QR_CODE
Authorization: Bearer {{authToken}}
```

**Expected Response:**

```json
{
  "message": "Lấy xe thành công!",
  "invoiceId": 1,
  "pickupTime": "2024-01-15T10:30:00",
  "bikeStatus": "PICKED_UP"
}
```

#### **6.4. Xác nhận trả xe**

```bash
POST {{baseUrl}}/api/qr/return/VALID_QR_CODE?returnStationId=2
Authorization: Bearer {{authToken}}
```

**Expected Response:**

```json
{
  "message": "Trả xe thành công!",
  "invoiceId": 1,
  "returnTime": "2024-01-15T12:30:00",
  "totalTime": 2,
  "bikeStatus": "RETURNED",
  "returnStationName": "Trạm xe số 2"
}
```

## 🧪 **Test Error Handling**

### **7.1. Test đăng ký với email đã tồn tại**

```bash
POST {{baseUrl}}/api/auth/register
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "123456",
  "fullName": "Nguyễn Văn Test",
  "phone": "0123456789"
}
```

**Expected Response:**

```json
{
  "code": "BUSINESS_ERROR",
  "message": "Email đã tồn tại",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/auth/register"
}
```

### **7.2. Test đăng nhập với email không tồn tại**

```bash
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

{
  "email": "nonexistent@example.com",
  "password": "123456"
}
```

**Expected Response:**

```json
{
  "code": "BUSINESS_ERROR",
  "message": "Email hoặc mật khẩu không đúng",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/auth/login"
}
```

### **7.3. Test tạo booking với station không tồn tại**

```bash
POST {{baseUrl}}/api/bookings/create
Content-Type: application/json
Authorization: Bearer {{authToken}}

{
  "stationId": 999,
  "bikeQuantity": 2,
  "notes": "Test booking"
}
```

**Expected Response:**

```json
{
  "code": "NOT_FOUND",
  "message": "Không tìm thấy trạm xe với ID: 999",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/bookings/create"
}
```

### **7.4. Test tạo booking với số lượng xe quá lớn**

```bash
POST {{baseUrl}}/api/bookings/create
Content-Type: application/json
Authorization: Bearer {{authToken}}

{
  "stationId": 1,
  "bikeQuantity": 100,
  "notes": "Test booking"
}
```

**Expected Response:**

```json
{
  "code": "BUSINESS_ERROR",
  "message": "Không đủ xe tại trạm này. Có sẵn: 8 xe",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/bookings/create"
}
```

### **7.5. Test scan QR code không hợp lệ**

```bash
GET {{baseUrl}}/api/qr/scan/INVALID_QR_CODE
Authorization: Bearer {{authToken}}
```

**Expected Response:**

```json
{
  "code": "BUSINESS_ERROR",
  "message": "QR code không hợp lệ",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/qr/scan/INVALID_QR_CODE"
}
```

## ✅ **Test Validation**

### **8.1. Test đăng ký với email không hợp lệ**

```bash
POST {{baseUrl}}/api/auth/register
Content-Type: application/json

{
  "email": "invalid-email",
  "password": "123456",
  "fullName": "Nguyễn Văn Test",
  "phone": "0123456789"
}
```

**Expected Response:**

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Dữ liệu không hợp lệ",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/auth/register",
  "validationErrors": {
    "email": "Email không đúng định dạng"
  }
}
```

### **8.2. Test đăng ký với password quá ngắn**

```bash
POST {{baseUrl}}/api/auth/register
Content-Type: application/json

{
  "email": "test2@example.com",
  "password": "123",
  "fullName": "Nguyễn Văn Test",
  "phone": "0123456789"
}
```

**Expected Response:**

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Dữ liệu không hợp lệ",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/auth/register",
  "validationErrors": {
    "password": "Mật khẩu phải có ít nhất 6 ký tự"
  }
}
```

## 🎯 **Cách chạy test**

### **1. Chạy từng bước:**

1. Chạy **1.1. Đăng ký tài khoản**
2. Chạy **1.2. Đăng nhập** (hoặc bỏ qua nếu đã đăng ký)
3. Chạy **2.1. Lấy danh sách trạm xe**
4. Chạy **3.1. Tạo booking**
5. Chạy **4.1. Lấy thông tin invoice**
6. Chạy **5.1. Tạo payment**
7. Chạy **6.1. Lấy danh sách trạm có thể trả xe**

### **2. Chạy toàn bộ collection:**

1. Click vào collection
2. Click "Run collection"
3. Chọn các test cases muốn chạy
4. Click "Run"

### **3. Chạy theo folder:**

1. Click vào folder (ví dụ: "1. Authentication Flow")
2. Click "Run folder"
3. Chạy tất cả test cases trong folder đó

## 📊 **Kiểm tra kết quả**

### **1. Success Cases:**

- ✅ Status code: 200
- ✅ Response có `success: true`
- ✅ Variables được set tự động (authToken, bookingId, etc.)

### **2. Error Cases:**

- ✅ Status code: 400, 403, 404, 500
- ✅ Response có format chuẩn với `code`, `message`, `timestamp`, `path`
- ✅ Error message rõ ràng và hữu ích

### **3. Validation Cases:**

- ✅ Status code: 400
- ✅ Response có `validationErrors` với chi tiết lỗi
- ✅ Validation message rõ ràng cho từng field

## 🔧 **Troubleshooting**

### **1. Lỗi Connection:**

- Kiểm tra server có đang chạy không
- Kiểm tra `baseUrl` có đúng không
- Kiểm tra port có đúng không

### **2. Lỗi Authentication:**

- Kiểm tra token có được set đúng không
- Kiểm tra token có hết hạn không
- Chạy lại test đăng nhập để lấy token mới

### **3. Lỗi Data:**

- Kiểm tra database có dữ liệu test không
- Kiểm tra station ID có tồn tại không
- Kiểm tra user có được tạo thành công không

## 🎉 **Kết luận**

Sau khi chạy toàn bộ test cases, bạn sẽ có:

- ✅ **Authentication system** hoạt động chính xác
- ✅ **Booking system** tạo booking và invoice thành công
- ✅ **Payment system** tích hợp với VNPay
- ✅ **QR Code system** scan và xử lý pickup/return
- ✅ **Error handling** trả về format chuẩn
- ✅ **Validation** kiểm tra dữ liệu đầu vào
- ✅ **Exception handling** xử lý lỗi tập trung

**Hệ thống Rent Bicycles đã sẵn sàng cho production!** 🚀
