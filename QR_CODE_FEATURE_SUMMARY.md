# Tóm tắt tính năng QR Code - Lấy xe và trả xe

## ✅ Đã hoàn thành

### 1. **QRCodeController** - Controller chính xử lý QR code

- **File:** `src/main/java/com/example/controller/QRCodeController.java`
- **Chức năng:** Xử lý tất cả các API liên quan đến quét QR code

### 2. **Các API Endpoints đã tạo:**

#### **GET** `/api/qr/scan/{qrCode}`

- Quét QR code để lấy thông tin invoice
- Kiểm tra điều kiện thanh toán và trạng thái xe
- Trả về thông tin chi tiết để frontend hiển thị

#### **POST** `/api/qr/pickup/{qrCode}`

- Xác nhận lấy xe sau khi đã thanh toán
- Cập nhật `bikeStatus` → `PICKED_UP`
- Cập nhật `rentalStartTime`
- Giảm số lượng xe tại trạm

#### **POST** `/api/qr/return/{qrCode}`

- Xác nhận trả xe
- Tính toán thời gian sử dụng thực tế
- Tính toán giá tiền cuối cùng
- Cập nhật `bikeStatus` → `RETURNED`
- Tăng số lượng xe tại trạm

#### **GET** `/api/qr/invoice/{qrCode}`

- Lấy thông tin chi tiết invoice theo QR code

### 3. **Cập nhật InvoiceServiceImpl**

- **File:** `src/main/java/com/example/service/impl/InvoiceServiceImpl.java`
- **Thêm logic:**
  - Cập nhật số lượng xe tại trạm khi lấy xe
  - Cập nhật số lượng xe tại trạm khi trả xe
  - Kiểm tra đủ xe trước khi cho phép lấy

### 4. **QRScanResponse DTO**

- **File:** `src/main/java/com/example/dto/QRScanResponse.java`
- **Chức năng:** DTO để trả về thông tin khi quét QR code

### 5. **Tài liệu hướng dẫn**

- **File:** `QR_CODE_API_GUIDE.md`
- **Nội dung:** Hướng dẫn chi tiết sử dụng API cho frontend

### 6. **Postman Collection**

- **File:** `QR_CODE_API_TEST.postman_collection.json`
- **Chức năng:** Test collection để kiểm tra các API

## 🔄 Quy trình hoạt động

### **Quy trình lấy xe:**

1. User đặt xe → Tạo booking → Tạo invoice
2. User thanh toán → Payment status = SUCCESS
3. User đến trạm → Scan QR code → `GET /api/qr/scan/{qrCode}`
4. Frontend hiển thị thông tin → User xác nhận
5. Frontend gọi → `POST /api/qr/pickup/{qrCode}`
6. Backend cập nhật:
   - `bikeStatus`: `NOT_PICKED_UP` → `PICKED_UP`
   - `rentalStartTime`: Thời gian hiện tại
   - `availableBikes`: Giảm số lượng xe đã lấy

### **Quy trình trả xe:**

1. User đến trạm → Scan QR code → `GET /api/qr/scan/{qrCode}`
2. Frontend hiển thị thông tin → User chọn trạm trả xe
3. Frontend gọi → `GET /api/qr/available-return-stations`
4. User xác nhận trả xe → `POST /api/qr/return/{qrCode}?returnStationId={stationId}`
5. Backend cập nhật:
   - `rentalEndTime`: Thời gian hiện tại
   - `totalTime`: Thời gian sử dụng (giờ) - chỉ để thống kê
   - `bikeStatus`: `PICKED_UP` → `RETURNED`
   - `returnStation`: Trạm trả xe (có thể khác trạm lấy xe)
   - `availableBikes`: Tăng số lượng xe tại trạm trả xe

## 💰 Công thức tính giá tiền

### **Giá tiền khi đặt xe:**

- Giá mỗi xe: **500 VND**
- Công thức: `Số lượng xe × 500 VND`

### **Lưu ý:**

- **Giá tiền được tính khi đặt xe**, không thay đổi khi trả xe
- **Thời gian sử dụng chỉ để thống kê**, không ảnh hưởng đến giá tiền
- **Không có phí phạt** dựa trên thời gian sử dụng

## 🚦 Trạng thái xe (bikeStatus)

- **`NOT_PICKED_UP`**: Chưa lấy xe
- **`PICKED_UP`**: Đã lấy xe, đang sử dụng
- **`RETURNED`**: Đã trả xe

## 🔒 Bảo mật và Validation

### **Kiểm tra điều kiện:**

- ✅ QR code hợp lệ
- ✅ Đã thanh toán thành công
- ✅ Xe chưa được lấy (cho pickup)
- ✅ Xe đã được lấy (cho return)
- ✅ Đủ xe tại trạm

### **Cập nhật tự động:**

- ✅ **Số lượng xe tại trạm:**
  - **Lấy xe:** Giảm số xe tại trạm lấy xe
  - **Trả xe:** Tăng số xe tại trạm trả xe
- ✅ Trạng thái xe
- ✅ Thời gian lấy/trả xe
- ✅ Thời gian sử dụng (chỉ để thống kê)

## 📱 Frontend Integration

### **Frontend cần implement:**

1. **QR Code Scanner** (sử dụng thư viện như `react-qr-reader`)
2. **Hiển thị thông tin** sau khi scan QR
3. **Nút xác nhận** lấy xe/trả xe
4. **Error handling** và thông báo lỗi
5. **Loading states** khi gọi API

### **Flow UI:**

```
Scan QR → Hiển thị thông tin → User xác nhận → Gọi API → Hiển thị kết quả
```

## 🧪 Testing

### **Test cases đã chuẩn bị:**

1. ✅ Quét QR code hợp lệ
2. ✅ Xác nhận lấy xe thành công
3. ✅ Xác nhận trả xe thành công
4. ✅ Test QR code không hợp lệ
5. ✅ Test lấy xe chưa thanh toán
6. ✅ Test trả xe chưa lấy

### **Cách test:**

1. Import file `QR_CODE_API_TEST.postman_collection.json` vào Postman
2. Set variable `base_url = http://localhost:8080`
3. Chạy từng test case theo thứ tự

## 🎯 Kết luận

Tính năng QR Code đã được hoàn thành với đầy đủ:

- ✅ API endpoints
- ✅ Business logic
- ✅ Validation
- ✅ Error handling
- ✅ Documentation
- ✅ Test cases

**Frontend chỉ cần:**

1. Implement QR code scanner
2. Gọi các API đã tạo
3. Hiển thị UI phù hợp

**Backend đã sẵn sàng để sử dụng!** 🚀
