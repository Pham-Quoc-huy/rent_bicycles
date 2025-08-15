package com.example.exception;

/**
 * Exception khi không đủ xe tại trạm
 */
public class InsufficientBikesException extends RuntimeException {
    public InsufficientBikesException(String message) {
        super(message);
    }
}
