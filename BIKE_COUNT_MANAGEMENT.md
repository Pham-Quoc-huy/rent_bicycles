# Quản lý số lượng xe tại trạm

## 🎯 Tổng quan

Hệ thống tự động cập nhật số lượng xe tại các trạm khi user lấy xe và trả xe để đảm bảo tính chính xác của dữ liệu.

## 🔄 Quy trình cập nhật số xe

### **1. Khi đặt xe (Booking)**

- **Không cập nhật** số xe tại trạm
- Chỉ **kiểm tra** số xe có đủ không
- Số xe chỉ được **đặt chỗ** (reserved), chưa thực sự lấy

### **2. Khi lấy xe (Pickup)**

- **Giảm** số xe tại trạm lấy xe
- Cập nhật `availableBikes = availableBikes - bikeQuantity`

### **3. Khi trả xe (Return)**

- **Tăng** số xe tại trạm trả xe
- Cập nhật `availableBikes = availableBikes + bikeQuantity`

## 📋 Logic chi tiết

### **Lấy xe (pickupBike method):**

```java
// Cập nhật số lượng xe tại trạm
Station station = invoice.getStation();
int currentAvailable = station.getAvailableBikes();
int requestedBikes = invoice.getBikeQuantity();

// Kiểm tra đủ xe không
if (currentAvailable < requestedBikes) {
    throw new RuntimeException("Không đủ xe tại trạm để lấy");
}

// Giảm số xe tại trạm
station.setAvailableBikes(currentAvailable - requestedBikes);
stationRepository.save(station);
```

### **Trả xe (returnBike method):**

```java
// Xác định trạm trả xe
Station returnStation = null;
if (returnStationId != null) {
    // Trả xe tại trạm khác
    returnStation = stationRepository.findById(returnStationId).get();
    invoice.setReturnStation(returnStation);
} else {
    // Trả xe tại trạm gốc
    returnStation = invoice.getStation();
    invoice.setReturnStation(returnStation);
}

// Tăng số xe tại trạm trả xe
int currentAvailable = returnStation.getAvailableBikes();
int returnedBikes = invoice.getBikeQuantity();

returnStation.setAvailableBikes(currentAvailable + returnedBikes);
stationRepository.save(returnStation);
```

## 🎯 Ví dụ thực tế

### **Trạm Hồ Hoàn Kiếm:**

- **Ban đầu:** `totalBikes = 50`, `availableBikes = 30`

### **User A đặt 2 xe:**

- **Sau đặt xe:** `availableBikes = 30` (không đổi)
- **Sau lấy xe:** `availableBikes = 30 - 2 = 28`

### **User A trả xe tại trạm Hồ Tây:**

- **Trạm Hồ Hoàn Kiếm:** `availableBikes = 28` (không đổi)
- **Trạm Hồ Tây:** `availableBikes = availableBikes + 2`

### **User B đặt 1 xe tại Hồ Hoàn Kiếm:**

- **Sau đặt xe:** `availableBikes = 28` (không đổi)
- **Sau lấy xe:** `availableBikes = 28 - 1 = 27`

## 🔒 Bảo mật và Validation

### **Kiểm tra khi lấy xe:**

- ✅ Đủ số xe tại trạm
- ✅ Xe chưa được lấy (`bikeStatus = "NOT_PICKED_UP"`)
- ✅ Đã thanh toán thành công

### **Kiểm tra khi trả xe:**

- ✅ Xe đã được lấy (`bikeStatus = "PICKED_UP"`)
- ✅ Trạm trả xe tồn tại (nếu trả tại trạm khác)

### **Cập nhật tự động:**

- ✅ Số xe tại trạm lấy xe: **Giảm**
- ✅ Số xe tại trạm trả xe: **Tăng**
- ✅ Trạng thái xe: `NOT_PICKED_UP` → `PICKED_UP` → `RETURNED`

## 📊 Theo dõi luồng xe

### **Invoice Entity lưu trữ:**

- `station`: Trạm lấy xe
- `returnStation`: Trạm trả xe (có thể khác trạm lấy xe)
- `bikeQuantity`: Số lượng xe
- `bikeStatus`: Trạng thái xe

### **Thống kê có thể thực hiện:**

- Số xe đang được thuê tại mỗi trạm
- Luồng xe giữa các trạm
- Trạm nào được sử dụng nhiều nhất
- Tối ưu hóa phân bố xe

## 🚨 Xử lý lỗi

### **Lỗi khi lấy xe:**

```java
if (currentAvailable < requestedBikes) {
    throw new RuntimeException("Không đủ xe tại trạm để lấy");
}
```

### **Lỗi khi trả xe:**

```java
if (returnStationId != null) {
    Optional<Station> stationOpt = stationRepository.findById(returnStationId);
    if (stationOpt.isEmpty()) {
        throw new RuntimeException("Không tìm thấy trạm trả xe");
    }
}
```

## 🎯 Lợi ích

### **Cho User:**

- ✅ Biết chính xác số xe có sẵn tại trạm
- ✅ Không bị "hết xe" khi đến trạm
- ✅ Có thể trả xe tại trạm khác

### **Cho Hệ thống:**

- ✅ Dữ liệu số xe chính xác
- ✅ Phân bố xe hợp lý
- ✅ Theo dõi luồng xe giữa các trạm

### **Cho Quản lý:**

- ✅ Thống kê sử dụng trạm
- ✅ Tối ưu hóa phân bố xe
- ✅ Dự báo nhu cầu

## 🚀 Kết luận

Hệ thống đã có đầy đủ logic để:

- ✅ **Giảm** số xe khi lấy xe
- ✅ **Tăng** số xe khi trả xe
- ✅ **Kiểm tra** đủ xe trước khi lấy
- ✅ **Hỗ trợ** trả xe tại trạm khác
- ✅ **Lưu trữ** thông tin trạm lấy/trả xe

**Quản lý số lượng xe hoạt động chính xác và tự động!** 🎉
