package com.example.exception;

/**
 * Exception khi payment status không hợp lệ
 */
public class PaymentStatusException extends RuntimeException {
    public PaymentStatusException(String message) {
        super(message);
    }
}



