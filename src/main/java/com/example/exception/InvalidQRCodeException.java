package com.example.exception;

/**
 * Exception khi QR code không hợp lệ
 */
public class InvalidQRCodeException extends RuntimeException {
    public InvalidQRCodeException(String message) {
        super(message);
    }
}
