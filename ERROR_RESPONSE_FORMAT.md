# Error Response Format

## 🎯 Tổng quan

Hệ thống sử dụng format Error Response chuẩn với `code` và `message` để trả về thông tin lỗi cho frontend.

## 📋 Cấu trúc ErrorResponse

### **ErrorResponse DTO:**

```java
public class ErrorResponse {
    private String code;           // Mã lỗi
    private String message;        // Thông báo lỗi
    private LocalDateTime timestamp; // Thời gian xảy ra lỗi
    private String path;           // Đường dẫn API
    private Map<String, String> validationErrors; // Chi tiết lỗi validation
}
```

## 🔢 Các loại Error Code

### **1. NOT_FOUND (404)**

**Sử dụng khi:** Không tìm thấy resource

```json
{
  "code": "NOT_FOUND",
  "message": "Không tìm thấy người dùng",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/users/123"
}
```

**Các exception:**

- `UserNotFoundException`
- `StationNotFoundException`
- `BookingNotFoundException`
- `InvoiceNotFoundException`
- `EntityNotFoundException`

### **2. BUSINESS_ERROR (400)**

**Sử dụng khi:** Lỗi logic nghiệp vụ

```json
{
  "code": "BUSINESS_ERROR",
  "message": "Không đủ xe tại trạm. Có sẵn: 5 xe",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/bookings/create"
}
```

**Các exception:**

- `InsufficientBikesException`
- `InvalidQRCodeException`
- `PaymentRequiredException`
- `BikeStatusException`
- `PaymentFailedException`

### **3. UNAUTHORIZED (403)**

**Sử dụng khi:** Không có quyền truy cập

```json
{
  "code": "UNAUTHORIZED",
  "message": "Bạn không có quyền truy cập tài nguyên này",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/admin/users"
}
```

**Các exception:**

- `UnauthorizedAccessException`
- `AccessDeniedException`

### **4. VALIDATION_ERROR (400)**

**Sử dụng khi:** Dữ liệu không hợp lệ

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Dữ liệu không hợp lệ",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/bookings/create",
  "validationErrors": {
    "bikeQuantity": "Số lượng xe phải lớn hơn 0",
    "stationId": "ID trạm không được để trống",
    "email": "Email không đúng định dạng"
  }
}
```

**Các exception:**

- `MethodArgumentNotValidException`

### **5. INVALID_ARGUMENT (400)**

**Sử dụng khi:** Tham số không hợp lệ

```json
{
  "code": "INVALID_ARGUMENT",
  "message": "ID không được để trống",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/stations/0"
}
```

**Các exception:**

- `IllegalArgumentException`

### **6. BAD_REQUEST (400)**

**Sử dụng khi:** Lỗi request chung

```json
{
  "code": "BAD_REQUEST",
  "message": "Dữ liệu không hợp lệ",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/bookings/create"
}
```

**Các exception:**

- `RuntimeException` (chung)

### **7. INTERNAL_SERVER_ERROR (500)**

**Sử dụng khi:** Lỗi hệ thống

```json
{
  "code": "INTERNAL_SERVER_ERROR",
  "message": "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/bookings/create"
}
```

**Các exception:**

- `Exception` (chung)

## 🎯 Ví dụ sử dụng trong Code

### **1. Throw Custom Exception:**

```java
// Trong service
if (userOpt.isEmpty()) {
    throw new UserNotFoundException("Không tìm thấy user với email: " + email);
}

if (station.getAvailableBikes() < request.getBikeQuantity()) {
    throw new InsufficientBikesException(
        "Không đủ xe tại trạm. Có sẵn: " + station.getAvailableBikes() + " xe"
    );
}
```

### **2. Response tự động được tạo:**

```json
{
  "code": "NOT_FOUND",
  "message": "Không tìm thấy user với email: user@example.com",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/users/profile"
}
```

## 📱 Frontend Integration

### **1. Xử lý Error Response:**

```javascript
// Axios interceptor
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const errorData = error.response.data;

      switch (errorData.code) {
        case "NOT_FOUND":
          showError("Không tìm thấy dữ liệu");
          break;
        case "BUSINESS_ERROR":
          showError(errorData.message);
          break;
        case "UNAUTHORIZED":
          redirectToLogin();
          break;
        case "VALIDATION_ERROR":
          showValidationErrors(errorData.validationErrors);
          break;
        default:
          showError("Đã xảy ra lỗi. Vui lòng thử lại.");
      }
    }
    return Promise.reject(error);
  }
);
```

### **2. Hiển thị Validation Errors:**

```javascript
function showValidationErrors(validationErrors) {
  Object.keys(validationErrors).forEach((field) => {
    const message = validationErrors[field];
    // Hiển thị lỗi cho từng field
    showFieldError(field, message);
  });
}
```

## 🔒 Lợi ích

### **Cho Frontend:**

- ✅ **Code rõ ràng** - Dễ dàng xử lý theo từng loại lỗi
- ✅ **Message cụ thể** - Thông báo lỗi chi tiết cho user
- ✅ **Validation errors** - Hiển thị lỗi cho từng field
- ✅ **Consistent format** - Format nhất quán

### **Cho Backend:**

- ✅ **Centralized handling** - Xử lý tập trung
- ✅ **Easy debugging** - Dễ debug với code và message
- ✅ **Flexible** - Dễ dàng thêm loại lỗi mới
- ✅ **Professional** - API chuyên nghiệp

### **Cho System:**

- ✅ **Monitoring** - Dễ dàng theo dõi lỗi theo code
- ✅ **Analytics** - Thống kê loại lỗi phổ biến
- ✅ **User experience** - Trải nghiệm người dùng tốt hơn

## 🚀 Best Practices

### **1. Code naming:**

```java
// Nên dùng
"NOT_FOUND"
"BUSINESS_ERROR"
"VALIDATION_ERROR"

// Không nên dùng
"ERROR_404"
"BIZ_ERROR"
"VAL_ERROR"
```

### **2. Message rõ ràng:**

```java
// Nên dùng
"Không tìm thấy user với email: user@example.com"

// Không nên dùng
"User not found"
"Lỗi"
```

### **3. Validation errors:**

```java
// Nên dùng
"validationErrors": {
  "email": "Email không đúng định dạng",
  "password": "Mật khẩu phải có ít nhất 6 ký tự"
}

// Không nên dùng
"errors": ["Email invalid", "Password too short"]
```

## 🎯 Kết luận

Error Response Format với `code` và `message` giúp:

- ✅ **Frontend** xử lý lỗi hiệu quả
- ✅ **Backend** quản lý exception tập trung
- ✅ **User** nhận thông báo lỗi rõ ràng
- ✅ **System** monitoring và analytics tốt hơn

**Format Error Response chuyên nghiệp và dễ sử dụng!** 🚀
