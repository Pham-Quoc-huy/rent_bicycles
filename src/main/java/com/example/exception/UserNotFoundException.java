package com.example.exception;

/**
 * Exception khi không tìm thấy user
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
