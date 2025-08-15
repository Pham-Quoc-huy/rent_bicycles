# Test Payment Success - Hướng dẫn đơn giản

## 🎯 **Cách nhanh nhất để test Payment Success**

### **Bước 1: Tạo Payment**

```bash
POST http://localhost:8080/api/payments/create
Content-Type: application/json
Authorization: Bearer {{authToken}}

{
  "invoiceId": 1,
  "amount": 40000,
  "paymentMethod": "VNPAY",
  "customerEmail": "test@example.com",
  "description": "Thanh toán thuê xe đạp"
}
```

### **Bước 2: Mark Payment as Success**

```bash
POST http://localhost:8080/api/test/test-payment-success
Content-Type: application/json
Authorization: Bearer {{authToken}}

{
  "paymentId": 1
}
```

### **Bước 3: Kiểm tra Payment Status**

```bash
GET http://localhost:8080/api/payments/1
Authorization: Bearer {{authToken}}
```

### **Bước 4: Test QR Code Scan**

```bash
GET http://localhost:8080/api/qr/scan/VALID_QR_CODE
Authorization: Bearer {{authToken}}
```

## 📊 **Expected Results**

### **Payment Success Response:**

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

### **QR Code Scan Response:**

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

## ✅ **Test Complete!**

Sau khi hoàn thành các bước trên:

- ✅ Payment status = `SUCCESS`
- ✅ Có thể scan QR code
- ✅ Có thể pickup bike
- ✅ Có thể return bike

**Payment flow đã hoạt động chính xác!** 🚀
