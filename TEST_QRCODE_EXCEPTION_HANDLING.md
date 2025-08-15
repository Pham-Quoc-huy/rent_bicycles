# Hướng dẫn Test Exception Handling cho QRCode

## 🎯 Tổng quan

Test các loại exception khác nhau trong QRCode Controller và Service sau khi đã cập nhật để sử dụng Exception Handling.

## 🧪 **1. Test QR Code Not Found Errors**

### **Test scan QR code không tồn tại:**

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

### **Test pickup với QR code không tồn tại:**

```bash
POST /api/qr/pickup/INVALID_QR_CODE
```

**Expected Response:**

```json
{
  "code": "BUSINESS_ERROR",
  "message": "QR code không hợp lệ",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/qr/pickup/INVALID_QR_CODE"
}
```

### **Test return với QR code không tồn tại:**

```bash
POST /api/qr/return/INVALID_QR_CODE
```

**Expected Response:**

```json
{
  "code": "BUSINESS_ERROR",
  "message": "QR code không hợp lệ",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/qr/return/INVALID_QR_CODE"
}
```

### **Test get invoice với QR code không tồn tại:**

```bash
GET /api/qr/invoice/INVALID_QR_CODE
```

**Expected Response:**

```json
{
  "code": "BUSINESS_ERROR",
  "message": "Không tìm thấy invoice cho QR code này",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/qr/invoice/INVALID_QR_CODE"
}
```

## 🧪 **2. Test Payment Required Errors**

### **Test scan QR code chưa thanh toán:**

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

### **Test pickup với QR code chưa thanh toán:**

```bash
POST /api/qr/pickup/UNPAID_QR_CODE
```

**Expected Response:**

```json
{
  "code": "BUSINESS_ERROR",
  "message": "Chưa thanh toán thành công",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/qr/pickup/UNPAID_QR_CODE"
}
```

## 🧪 **3. Test Bike Status Errors**

### **Test pickup xe đã được lấy:**

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

### **Test return xe chưa được lấy:**

```bash
POST /api/qr/return/NOT_PICKED_QR_CODE
```

**Expected Response:**

```json
{
  "code": "BUSINESS_ERROR",
  "message": "Xe chưa được lấy hoặc đã trả",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/qr/return/NOT_PICKED_QR_CODE"
}
```

## 🧪 **4. Test QR Code Creation Errors**

### **Test tạo QR code đã tồn tại:**

```bash
POST /api/qr/create
Content-Type: application/json

{
  "qrCode": "EXISTING_QR_CODE",
  "invoiceId": 1,
  "type": "PICKUP"
}
```

**Expected Response:**

```json
{
  "code": "BUSINESS_ERROR",
  "message": "QR code đã tồn tại: EXISTING_QR_CODE",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/qr/create"
}
```

### **Test tạo QR code với invoice không tồn tại:**

```bash
POST /api/qr/create
Content-Type: application/json

{
  "qrCode": "NEW_QR_CODE",
  "invoiceId": 99999,
  "type": "PICKUP"
}
```

**Expected Response:**

```json
{
  "code": "NOT_FOUND",
  "message": "Invoice không tồn tại với ID: 99999",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/qr/create"
}
```

## 🧪 **5. Test Success Cases**

### **Test scan QR code hợp lệ và đã thanh toán:**

```bash
GET /api/qr/scan/VALID_PAID_QR_CODE
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

### **Test pickup xe thành công:**

```bash
POST /api/qr/pickup/VALID_PAID_QR_CODE
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

### **Test return xe thành công:**

```bash
POST /api/qr/return/VALID_PICKED_QR_CODE
```

**Expected Response:**

```json
{
  "message": "Trả xe thành công!",
  "invoiceId": 1,
  "returnTime": "2024-01-15T12:30:00",
  "totalTime": 2,
  "bikeStatus": "RETURNED",
  "returnStationName": "Trạm xe số 1"
}
```

### **Test return xe tại trạm khác:**

```bash
POST /api/qr/return/VALID_PICKED_QR_CODE?returnStationId=2
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

### **Test lấy danh sách trạm trả xe:**

```bash
GET /api/qr/available-return-stations
```

**Expected Response:**

```json
{
  "stations": [
    {
      "id": 1,
      "location": "Trạm xe số 1",
      "city": "Hà Nội",
      "availableBikes": 5,
      "totalBikes": 10
    },
    {
      "id": 2,
      "location": "Trạm xe số 2",
      "city": "Hà Nội",
      "availableBikes": 3,
      "totalBikes": 8
    }
  ],
  "message": "Danh sách trạm có thể trả xe"
}
```

## 🧪 **6. Test Postman Collection cho QRCode**

### **Tạo file test collection:**

```json
{
  "info": {
    "name": "QRCode Exception Handling Tests",
    "description": "Test các loại exception trong QRCode"
  },
  "item": [
    {
      "name": "QR Code Not Found",
      "item": [
        {
          "name": "Scan Invalid QR Code",
          "request": {
            "method": "GET",
            "url": "{{baseUrl}}/api/qr/scan/INVALID_QR_CODE"
          }
        },
        {
          "name": "Pickup Invalid QR Code",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/api/qr/pickup/INVALID_QR_CODE"
          }
        }
      ]
    },
    {
      "name": "Payment Required",
      "item": [
        {
          "name": "Scan Unpaid QR Code",
          "request": {
            "method": "GET",
            "url": "{{baseUrl}}/api/qr/scan/UNPAID_QR_CODE"
          }
        },
        {
          "name": "Pickup Unpaid QR Code",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/api/qr/pickup/UNPAID_QR_CODE"
          }
        }
      ]
    },
    {
      "name": "Bike Status Errors",
      "item": [
        {
          "name": "Pickup Already Picked Bike",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/api/qr/pickup/ALREADY_PICKED_QR_CODE"
          }
        },
        {
          "name": "Return Not Picked Bike",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/api/qr/return/NOT_PICKED_QR_CODE"
          }
        }
      ]
    },
    {
      "name": "Success Cases",
      "item": [
        {
          "name": "Scan Valid QR Code",
          "request": {
            "method": "GET",
            "url": "{{baseUrl}}/api/qr/scan/VALID_PAID_QR_CODE"
          }
        },
        {
          "name": "Get Available Return Stations",
          "request": {
            "method": "GET",
            "url": "{{baseUrl}}/api/qr/available-return-stations"
          }
        }
      ]
    }
  ]
}
```

## 🎯 **Kết quả mong đợi**

### **✅ Success Cases:**

- QR code hợp lệ và đã thanh toán trả về thông tin đầy đủ
- Pickup xe thành công với thông tin cập nhật
- Return xe thành công với thông tin chi tiết
- Lấy danh sách trạm trả xe thành công

### **✅ Error Response Format:**

- Tất cả errors đều có format nhất quán
- Có `code`, `message`, `timestamp`, `path`
- Message rõ ràng và hữu ích cho debugging

### **✅ HTTP Status Codes:**

- `400 Bad Request` cho business logic errors
- `404 Not Found` cho not found errors
- `200 OK` cho success cases

## 🚀 **Best Practices khi Test QRCode**

### **1. Test tất cả loại exception:**

- QR code không tồn tại
- QR code chưa thanh toán
- Trạng thái xe không hợp lệ
- Invoice không tồn tại khi tạo QR code

### **2. Kiểm tra response format:**

- Code đúng loại lỗi
- Message rõ ràng và hữu ích
- Timestamp và path chính xác

### **3. Test edge cases:**

- QR code rỗng hoặc null
- QR code không đúng định dạng
- Trạng thái xe không hợp lệ
- Invoice ID không tồn tại

### **4. Test business logic:**

- Không thể pickup xe chưa thanh toán
- Không thể pickup xe đã được lấy
- Không thể return xe chưa được lấy
- Có thể return xe tại trạm khác

### **5. Test success flows:**

- Scan QR code hợp lệ
- Pickup xe thành công
- Return xe thành công
- Return xe tại trạm khác
- Lấy danh sách trạm trả xe

**QRCode Exception Handling giúp API chuyên nghiệp và dễ debug!** 🚀
