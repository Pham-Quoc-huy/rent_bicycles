package com.example.dto;

import com.example.entity.User;

public class AuthResponse {
    private String token;
    private String message;
    private User user;
    private boolean success;
    
    // Constructors
    public AuthResponse() {}
    
    public AuthResponse(String token, String message, User user, boolean success) {
        this.token = token;
        this.message = message;
        this.user = user;
        this.success = success;
    }
    
    // Success response for login
    public static AuthResponse loginSuccess(String token, User user) {
        return new AuthResponse(token, "Đăng nhập thành công", user, true);
    }
    
    // Success response for register
    public static AuthResponse registerSuccess(String token, User user) {
        return new AuthResponse(token, "Đăng ký thành công", user, true);
    }
    
    // Error response
    public static AuthResponse error(String message) {
        return new AuthResponse(null, message, null, false);
    }
    
    // Getters and Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
} 