# Hướng dẫn sử dụng API QR Code

## Tổng quan

API QR Code được sử dụng để xử lý việc lấy xe và trả xe thông qua quét mã QR. Frontend sẽ scan QR code và gọi các API tương ứng.

## Các API Endpoints

### 1. Quét QR Code để lấy thông tin

**GET** `/api/qr/scan/{qrCode}`

**Mô tả:** Quét QR code để lấy thông tin invoice và kiểm tra điều kiện lấy xe

**Response thành công:**

```json
{
  "invoiceId": 1,
  "bikeQuantity": 2,
  "stationName": "Hồ Hoàn Kiếm",
  "bikeStatus": "NOT_PICKED_UP",
  "totalPrice": 1000.0,
  "canProceed": true
}
```

**Response lỗi (chưa thanh toán):**

```json
{
  "error": "Chưa thanh toán thành công",
  "invoiceId": 1,
  "canProceed": false
}
```

### 2. Xác nhận lấy xe

**POST** `/api/qr/pickup/{qrCode}`

**Mô tả:** Xác nhận lấy xe sau khi đã thanh toán thành công

**Response thành công:**

```json
{
  "message": "Lấy xe thành công!",
  "invoiceId": 1,
  "pickupTime": "2024-01-15T10:30:00",
  "bikeStatus": "PICKED_UP"
}
```

**Response lỗi:**

```json
{
  "error": "Xe đã được lấy hoặc đã trả"
}
```

### 3. Xác nhận trả xe

**POST** `/api/qr/return/{qrCode}?returnStationId={stationId}`

**Mô tả:** Xác nhận trả xe và cập nhật thời gian sử dụng. Có thể trả xe tại trạm khác.

**Parameters:**

- `returnStationId` (optional): ID của trạm muốn trả xe. Nếu không có, sẽ trả tại trạm gốc.

**Response thành công:**

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

### 4. Lấy thông tin chi tiết invoice

**GET** `/api/qr/invoice/{qrCode}`

**Mô tả:** Lấy thông tin chi tiết invoice theo QR code

### 5. Lấy danh sách trạm có thể trả xe

**GET** `/api/qr/available-return-stations`

**Mô tả:** Lấy danh sách tất cả trạm có thể trả xe

**Response thành công:**

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

## Quy trình hoạt động

### Quy trình lấy xe:

1. **Frontend scan QR code** → Gọi `GET /api/qr/scan/{qrCode}`
2. **Kiểm tra điều kiện:**
   - QR code hợp lệ
   - Đã thanh toán thành công
   - Xe chưa được lấy
3. **Hiển thị thông tin cho user** (số lượng xe, trạm, giá tiền)
4. **User xác nhận lấy xe** → Gọi `POST /api/qr/pickup/{qrCode}`
5. **Cập nhật trạng thái:**
   - `bikeStatus`: `NOT_PICKED_UP` → `PICKED_UP`
   - `rentalStartTime`: Thời gian hiện tại
   - `availableBikes` tại trạm: **Giảm** số lượng xe đã lấy

### Quy trình trả xe:

1. **Frontend scan QR code** → Gọi `GET /api/qr/scan/{qrCode}`
2. **Kiểm tra điều kiện:**
   - QR code hợp lệ
   - Xe đã được lấy (`bikeStatus = "PICKED_UP"`)
3. **User chọn trạm trả xe** → Gọi `GET /api/qr/available-return-stations`
4. **User xác nhận trả xe** → Gọi `POST /api/qr/return/{qrCode}?returnStationId={stationId}`
5. **Tính toán và cập nhật:**
   - `rentalEndTime`: Thời gian hiện tại
   - `totalTime`: Thời gian sử dụng (giờ) - chỉ để thống kê
   - `bikeStatus`: `PICKED_UP` → `RETURNED`
   - `returnStation`: Trạm trả xe (có thể khác trạm lấy xe)
   - `availableBikes`: **Tăng** số lượng xe tại trạm trả xe

## Công thức tính giá tiền

### Giá tiền khi đặt xe:

- **Giá mỗi xe:** 500 VND
- **Tổng giá =** Số lượng xe × 500 VND

### Lưu ý:

- **Giá tiền được tính khi đặt xe**, không thay đổi khi trả xe
- **Thời gian sử dụng chỉ để thống kê**, không ảnh hưởng đến giá tiền
- **Không có phí phạt** dựa trên thời gian sử dụng

## Ví dụ thực tế

### Ví dụ 1: Thuê 2 xe

```
Số lượng xe: 2
Giá tiền: 2 × 500 = 1,000 VND
Thời gian sử dụng: 2 giờ (chỉ để thống kê)
```

### Ví dụ 2: Thuê 1 xe

```
Số lượng xe: 1
Giá tiền: 1 × 500 = 500 VND
Thời gian sử dụng: 1.5 giờ (chỉ để thống kê)
```

## Trạng thái xe (bikeStatus)

- `NOT_PICKED_UP`: Chưa lấy xe
- `PICKED_UP`: Đã lấy xe, đang sử dụng
- `RETURNED`: Đã trả xe

## Lưu ý quan trọng

1. **QR code chỉ hợp lệ sau khi thanh toán thành công**
2. **Mỗi QR code chỉ có thể sử dụng một lần cho mỗi hành động**
3. **Số lượng xe tại trạm được cập nhật tự động:**
   - **Lấy xe:** Giảm số xe tại trạm lấy xe
   - **Trả xe:** Tăng số xe tại trạm trả xe
4. **Giá tiền được tính khi đặt xe, không thay đổi khi trả xe**
5. **Frontend cần xử lý các trường hợp lỗi và hiển thị thông báo phù hợp**

## Error Handling

### Các lỗi thường gặp:

- `QR code không hợp lệ`: QR code không tồn tại hoặc sai định dạng
- `Chưa thanh toán thành công`: User chưa thanh toán hoặc thanh toán thất bại
- `Xe đã được lấy hoặc đã trả`: QR code đã được sử dụng
- `Không đủ xe tại trạm để lấy`: Trạm không còn đủ xe

### Frontend cần xử lý:

- Hiển thị thông báo lỗi rõ ràng
- Disable nút xác nhận khi có lỗi
- Redirect về trang thanh toán nếu chưa thanh toán
- Refresh thông tin trạm sau khi lấy/trả xe
