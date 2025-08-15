# Exception Handling trong hệ thống Rent Bicycles

## 🎯 Mục đích của @ExceptionHandler

### **1. Xử lý Exception tập trung**

- Thay vì xử lý exception trong từng controller riêng lẻ
- Tạo một nơi duy nhất để xử lý tất cả các loại exception
- Đảm bảo response format nhất quán

### **2. Tùy chỉnh Response**

- Trả về JSON response thay vì error page
- Có thể thêm thông tin chi tiết về lỗi
- Format response theo chuẩn API

### **3. Logging và Monitoring**

- Ghi log tất cả exception tại một nơi
- Dễ dàng theo dõi và debug
- Có thể tích hợp với monitoring tools

## 📋 Cấu trúc Exception Handling

### **1. GlobalExceptionHandler**

**File:** `src/main/java/com/example/exception/GlobalExceptionHandler.java`

**Annotation:**

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    // Các method xử lý exception
}
```

### **2. Custom Exceptions**

**File:** `src/main/java/com/example/exception/CustomExceptions.java`

**Các loại exception:**

- `UserNotFoundException`
- `StationNotFoundException`
- `BookingNotFoundException`
- `InvoiceNotFoundException`
- `InsufficientBikesException`
- `InvalidQRCodeException`
- `PaymentRequiredException`
- `BikeStatusException`
- `PaymentFailedException`
- `UnauthorizedAccessException`

## 🔄 Cách hoạt động

### **1. Khi có Exception xảy ra:**

```java
// Trong service
if (userOpt.isEmpty()) {
    throw new UserNotFoundException("Không tìm thấy người dùng");
}
```

### **2. GlobalExceptionHandler bắt exception:**

```java
@ExceptionHandler(UserNotFoundException.class)
public ResponseEntity<?> handleNotFoundException(RuntimeException ex, WebRequest request) {
    // Xử lý exception và trả về response
}
```

### **3. Response trả về client:**

```json
{
  "code": "NOT_FOUND",
  "message": "Không tìm thấy người dùng",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/users/123"
}
```

## 📊 Các loại Exception được xử lý

### **1. Not Found Exceptions (404)**

```java
@ExceptionHandler({
    UserNotFoundException.class,
    StationNotFoundException.class,
    BookingNotFoundException.class,
    InvoiceNotFoundException.class
})
```

**Ví dụ:**

- Không tìm thấy user
- Không tìm thấy station
- Không tìm thấy booking
- Không tìm thấy invoice

### **2. Business Logic Exceptions (400)**

```java
@ExceptionHandler({
    InsufficientBikesException.class,
    InvalidQRCodeException.class,
    PaymentRequiredException.class,
    BikeStatusException.class,
    PaymentFailedException.class
})
```

**Ví dụ:**

- Không đủ xe tại trạm
- QR code không hợp lệ
- Chưa thanh toán
- Xe đã được lấy/trả
- Payment thất bại

### **3. Unauthorized Access (403)**

```java
@ExceptionHandler(UnauthorizedAccessException.class)
```

**Ví dụ:**

- User không có quyền truy cập
- Không có quyền admin

### **4. Validation Errors (400)**

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
```

**Ví dụ:**

- Dữ liệu không hợp lệ
- Thiếu thông tin bắt buộc

### **5. Internal Server Error (500)**

```java
@ExceptionHandler(Exception.class)
```

**Ví dụ:**

- Lỗi database
- Lỗi hệ thống không xác định

## 🎯 Ví dụ sử dụng trong Code

### **Trước khi có ExceptionHandler:**

```java
@PostMapping("/bookings")
public ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {
    try {
        // Logic tạo booking
        return ResponseEntity.ok(result);
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
    }
}
```

### **Sau khi có ExceptionHandler:**

```java
@PostMapping("/bookings")
public ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {
    // Logic tạo booking - không cần try-catch
    if (userOpt.isEmpty()) {
        throw new UserNotFoundException("Không tìm thấy người dùng");
    }
    if (stationOpt.isEmpty()) {
        throw new StationNotFoundException("Không tìm thấy trạm xe");
    }
    if (station.getAvailableBikes() < request.getBikeQuantity()) {
        throw new InsufficientBikesException("Không đủ xe tại trạm");
    }

    return ResponseEntity.ok(result);
}
```

## 📋 Response Format chuẩn

### **Success Response:**

```json
{
  "success": true,
  "data": { ... },
  "message": "Thành công"
}
```

### **Error Response:**

```json
{
  "code": "BUSINESS_ERROR",
  "message": "Không đủ xe tại trạm",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/bookings/create"
}
```

### **Validation Error Response:**

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Dữ liệu không hợp lệ",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/bookings/create",
  "validationErrors": {
    "bikeQuantity": "Số lượng xe phải lớn hơn 0",
    "stationId": "ID trạm không được để trống"
  }
}
```

## 🔒 Lợi ích

### **Cho Developer:**

- ✅ Code sạch hơn, không cần try-catch ở mọi nơi
- ✅ Xử lý exception tập trung
- ✅ Dễ dàng thêm/sửa logic xử lý lỗi
- ✅ Response format nhất quán

### **Cho Frontend:**

- ✅ Response format chuẩn
- ✅ Thông tin lỗi rõ ràng
- ✅ Dễ dàng xử lý error handling
- ✅ HTTP status code chính xác

### **Cho System:**

- ✅ Logging tập trung
- ✅ Monitoring dễ dàng
- ✅ Debug nhanh chóng
- ✅ Bảo mật thông tin

## 🚀 Best Practices

### **1. Sử dụng Custom Exceptions:**

```java
// Thay vì
throw new RuntimeException("Không tìm thấy user");

// Nên dùng
throw new UserNotFoundException("Không tìm thấy user với email: " + email);
```

### **2. Message rõ ràng:**

```java
// Không nên
throw new InsufficientBikesException("Lỗi");

// Nên dùng
throw new InsufficientBikesException("Không đủ xe tại trạm. Có sẵn: " + available + " xe");
```

### **3. Logging phù hợp:**

```java
// Log lỗi hệ thống
System.err.println("Lỗi hệ thống: " + ex.getMessage());
ex.printStackTrace();

// Không log thông tin nhạy cảm
```

## 🎯 Kết luận

Exception Handling giúp:

- ✅ **Code sạch** và dễ maintain
- ✅ **Response nhất quán** cho frontend
- ✅ **Debug nhanh** khi có lỗi
- ✅ **Bảo mật** thông tin hệ thống
- ✅ **Monitoring** hiệu quả

**Hệ thống có Exception Handling chuyên nghiệp và đầy đủ!** 🚀
