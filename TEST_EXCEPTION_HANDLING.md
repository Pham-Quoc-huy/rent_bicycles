# Hướng dẫn Test Exception Handling

## 🎯 Tổng quan

Sau khi đã cập nhật code để sử dụng Exception Handling, bạn có thể test các loại lỗi khác nhau để đảm bảo hệ thống hoạt động đúng.

## 🧪 **1. Test Validation Errors**

### **Test Register với dữ liệu không hợp lệ:**

```bash
POST /api/auth/register
Content-Type: application/json

{
  "email": "invalid-email",
  "password": "123",
  "fullName": "",
  "phone": "123"
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
    "email": "Email không đúng định dạng",
    "password": "Mật khẩu phải có ít nhất 6 ký tự",
    "fullName": "Họ tên không được để trống",
    "phone": "Số điện thoại không đúng định dạng"
  }
}
```

### **Test Booking với dữ liệu không hợp lệ:**

```bash
POST /api/bookings/create
Content-Type: application/json
Authorization: Bearer <token>

{
  "stationId": null,
  "bikeQuantity": 0,
  "notes": "Test booking"
}
```

**Expected Response:**

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Dữ liệu không hợp lệ",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/bookings/create",
  "validationErrors": {
    "stationId": "ID trạm không được để trống",
    "bikeQuantity": "Số lượng xe phải lớn hơn 0"
  }
}
```

## 🧪 **2. Test Not Found Errors**

### **Test Booking với station không tồn tại:**

```bash
POST /api/bookings/create
Content-Type: application/json
Authorization: Bearer <token>

{
  "stationId": 99999,
  "bikeQuantity": 1,
  "notes": "Test booking"
}
```

**Expected Response:**

```json
{
  "code": "NOT_FOUND",
  "message": "Không tìm thấy trạm xe với ID: 99999",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/bookings/create"
}
```

### **Test QR Code không hợp lệ:**

```bash
GET /api/qr/scan/INVALID_QR_CODE
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

## 🧪 **3. Test Business Logic Errors**

### **Test Booking khi không đủ xe:**

```bash
POST /api/bookings/create
Content-Type: application/json
Authorization: Bearer <token>

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
  "message": "Không đủ xe tại trạm này. Có sẵn: 5 xe",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/bookings/create"
}
```

### **Test QR Code chưa thanh toán:**

```bash
GET /api/qr/scan/UNPAID_QR_CODE
```

**Expected Response:**

```json
{
  "code": "BUSINESS_ERROR",
  "message": "Chưa thanh toán thành công",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/qr/scan/UNPAID_QR_CODE"
}
```

## 🧪 **4. Test Authorization Errors**

### **Test Booking không có token:**

```bash
POST /api/bookings/create
Content-Type: application/json

{
  "stationId": 1,
  "bikeQuantity": 1,
  "notes": "Test booking"
}
```

**Expected Response:**

```json
{
  "code": "UNAUTHORIZED",
  "message": "Không tìm thấy thông tin người dùng",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/bookings/create"
}
```

### **Test Token không hợp lệ:**

```bash
GET /api/auth/validate
Authorization: Bearer INVALID_TOKEN
```

**Expected Response:**

```json
{
  "code": "UNAUTHORIZED",
  "message": "Token không hợp lệ hoặc đã hết hạn",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/auth/validate"
}
```

## 🧪 **5. Test Bike Status Errors**

### **Test Pickup xe đã được lấy:**

```bash
POST /api/qr/pickup/ALREADY_PICKED_QR_CODE
```

**Expected Response:**

```json
{
  "code": "BUSINESS_ERROR",
  "message": "Xe đã được lấy hoặc đã trả",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/qr/pickup/ALREADY_PICKED_QR_CODE"
}
```

### **Test Return xe chưa được lấy:**

```bash
POST /api/qr/return/NOT_PICKED_QR_CODE
```

**Expected Response:**

```json
{
  "code": "BUSINESS_ERROR",
  "message": "Xe chưa được lấy",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/qr/return/NOT_PICKED_QR_CODE"
}
```

## 🧪 **6. Test Booking Status Errors**

### **Test Cancel booking đã xác nhận:**

```bash
POST /api/bookings/123/cancel
Authorization: Bearer <token>
```

**Expected Response:**

```json
{
  "code": "BUSINESS_ERROR",
  "message": "Chỉ có thể hủy booking đang chờ xác nhận",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/bookings/123/cancel"
}
```

### **Test Complete booking chưa xác nhận:**

```bash
POST /api/bookings/123/complete
Authorization: Bearer <token>
```

**Expected Response:**

```json
{
  "code": "BUSINESS_ERROR",
  "message": "Booking phải được xác nhận trước khi hoàn thành",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/bookings/123/complete"
}
```

## 🧪 **7. Test Postman Collection**

### **Tạo file test collection:**

```json
{
  "info": {
    "name": "Exception Handling Tests",
    "description": "Test các loại exception khác nhau"
  },
  "item": [
    {
      "name": "Validation Errors",
      "item": [
        {
          "name": "Register - Invalid Email",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/api/auth/register",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"email\": \"invalid-email\",\n  \"password\": \"123\",\n  \"fullName\": \"\",\n  \"phone\": \"123\"\n}"
            }
          }
        }
      ]
    },
    {
      "name": "Not Found Errors",
      "item": [
        {
          "name": "Booking - Station Not Found",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/api/bookings/create",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              },
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"stationId\": 99999,\n  \"bikeQuantity\": 1\n}"
            }
          }
        }
      ]
    },
    {
      "name": "Business Logic Errors",
      "item": [
        {
          "name": "Booking - Insufficient Bikes",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/api/bookings/create",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              },
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"stationId\": 1,\n  \"bikeQuantity\": 100\n}"
            }
          }
        }
      ]
    }
  ]
}
```

## 🎯 **Kết quả mong đợi**

### **✅ Success Cases:**

- Validation errors trả về `VALIDATION_ERROR` với danh sách lỗi
- Not found errors trả về `NOT_FOUND` với message rõ ràng
- Business logic errors trả về `BUSINESS_ERROR` với thông tin chi tiết
- Authorization errors trả về `UNAUTHORIZED` với message phù hợp

### **✅ Error Response Format:**

- Tất cả errors đều có format nhất quán
- Có `code`, `message`, `timestamp`, `path`
- Validation errors có thêm `validationErrors`

### **✅ HTTP Status Codes:**

- `400 Bad Request` cho validation và business errors
- `404 Not Found` cho not found errors
- `403 Forbidden` cho authorization errors
- `500 Internal Server Error` cho system errors

## 🚀 **Best Practices khi Test**

### **1. Test tất cả loại exception:**

- Validation errors
- Not found errors
- Business logic errors
- Authorization errors
- System errors

### **2. Kiểm tra response format:**

- Code đúng loại lỗi
- Message rõ ràng và hữu ích
- Timestamp và path chính xác

### **3. Test edge cases:**

- Dữ liệu null/empty
- Dữ liệu không hợp lệ
- Quyền truy cập không đủ
- Trạng thái không hợp lệ

**Exception Handling giúp API chuyên nghiệp và dễ debug!** 🚀
