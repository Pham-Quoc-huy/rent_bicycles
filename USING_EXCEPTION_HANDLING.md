# Hướng dẫn sử dụng Exception Handling

## 🎯 Tổng quan

Sau khi đã setup Exception Handling, bạn cần cập nhật code để sử dụng Custom Exceptions thay vì xử lý lỗi thủ công.

## 🔄 **1. Cập nhật Controllers - Bỏ try-catch**

### **Trước:**

```java
@PostMapping("/register")
public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
    try {
        // Validation cơ bản
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(AuthResponse.error("Email không được để trống"));
        }

        AuthResponse response = authService.register(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(AuthResponse.error("Lỗi: " + e.getMessage()));
    }
}
```

### **Sau:**

```java
@PostMapping("/register")
public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
    // Validation sẽ được xử lý bởi @Valid và GlobalExceptionHandler
    AuthResponse response = authService.register(request);
    return ResponseEntity.ok(response);
}
```

## 🔄 **2. Cập nhật Services - Sử dụng Custom Exceptions**

### **Trước:**

```java
public BookingResponse createBooking(String userEmail, BookingRequest request) {
    // Tìm user
    Optional<User> userOpt = userRepository.findByEmail(userEmail);
    if (userOpt.isEmpty()) {
        return BookingResponse.error("Không tìm thấy người dùng");
    }

    // Tìm station
    Optional<Station> stationOpt = stationRepository.findById(request.getStationId());
    if (stationOpt.isEmpty()) {
        return BookingResponse.error("Không tìm thấy trạm xe");
    }

    // Kiểm tra số lượng xe
    if (station.getAvailableBikes() < request.getBikeQuantity()) {
        return BookingResponse.error("Không đủ xe tại trạm");
    }

    // Logic tạo booking...
    return BookingResponse.success(booking, "Thành công");
}
```

### **Sau:**

```java
public BookingResponse createBooking(String userEmail, BookingRequest request) {
    // Tìm user
    Optional<User> userOpt = userRepository.findByEmail(userEmail);
    if (userOpt.isEmpty()) {
        throw new UserNotFoundException("Không tìm thấy người dùng với email: " + userEmail);
    }

    // Tìm station
    Optional<Station> stationOpt = stationRepository.findById(request.getStationId());
    if (stationOpt.isEmpty()) {
        throw new StationNotFoundException("Không tìm thấy trạm xe với ID: " + request.getStationId());
    }

    // Kiểm tra số lượng xe
    if (station.getAvailableBikes() < request.getBikeQuantity()) {
        throw new InsufficientBikesException("Không đủ xe tại trạm. Có sẵn: " + station.getAvailableBikes() + " xe");
    }

    // Logic tạo booking...
    return BookingResponse.success(booking, "Thành công");
}
```

## 🔄 **3. Thêm Validation Annotations**

### **Cập nhật DTOs với @Valid:**

```java
@PostMapping("/register")
public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    AuthResponse response = authService.register(request);
    return ResponseEntity.ok(response);
}
```

### **Thêm validation annotations vào DTO:**

```java
public class RegisterRequest {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không đúng định dạng")
    private String phone;
}
```

## 🔄 **4. Import Custom Exceptions**

### **Thêm import vào các file:**

```java
import com.example.exception.*;
```

### **Hoặc import cụ thể:**

```java
import com.example.exception.UserNotFoundException;
import com.example.exception.StationNotFoundException;
import com.example.exception.InsufficientBikesException;
import com.example.exception.InvalidQRCodeException;
import com.example.exception.PaymentRequiredException;
import com.example.exception.BikeStatusException;
import com.example.exception.UnauthorizedAccessException;
```

## 🔄 **5. Các loại Exception cần sử dụng**

### **Validation Errors:**

```java
// Thay vì return error response
if (request.getEmail() == null) {
    return ResponseEntity.badRequest().body("Email không được để trống");
}

// Sử dụng @Valid annotation
@Valid @RequestBody RegisterRequest request
```

### **Not Found Errors:**

```java
// Thay vì return error response
if (userOpt.isEmpty()) {
    return ResponseEntity.badRequest().body("Không tìm thấy user");
}

// Throw Custom Exception
if (userOpt.isEmpty()) {
    throw new UserNotFoundException("Không tìm thấy user với email: " + email);
}
```

### **Business Logic Errors:**

```java
// Thay vì return error response
if (station.getAvailableBikes() < request.getBikeQuantity()) {
    return ResponseEntity.badRequest().body("Không đủ xe");
}

// Throw Custom Exception
if (station.getAvailableBikes() < request.getBikeQuantity()) {
    throw new InsufficientBikesException("Không đủ xe. Có sẵn: " + station.getAvailableBikes() + " xe");
}
```

### **Authorization Errors:**

```java
// Thay vì return error response
if (!user.hasRole("ADMIN")) {
    return ResponseEntity.forbidden().body("Không có quyền truy cập");
}

// Throw Custom Exception
if (!user.hasRole("ADMIN")) {
    throw new UnauthorizedAccessException("Bạn không có quyền truy cập tài nguyên này");
}
```

## 🔄 **6. Response Format tự động**

### **Khi throw exception:**

```java
throw new UserNotFoundException("Không tìm thấy user với email: user@example.com");
```

### **Response tự động được tạo:**

```json
{
  "code": "NOT_FOUND",
  "message": "Không tìm thấy user với email: user@example.com",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/users/profile"
}
```

## 🔄 **7. Validation Errors tự động**

### **Khi có validation error:**

```java
@NotBlank(message = "Email không được để trống")
private String email;
```

### **Response tự động:**

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Dữ liệu không hợp lệ",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/auth/register",
  "validationErrors": {
    "email": "Email không được để trống",
    "password": "Mật khẩu phải có ít nhất 6 ký tự"
  }
}
```

## 🎯 **Lợi ích sau khi cập nhật**

### **Cho Developer:**

- ✅ **Code sạch hơn** - Không cần try-catch ở mọi nơi
- ✅ **Logic rõ ràng** - Tập trung vào business logic
- ✅ **Dễ maintain** - Xử lý lỗi tập trung
- ✅ **Consistent** - Format response nhất quán

### **Cho Frontend:**

- ✅ **Error handling dễ dàng** - Chỉ cần xử lý theo code
- ✅ **User experience tốt** - Thông báo lỗi rõ ràng
- ✅ **Validation errors** - Hiển thị lỗi cho từng field

### **Cho System:**

- ✅ **Monitoring hiệu quả** - Theo dõi lỗi theo code
- ✅ **Debug nhanh** - Thông tin lỗi chi tiết
- ✅ **Professional API** - Chuẩn RESTful

## 🚀 **Best Practices**

### **1. Message rõ ràng:**

```java
// Nên dùng
throw new UserNotFoundException("Không tìm thấy user với email: " + email);

// Không nên dùng
throw new UserNotFoundException("User not found");
```

### **2. Validation với @Valid:**

```java
// Nên dùng
@Valid @RequestBody RegisterRequest request

// Không nên dùng
// Validation thủ công trong controller
```

### **3. Specific Exceptions:**

```java
// Nên dùng
throw new InsufficientBikesException("Không đủ xe");

// Không nên dùng
throw new RuntimeException("Không đủ xe");
```

## 🎯 **Kết luận**

Sau khi cập nhật code để sử dụng Exception Handling:

- ✅ **Controllers** sạch hơn, không cần try-catch
- ✅ **Services** tập trung vào business logic
- ✅ **Validation** tự động với @Valid
- ✅ **Response format** nhất quán
- ✅ **Error handling** chuyên nghiệp

**Exception Handling giúp code chuyên nghiệp và dễ maintain!** 🚀
