package com.example.exception;

/**
 * Exception khi không tìm thấy invoice
 */
public class InvoiceNotFoundException extends RuntimeException {
    public InvoiceNotFoundException(String message) {
        super(message);
    }
}



