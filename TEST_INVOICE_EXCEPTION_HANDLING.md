# Hướng dẫn Test Exception Handling cho Invoice

## 🎯 Tổng quan

Test các loại exception khác nhau trong Invoice Controller và Service sau khi đã cập nhật để sử dụng Exception Handling.

## 🧪 **1. Test Invoice Not Found Errors**

### **Test tạo invoice với booking không tồn tại:**

```bash
POST /api/invoices/create-from-booking/99999
```

**Expected Response:**

```json
{
  "code": "NOT_FOUND",
  "message": "Không tìm thấy booking với ID: 99999",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/invoices/create-from-booking/99999"
}
```

### **Test lấy invoice theo ID không tồn tại:**

```bash
GET /api/invoices/99999
```

**Expected Response:**

```json
{
  "code": "NOT_FOUND",
  "message": "Không tìm thấy hóa đơn với ID: 99999",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/invoices/99999"
}
```

### **Test lấy invoice theo booking ID không tồn tại:**

```bash
GET /api/invoices/booking/99999
```

**Expected Response:**

```json
{
  "code": "NOT_FOUND",
  "message": "Không tìm thấy hóa đơn cho booking ID: 99999",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/invoices/booking/99999"
}
```

## 🧪 **2. Test QR Code Errors**

### **Test check pickup với QR code không hợp lệ:**

```bash
GET /api/invoices/check-pickup?qrCode=INVALID_QR
```

**Expected Response:**

```json
{
  "code": "BUSINESS_ERROR",
  "message": "QR code không hợp lệ",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/invoices/check-pickup"
}
```

### **Test pickup bike với QR code không hợp lệ:**

```bash
POST /api/invoices/pickup-by-qr?qrCode=INVALID_QR
```

**Expected Response:**

```json
{
  "code": "BUSINESS_ERROR",
  "message": "QR code không hợp lệ",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/invoices/pickup-by-qr"
}
```

### **Test return bike với QR code không hợp lệ:**

```bash
POST /api/invoices/return-by-qr?qrCode=INVALID_QR
```

**Expected Response:**

```json
{
  "code": "BUSINESS_ERROR",
  "message": "QR code không hợp lệ",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/invoices/return-by-qr"
}
```

## 🧪 **3. Test Bike Status Errors**

### **Test pickup bike đã được lấy:**

```bash
POST /api/invoices/pickup-by-qr?qrCode=ALREADY_PICKED_QR
```

**Expected Response:**

```json
{
  "code": "BUSINESS_ERROR",
  "message": "Xe đã được lấy hoặc đã trả",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/invoices/pickup-by-qr"
}
```

### **Test return bike chưa được lấy:**

```bash
POST /api/invoices/return-by-qr?qrCode=NOT_PICKED_QR
```

**Expected Response:**

```json
{
  "code": "BUSINESS_ERROR",
  "message": "Xe chưa được lấy",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/invoices/return-by-qr"
}
```

### **Test mark return với xe chưa được lấy:**

```bash
POST /api/invoices/123/mark-return
```

**Expected Response:**

```json
{
  "code": "BUSINESS_ERROR",
  "message": "Xe chưa được lấy. Không thể đánh dấu trả.",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/invoices/123/mark-return"
}
```

## 🧪 **4. Test Insufficient Bikes Errors**

### **Test pickup bike khi không đủ xe tại trạm:**

```bash
POST /api/invoices/pickup-by-qr?qrCode=INSUFFICIENT_BIKES_QR
```

**Expected Response:**

```json
{
  "code": "BUSINESS_ERROR",
  "message": "Không đủ xe tại trạm để lấy. Có sẵn: 2 xe",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/invoices/pickup-by-qr"
}
```

## 🧪 **5. Test Station Not Found Errors**

### **Test return bike tại trạm không tồn tại:**

```bash
POST /api/invoices/return-by-qr?qrCode=VALID_QR&returnStationId=99999
```

**Expected Response:**

```json
{
  "code": "NOT_FOUND",
  "message": "Không tìm thấy trạm trả xe với ID: 99999",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/invoices/return-by-qr"
}
```

## 🧪 **6. Test Authorization Errors**

### **Test my-invoices không có token:**

```bash
GET /api/invoices/my-invoices
```

**Expected Response:**

```json
{
  "code": "UNAUTHORIZED",
  "message": "Không tìm thấy thông tin người dùng",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/invoices/my-invoices"
}
```

## 🧪 **7. Test Postman Collection cho Invoice**

### **Tạo file test collection:**

```json
{
  "info": {
    "name": "Invoice Exception Handling Tests",
    "description": "Test các loại exception trong Invoice"
  },
  "item": [
    {
      "name": "Invoice Not Found",
      "item": [
        {
          "name": "Create Invoice - Booking Not Found",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/api/invoices/create-from-booking/99999"
          }
        },
        {
          "name": "Get Invoice - ID Not Found",
          "request": {
            "method": "GET",
            "url": "{{baseUrl}}/api/invoices/99999"
          }
        }
      ]
    },
    {
      "name": "QR Code Errors",
      "item": [
        {
          "name": "Check Pickup - Invalid QR",
          "request": {
            "method": "GET",
            "url": "{{baseUrl}}/api/invoices/check-pickup?qrCode=INVALID_QR"
          }
        },
        {
          "name": "Pickup Bike - Invalid QR",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/api/invoices/pickup-by-qr?qrCode=INVALID_QR"
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
            "url": "{{baseUrl}}/api/invoices/pickup-by-qr?qrCode=ALREADY_PICKED_QR"
          }
        },
        {
          "name": "Return Not Picked Bike",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/api/invoices/return-by-qr?qrCode=NOT_PICKED_QR"
          }
        }
      ]
    }
  ]
}
```

## 🎯 **Kết quả mong đợi**

### **✅ Success Cases:**

- Not found errors trả về `NOT_FOUND` với message rõ ràng
- QR code errors trả về `BUSINESS_ERROR` với thông tin chi tiết
- Bike status errors trả về `BUSINESS_ERROR` với message phù hợp
- Authorization errors trả về `UNAUTHORIZED` với message phù hợp

### **✅ Error Response Format:**

- Tất cả errors đều có format nhất quán
- Có `code`, `message`, `timestamp`, `path`
- Message rõ ràng và hữu ích cho debugging

### **✅ HTTP Status Codes:**

- `404 Not Found` cho not found errors
- `400 Bad Request` cho business logic errors
- `403 Forbidden` cho authorization errors

## 🚀 **Best Practices khi Test Invoice**

### **1. Test tất cả loại exception:**

- Not found errors (booking, invoice, station)
- QR code errors (invalid, expired)
- Bike status errors (wrong status transitions)
- Authorization errors (no token, invalid token)

### **2. Kiểm tra response format:**

- Code đúng loại lỗi
- Message rõ ràng và hữu ích
- Timestamp và path chính xác

### **3. Test edge cases:**

- ID không tồn tại
- QR code không hợp lệ
- Trạng thái xe không hợp lệ
- Quyền truy cập không đủ

### **4. Test business logic:**

- Không thể pickup xe đã được lấy
- Không thể return xe chưa được lấy
- Không đủ xe tại trạm
- Trạm trả xe không tồn tại

**Invoice Exception Handling giúp API chuyên nghiệp và dễ debug!** 🚀
