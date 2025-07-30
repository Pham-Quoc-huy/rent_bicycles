# Hệ thống Thuê Xe Đạp - Spring Boot

Đây là một hệ thống quản lý thuê xe đạp với chức năng đăng nhập/đăng ký, hỗ trợ đăng nhập Google và phân quyền admin/user.

## Cấu trúc Project

```
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           ├── SimpleApp.java
│   │   │           ├── entity/
│   │   │           │   └── User.java
│   │   │           ├── dto/
│   │   │           │   ├── LoginRequest.java
│   │   │           │   ├── RegisterRequest.java
│   │   │           │   └── AuthResponse.java
│   │   │           ├── repository/
│   │   │           │   └── UserRepository.java
│   │   │           ├── service/
│   │   │           │   └── AuthService.java
│   │   │           ├── controller/
│   │   │           │   └── AuthController.java
│   │   │           └── config/
│   │   │               ├── SecurityConfig.java
│   │   │               └── DataInitializer.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
└── README.md
```

## Cài đặt Extension trong VS Code

Trước khi bắt đầu, hãy cài đặt các extension sau:

1. **Extension Pack for Java** (Microsoft)
2. **Spring Boot Extension Pack** (Pivotal)
3. **Maven for Java** (Microsoft)

## Cách chạy project

### Phương pháp 1: Sử dụng VS Code

1. Mở project trong VS Code
2. Nhấn `Ctrl+Shift+P` (hoặc `Cmd+Shift+P` trên Mac)
3. Gõ "Spring Boot Dashboard" và chọn
4. Nhấn nút "Play" bên cạnh tên project

### Phương pháp 2: Sử dụng Terminal trong VS Code

1. Mở Terminal trong VS Code (` Ctrl+``  `)
2. Chạy lệnh:
   ```bash
   mvn spring-boot:run
   ```

### Phương pháp 3: Sử dụng Maven

```bash
mvn clean install
mvn spring-boot:run
```

## API Endpoints

Sau khi chạy thành công, ứng dụng sẽ chạy tại `http://localhost:8080`

### Authentication Endpoints:

- `POST /api/auth/register` - Đăng ký tài khoản mới
- `POST /api/auth/login` - Đăng nhập thường
- `POST /api/auth/google` - Đăng nhập bằng Google
- `GET /api/auth/validate` - Kiểm tra token

### Test Endpoints:

- `GET /` - Trang chủ
- `GET /hello` - Hello World
- `GET /info` - Thông tin ứng dụng

## Ví dụ sử dụng API

### Đăng ký tài khoản mới:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "fullName": "Nguyễn Văn A",
    "phone": "0123456789",
    "address": "Hà Nội"
  }'
```

### Đăng nhập:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

### Đăng nhập Google:

```bash
curl -X POST http://localhost:8080/api/auth/google \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@gmail.com",
    "fullName": "Nguyễn Văn B",
    "googleId": "google123456",
    "avatarUrl": "https://example.com/avatar.jpg"
  }'
```

### Kiểm tra token:

```bash
curl -X GET http://localhost:8080/api/auth/validate \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

## Tài khoản Admin mặc định

Khi ứng dụng khởi động, hệ thống sẽ tự động tạo tài khoản admin trong database:

- **Email:** admin@rentbicycles.com
- **Password:** admin123
- **Role:** ADMIN

**Lưu ý:** Admin chỉ có thể đăng nhập, không thể đăng ký mới.

## Tính năng

- ✅ **Spring Boot 3.2.0** với Spring Security
- ✅ **Đăng ký/Đăng nhập** thông thường
- ✅ **Đăng nhập Google** OAuth
- ✅ **Phân quyền** ADMIN/USER
- ✅ **JWT Token** (thật sự)
- ✅ **H2 Database** in-memory
- ✅ **BCrypt** password encryption
- ✅ **CORS** enabled
- ✅ **H2 Console** tại `/h2-console`

## Database

- **URL:** `http://localhost:8080/h2-console`
- **JDBC URL:** `jdbc:h2:mem:testdb`
- **Username:** `sa`
- **Password:** `password`

## Lưu ý

- Admin được tạo sẵn trong database, chỉ có thể đăng nhập
- Customer (USER) có thể đăng ký và đăng nhập
- JWT token có thời hạn 24 giờ
- Database sẽ được tạo lại mỗi khi restart (H2 in-memory)
- Để sử dụng Google OAuth thật, cần cấu hình Google OAuth credentials
