# Tính năng trả xe tại trạm khác

## 🎯 Vấn đề đã giải quyết

**Vấn đề ban đầu:**

- User đặt xe tại trạm A
- User chỉ có thể trả xe tại trạm A
- Không thể trả xe tại trạm B, C, D...

**Giải pháp:**

- Cho phép user trả xe tại bất kỳ trạm nào trong hệ thống
- Tự động cập nhật số lượng xe tại trạm trả xe
- Lưu trữ thông tin trạm trả xe trong invoice

## 🔧 Thay đổi kỹ thuật

### 1. **Cập nhật Invoice Entity**

**File:** `src/main/java/com/example/entity/Invoice.java`

**Thêm trường mới:**

```java
@ManyToOne
@JoinColumn(name = "return_station_id")
private Station returnStation;  // Trạm trả xe (có thể khác trạm lấy xe)
```

**Ý nghĩa:**

- `station`: Trạm lấy xe (không đổi)
- `returnStation`: Trạm trả xe (có thể khác trạm lấy xe)

### 2. **Cập nhật InvoiceService Interface**

**File:** `src/main/java/com/example/service/InvoiceService.java`

**Thay đổi method:**

```java
// Trước
Invoice returnBike(String qrCode);

// Sau
Invoice returnBike(String qrCode, Long returnStationId);
```

### 3. **Cập nhật InvoiceServiceImpl**

**File:** `src/main/java/com/example/service/impl/InvoiceServiceImpl.java`

**Logic mới:**

```java
// Xác định trạm trả xe
Station returnStation = null;
if (returnStationId != null) {
    // Trả xe tại trạm khác
    Optional<Station> stationOpt = stationRepository.findById(returnStationId);
    if (stationOpt.isEmpty()) {
        throw new RuntimeException("Không tìm thấy trạm trả xe");
    }
    returnStation = stationOpt.get();
    invoice.setReturnStation(returnStation);
} else {
    // Trả xe tại trạm gốc (trạm lấy xe)
    invoice.setReturnStation(invoice.getStation());
    returnStation = invoice.getStation();
}

// Cập nhật số lượng xe tại trạm trả xe
int currentAvailable = returnStation.getAvailableBikes();
int returnedBikes = invoice.getBikeQuantity();
returnStation.setAvailableBikes(currentAvailable + returnedBikes);
stationRepository.save(returnStation);
```

### 4. **Cập nhật QRCodeController**

**File:** `src/main/java/com/example/controller/QRCodeController.java`

**API mới:**

```java
// Trả xe với tham số trạm
@PostMapping("/return/{qrCode}")
public ResponseEntity<?> confirmReturn(@PathVariable String qrCode,
                                     @RequestParam(required = false) Long returnStationId)

// Lấy danh sách trạm có thể trả xe
@GetMapping("/available-return-stations")
public ResponseEntity<?> getAvailableReturnStations()
```

## 📋 API Endpoints mới

### **1. Trả xe tại trạm khác**

```
POST /api/qr/return/{qrCode}?returnStationId={stationId}
```

**Parameters:**

- `returnStationId` (optional): ID của trạm muốn trả xe
- Nếu không có, sẽ trả tại trạm gốc

**Response:**

```json
{
  "message": "Trả xe thành công!",
  "invoiceId": 1,
  "returnTime": "2024-01-15T12:30:00",
  "totalTime": 2,
  "bikeStatus": "RETURNED",
  "returnStationName": "Hồ Tây"
}
```

### **2. Lấy danh sách trạm có thể trả xe**

```
GET /api/qr/available-return-stations
```

**Response:**

```json
{
  "stations": [
    {
      "id": 1,
      "location": "Hồ Hoàn Kiếm",
      "city": "Hà Nội",
      "availableBikes": 15,
      "totalBikes": 50
    },
    {
      "id": 2,
      "location": "Hồ Tây",
      "city": "Hà Nội",
      "availableBikes": 20,
      "totalBikes": 60
    }
  ],
  "message": "Danh sách trạm có thể trả xe"
}
```

## 🔄 Quy trình hoạt động mới

### **Quy trình trả xe tại trạm khác:**

1. **User scan QR code** → `GET /api/qr/scan/{qrCode}`
2. **Frontend hiển thị thông tin** và nút "Trả xe"
3. **User chọn trạm trả xe** → `GET /api/qr/available-return-stations`
4. **Frontend hiển thị danh sách trạm** cho user chọn
5. **User chọn trạm và xác nhận** → `POST /api/qr/return/{qrCode}?returnStationId={stationId}`
6. **Backend xử lý:**
   - Cập nhật `returnStation` trong invoice
   - Cập nhật thời gian sử dụng (chỉ để thống kê)
   - Tăng số lượng xe tại trạm trả xe
   - Cập nhật trạng thái xe

## 💰 Tính giá tiền

**Công thức tính giá đơn giản:**

- Giá mỗi xe: 500 VND
- Tổng giá: Số lượng xe × 500 VND

**Lưu ý:**

- Giá tiền được tính khi đặt xe, không thay đổi khi trả xe
- Thời gian sử dụng chỉ để thống kê, không ảnh hưởng đến giá tiền
- Không có phí phạt dựa trên thời gian sử dụng

## 🗄️ Database Schema

### **Bảng `invoices` mới:**

```sql
ALTER TABLE invoices ADD COLUMN return_station_id BIGINT;
ALTER TABLE invoices ADD FOREIGN KEY (return_station_id) REFERENCES stations(id);
```

### **Dữ liệu mẫu:**

```sql
-- Invoice trả xe tại trạm gốc
UPDATE invoices SET return_station_id = station_id WHERE id = 1;

-- Invoice trả xe tại trạm khác
UPDATE invoices SET return_station_id = 2 WHERE id = 2;
```

## 📱 Frontend Integration

### **UI Flow mới:**

```
Scan QR → Hiển thị thông tin → Chọn trạm trả xe → Xác nhận → Hoàn thành
```

### **Components cần thêm:**

1. **Station Selector**: Dropdown chọn trạm trả xe
2. **Station List**: Hiển thị danh sách trạm có thể trả xe
3. **Confirmation Dialog**: Xác nhận trả xe tại trạm đã chọn

### **API calls:**

```javascript
// Lấy danh sách trạm
const stations = await fetch("/api/qr/available-return-stations");

// Trả xe tại trạm đã chọn
const result = await fetch(
  `/api/qr/return/${qrCode}?returnStationId=${selectedStationId}`,
  {
    method: "POST",
  }
);
```

## 🧪 Testing

### **Test cases mới:**

1. ✅ Trả xe tại trạm gốc (không có returnStationId)
2. ✅ Trả xe tại trạm khác (có returnStationId)
3. ✅ Test trạm trả xe không tồn tại
4. ✅ Kiểm tra cập nhật số lượng xe tại trạm trả xe
5. ✅ Kiểm tra lưu trữ returnStation trong invoice

### **Postman Collection:**

- Thêm test case "3.1. Xác nhận trả xe tại trạm khác"
- Thêm test case "8. Lấy danh sách trạm có thể trả xe"

## 🎯 Lợi ích

### **Cho User:**

- ✅ Linh hoạt trong việc trả xe
- ✅ Không bị ràng buộc về địa điểm
- ✅ Tiết kiệm thời gian di chuyển

### **Cho Hệ thống:**

- ✅ Tăng trải nghiệm người dùng
- ✅ Phân bố xe đều giữa các trạm
- ✅ Giảm tải cho trạm trung tâm

### **Cho Quản lý:**

- ✅ Theo dõi được luồng xe giữa các trạm
- ✅ Thống kê trạm nào được sử dụng nhiều
- ✅ Tối ưu hóa phân bố xe

## 🚀 Kết luận

Tính năng trả xe tại trạm khác đã được hoàn thành với:

- ✅ Backend logic đầy đủ
- ✅ API endpoints mới
- ✅ Database schema cập nhật
- ✅ Documentation chi tiết
- ✅ Test cases đầy đủ

**Hệ thống giờ đây hỗ trợ đầy đủ việc trả xe tại bất kỳ trạm nào!** 🎉
