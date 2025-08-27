package com.example.exception;

/**
 * Exception khi xe đã được lấy hoặc trả
 */
public class BikeStatusException extends RuntimeException {
    public BikeStatusException(String message) {
        super(message);
    }
}



