package com.example.exception;

/**
 * Exception khi không tìm thấy station
 */
public class StationNotFoundException extends RuntimeException {
    public StationNotFoundException(String message) {
        super(message);
    }
}



