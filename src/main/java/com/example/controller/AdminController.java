package com.example.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    
    @GetMapping("/dashboard")
    public String adminDashboard() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return "Chào mừng Admin! Email: " + auth.getName() + ", Role: " + auth.getAuthorities();
    }
    
    @GetMapping("/users")
    public String getAllUsers() {
        return "Danh sách tất cả users (chỉ admin mới thấy)";
    }
    
    @PostMapping("/create-admin")
    public String createAdmin(@RequestBody CreateAdminRequest request) {
        // Logic tạo admin mới
        return "Admin đã được tạo: " + request.getEmail();
    }
    
    public static class CreateAdminRequest {
        private String email;
        private String password;
        private String fullName;
        private String phone;
        
        // Getters and Setters
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public String getFullName() {
            return fullName;
        }
        
        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
        
        public String getPhone() {
            return phone;
        }
        
        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
} 