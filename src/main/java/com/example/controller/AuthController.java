package com.example.controller;

import com.example.dto.AuthResponse;
import com.example.dto.LoginRequest;
import com.example.dto.RegisterRequest;
import com.example.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    // Đăng ký
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        // Validation cơ bản
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(AuthResponse.error("Email không được để trống"));
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(AuthResponse.error("Mật khẩu không được để trống"));
        }
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(AuthResponse.error("Họ tên không được để trống"));
        }
        
        AuthResponse response = authService.register(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // Validation cơ bản
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(AuthResponse.error("Email không được để trống"));
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(AuthResponse.error("Mật khẩu không được để trống"));
        }
        
        AuthResponse response = authService.login(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Đăng nhập Google
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(@RequestBody GoogleLoginRequest request) {
        // Validation cơ bản
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(AuthResponse.error("Email không được để trống"));
        }
        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(AuthResponse.error("Họ tên không được để trống"));
        }
        if (request.getGoogleId() == null || request.getGoogleId().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(AuthResponse.error("Google ID không được để trống"));
        }
        
        AuthResponse response = authService.loginWithGoogle(
            request.getEmail(), 
            request.getFullName(), 
            request.getGoogleId(), 
            request.getAvatarUrl()
        );
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Kiểm tra token
    @GetMapping("/validate")
    public ResponseEntity<AuthResponse> validateToken(@RequestHeader("Authorization") String token) {
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(AuthResponse.error("Token không được để trống"));
        }
        if (!token.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(AuthResponse.error("Token phải bắt đầu với 'Bearer '"));
        }
        
        String actualToken = token.substring(7); // Bỏ "Bearer "
        var userOpt = authService.validateToken(actualToken);
        
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(AuthResponse.loginSuccess(actualToken, userOpt.get()));
        } else {
            return ResponseEntity.badRequest().body(AuthResponse.error("Token không hợp lệ hoặc đã hết hạn"));
        }
    }
    

    
    // Inner class cho Google login request
    public static class GoogleLoginRequest {
        private String email;
        private String fullName;
        private String googleId;
        private String avatarUrl;
        
        // Getters and Setters
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getFullName() {
            return fullName;
        }
        
        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
        
        public String getGoogleId() {
            return googleId;
        }
        
        public void setGoogleId(String googleId) {
            this.googleId = googleId;
        }
        
        public String getAvatarUrl() {
            return avatarUrl;
        }
        
        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
    }
} 