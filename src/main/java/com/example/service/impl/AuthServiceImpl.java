package com.example.service.impl;

import com.example.service.AuthService;
import com.example.dto.AuthResponse;
import com.example.dto.LoginRequest;
import com.example.dto.RegisterRequest;
import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Override
    public AuthResponse register(RegisterRequest request) {
        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            return AuthResponse.error("Email đã được sử dụng");
        }
        
        // Tạo customer mới (chỉ USER role)
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone() != null ? request.getPhone() : "Chưa cập nhật");
        user.setAddress(request.getAddress());
        user.setRole(User.UserRole.USER); // Chỉ USER được đăng ký
        
        userRepository.save(user);
        
        // Tạo JWT token
        String token = jwtTokenUtil.generateToken(user.getEmail());
        
        return AuthResponse.registerSuccess(token, user);
    }
    
    @Override
    public AuthResponse login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        
        if (userOpt.isEmpty()) {
            return AuthResponse.error("Email không tồn tại");
        }
        
        User user = userOpt.get();
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return AuthResponse.error("Mật khẩu không đúng");
        }
        
        if (!user.isEnabled()) {
            return AuthResponse.error("Tài khoản đã bị khóa");
        }
        
        // Cập nhật thời gian đăng nhập
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        String token = jwtTokenUtil.generateToken(user.getEmail());
        return AuthResponse.loginSuccess(token, user);
    }
    
    @Override
    public AuthResponse loginWithGoogle(String email, String fullName, String googleId, String avatarUrl) {
        // Kiểm tra user đã tồn tại với Google ID
        Optional<User> existingUser = userRepository.findByGoogleId(googleId);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            
            String token = jwtTokenUtil.generateToken(user.getEmail());
            return AuthResponse.loginSuccess(token, user);
        }
        
        // Kiểm tra email đã tồn tại
        Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent()) {
            return AuthResponse.error("Email đã được sử dụng cho tài khoản khác");
        }
        
        // Tạo customer mới từ Google (chỉ USER role)
        User newUser = new User(email, fullName, googleId, avatarUrl);
        newUser.setRole(User.UserRole.USER); // Đảm bảo chỉ là USER
        newUser.setPhone("Chưa cập nhật"); // Set giá trị mặc định cho phone
        userRepository.save(newUser);
        
        String token = jwtTokenUtil.generateToken(newUser.getEmail());
        return AuthResponse.loginSuccess(token, newUser);
    }
    
    @Override
    public Optional<User> validateToken(String token) {
        if (token == null) {
            return Optional.empty();
        }
        
        try {
            if (jwtTokenUtil.isTokenExpired(token)) {
                return Optional.empty();
            }
            
            String email = jwtTokenUtil.extractUsername(token);
            return userRepository.findByEmail(email);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}