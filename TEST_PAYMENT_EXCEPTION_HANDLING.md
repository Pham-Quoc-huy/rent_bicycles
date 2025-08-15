# Hướng dẫn Test Exception Handling cho Payment

## 🎯 Tổng quan

Test các loại exception khác nhau trong Payment Controller và Service sau khi đã cập nhật để sử dụng Exception Handling.

## 🧪 **1. Test Payment Not Found Errors**

### **Test lấy payment với ID không tồn tại:**

```bash
GET /api/payments/99999
```

**Expected Response:**

```json
{
  "code": "NOT_FOUND",
  "message": "Không tìm thấy payment với ID: 99999",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/payments/99999"
}
```

### **Test lấy payment với gateway ID không tồn tại:**

```bash
GET /api/payments/gateway/INVALID_GATEWAY_ID
```

**Expected Response:**

```json
{
  "code": "NOT_FOUND",
  "message": "Không tìm thấy payment với gateway ID: INVALID_GATEWAY_ID",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/payments/gateway/INVALID_GATEWAY_ID"
}
```

### **Test lấy payment với transaction ID không tồn tại:**

```bash
GET /api/payments/transaction/INVALID_TRANSACTION_ID
```

**Expected Response:**

```json
{
  "code": "NOT_FOUND",
  "message": "Không tìm thấy payment với transaction ID: INVALID_TRANSACTION_ID",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/payments/transaction/INVALID_TRANSACTION_ID"
}
```

## 🧪 **2. Test Payment Status Errors**

### **Test cancel payment đã thành công:**

```bash
POST /api/payments/123/cancel?reason=Test cancel
```

**Expected Response:**

```json
{
  "code": "BUSINESS_ERROR",
  "message": "Chỉ có thể hủy payment đang pending",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/payments/123/cancel"
}
```

### **Test refund payment chưa thành công:**

```bash
POST /api/payments/123/refund?amount=1000&reason=Test refund
```

**Expected Response:**

```json
{
  "code": "BUSINESS_ERROR",
  "message": "Chỉ có thể refund payment đã thành công",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/payments/123/refund"
}
```

### **Test refund với số tiền lớn hơn số tiền đã thanh toán:**

```bash
POST /api/payments/123/refund?amount=100000&reason=Test refund
```

**Expected Response:**

```json
{
  "code": "INVALID_ARGUMENT",
  "message": "Số tiền refund không được lớn hơn số tiền đã thanh toán",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/payments/123/refund"
}
```

## 🧪 **3. Test Invoice Not Found Errors**

### **Test tạo payment với invoice không tồn tại:**

```bash
POST /api/payments/create
Content-Type: application/json

{
  "invoiceId": 99999,
  "amount": 50000,
  "paymentMethod": "VNPAY",
  "customerEmail": "test@example.com"
}
```

**Expected Response:**

```json
{
  "code": "NOT_FOUND",
  "message": "Không tìm thấy invoice với ID: 99999",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/payments/create"
}
```

## 🧪 **4. Test Validation Errors**

### **Test tạo payment với dữ liệu không hợp lệ:**

```bash
POST /api/payments/create
Content-Type: application/json

{
  "invoiceId": null,
  "amount": 500,
  "paymentMethod": "",
  "customerEmail": "invalid-email"
}
```

**Expected Response:**

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Dữ liệu không hợp lệ",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/payments/create",
  "validationErrors": {
    "invoiceId": "Invoice ID không được để trống",
    "amount": "Số tiền tối thiểu là 1,000 VND",
    "paymentMethod": "Phương thức thanh toán không được để trống",
    "customerEmail": "Email khách hàng không được để trống"
  }
}
```

### **Test cập nhật payment status với dữ liệu không hợp lệ:**

```bash
PUT /api/payments/123/status
Content-Type: application/json

{
  "status": ""
}
```

**Expected Response:**

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Dữ liệu không hợp lệ",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/payments/123/status",
  "validationErrors": {
    "status": "Trạng thái không được để trống"
  }
}
```

## 🧪 **5. Test Invalid Argument Errors**

### **Test tạo payment với request không hợp lệ:**

```bash
POST /api/payments/create
Content-Type: application/json

{
  "invoiceId": 1,
  "amount": 50000,
  "paymentMethod": "INVALID_METHOD",
  "customerEmail": "test@example.com"
}
```

**Expected Response:**

```json
{
  "code": "INVALID_ARGUMENT",
  "message": "Payment request không hợp lệ",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/payments/create"
}
```

## 🧪 **6. Test Webhook Errors**

### **Test VNPay webhook với signature không hợp lệ:**

```bash
POST /api/payments/webhook/vnpay
Content-Type: application/json

{
  "gatewayId": "test_gateway_id",
  "status": "SUCCESS"
}
```

**Expected Response:**

```json
{
  "code": "BUSINESS_ERROR",
  "message": "VNPay webhook signature không hợp lệ",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/payments/webhook/vnpay"
}
```

### **Test MOMO webhook với signature không hợp lệ:**

```bash
POST /api/payments/webhook/momo
Content-Type: application/json

{
  "gatewayId": "test_gateway_id",
  "status": "SUCCESS"
}
```

**Expected Response:**

```json
{
  "code": "BUSINESS_ERROR",
  "message": "MOMO webhook signature không hợp lệ",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/payments/webhook/momo"
}
```

## 🧪 **7. Test Postman Collection cho Payment**

### **Tạo file test collection:**

```json
{
  "info": {
    "name": "Payment Exception Handling Tests",
    "description": "Test các loại exception trong Payment"
  },
  "item": [
    {
      "name": "Payment Not Found",
      "item": [
        {
          "name": "Get Payment - ID Not Found",
          "request": {
            "method": "GET",
            "url": "{{baseUrl}}/api/payments/99999"
          }
        },
        {
          "name": "Get Payment - Gateway ID Not Found",
          "request": {
            "method": "GET",
            "url": "{{baseUrl}}/api/payments/gateway/INVALID_GATEWAY_ID"
          }
        }
      ]
    },
    {
      "name": "Payment Status Errors",
      "item": [
        {
          "name": "Cancel Success Payment",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/api/payments/123/cancel?reason=Test cancel"
          }
        },
        {
          "name": "Refund Pending Payment",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/api/payments/123/refund?amount=1000&reason=Test refund"
          }
        }
      ]
    },
    {
      "name": "Validation Errors",
      "item": [
        {
          "name": "Create Payment - Invalid Data",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/api/payments/create",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"invoiceId\": null,\n  \"amount\": 500,\n  \"paymentMethod\": \"\",\n  \"customerEmail\": \"invalid-email\"\n}"
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

- Not found errors trả về `NOT_FOUND` với message rõ ràng
- Payment status errors trả về `BUSINESS_ERROR` với thông tin chi tiết
- Validation errors trả về `VALIDATION_ERROR` với danh sách lỗi
- Invalid argument errors trả về `INVALID_ARGUMENT` với message phù hợp

### **✅ Error Response Format:**

- Tất cả errors đều có format nhất quán
- Có `code`, `message`, `timestamp`, `path`
- Validation errors có thêm `validationErrors`
- Message rõ ràng và hữu ích cho debugging

### **✅ HTTP Status Codes:**

- `404 Not Found` cho not found errors
- `400 Bad Request` cho business logic và validation errors
- `400 Bad Request` cho invalid argument errors

## 🚀 **Best Practices khi Test Payment**

### **1. Test tất cả loại exception:**

- Not found errors (payment, invoice)
- Payment status errors (wrong status transitions)
- Validation errors (invalid data)
- Invalid argument errors (business logic validation)
- Webhook errors (signature verification)

### **2. Kiểm tra response format:**

- Code đúng loại lỗi
- Message rõ ràng và hữu ích
- Timestamp và path chính xác
- Validation errors cho từng field

### **3. Test edge cases:**

- ID không tồn tại
- Dữ liệu null/empty
- Số tiền không hợp lệ
- Trạng thái payment không hợp lệ
- Signature webhook không hợp lệ

### **4. Test business logic:**

- Không thể cancel payment đã thành công
- Không thể refund payment chưa thành công
- Số tiền refund không được vượt quá số tiền đã thanh toán
- Invoice phải tồn tại khi tạo payment

### **5. Test validation:**

- Invoice ID bắt buộc
- Amount tối thiểu 1,000 VND
- Payment method bắt buộc
- Customer email bắt buộc và đúng định dạng
- Status bắt buộc khi cập nhật

**Payment Exception Handling giúp API chuyên nghiệp và dễ debug!** 🚀
