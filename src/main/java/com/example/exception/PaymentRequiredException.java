package com.example.exception;

/**
 * Exception khi chưa thanh toán
 */
public class PaymentRequiredException extends RuntimeException {
    public PaymentRequiredException(String message) {
        super(message);
    }
}



