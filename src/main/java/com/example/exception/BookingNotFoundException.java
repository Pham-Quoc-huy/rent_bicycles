package com.example.exception;

/**
 * Exception khi không tìm thấy booking
 */
public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(String message) {
        super(message);
    }
}



