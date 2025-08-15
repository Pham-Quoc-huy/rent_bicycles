package com.example.service;

import com.example.dto.AuthResponse;
import com.example.dto.LoginRequest;
import com.example.dto.RegisterRequest;
import com.example.entity.User;
import java.util.Optional;

public interface AuthService {
    
    // Đăng ký customer mới (chỉ USER role)
    AuthResponse register(RegisterRequest request);
    
    // Đăng nhập thường
    AuthResponse login(LoginRequest request);
    
    // Đăng nhập Google
    AuthResponse loginWithGoogle(String email, String fullName, String googleId, String avatarUrl);
    
    // Validate JWT token
    Optional<User> validateToken(String token);
}