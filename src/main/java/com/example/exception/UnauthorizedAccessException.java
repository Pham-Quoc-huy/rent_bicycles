package com.example.exception;

/**
 * Exception khi không có quyền truy cập
 */
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}



