package com.example.controller;

import com.example.dto.AuthResponse;
import com.example.dto.LoginRequest;
import com.example.dto.RegisterRequest;
import com.example.service.AuthService;
import com.example.exception.*;
import jakarta.validation.Valid;
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
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Validation sẽ được xử lý bởi @Valid và GlobalExceptionHandler
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }
    
    // Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // Validation sẽ được xử lý bởi @Valid và GlobalExceptionHandler
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    // Đăng nhập Google
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(@RequestBody GoogleLoginRequest request) {
        // Validation sẽ được xử lý bởi @Valid và GlobalExceptionHandler
        AuthResponse response = authService.loginWithGoogle(
            request.getEmail(), 
            request.getFullName(), 
            request.getGoogleId(), 
            request.getAvatarUrl()
        );
        return ResponseEntity.ok(response);
    }
    
    // Kiểm tra token
    @GetMapping("/validate")
    public ResponseEntity<AuthResponse> validateToken(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token không hợp lệ");
        }
        
        String actualToken = token.substring(7); // Bỏ "Bearer "
        var userOpt = authService.validateToken(actualToken);
        
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(AuthResponse.loginSuccess(actualToken, userOpt.get()));
        } else {
            throw new UnauthorizedAccessException("Token không hợp lệ hoặc đã hết hạn");
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