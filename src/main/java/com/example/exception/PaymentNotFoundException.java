package com.example.exception;

/**
 * Exception khi không tìm thấy payment
 */
public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String message) {
        super(message);
    }
}
