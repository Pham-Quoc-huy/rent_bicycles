package com.example.exception;

/**
 * Exception khi payment thất bại
 */
public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String message) {
        super(message);
    }
}



