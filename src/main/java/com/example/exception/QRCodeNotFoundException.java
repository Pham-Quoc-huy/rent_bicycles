package com.example.exception;

/**
 * Exception khi không tìm thấy QR code
 */
public class QRCodeNotFoundException extends RuntimeException {
    public QRCodeNotFoundException(String message) {
        super(message);
    }
}



