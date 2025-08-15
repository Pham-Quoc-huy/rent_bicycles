package com.example.dto;

import jakarta.validation.constraints.NotBlank;

public class PaymentStatusUpdateRequest {
    
    @NotBlank(message = "Trạng thái không được để trống")
    private String status; // SUCCESS, FAILED, CANCELLED, PROCESSING
    
    private String transactionId;
    
    private String gatewayResponse;
    
    private String failureReason;
    
    private String gatewayName; // STRIPE, VNPAY, MOMO
    
    // Constructors
    public PaymentStatusUpdateRequest() {}
    
    public PaymentStatusUpdateRequest(String status) {
        this.status = status;
    }
    
    public PaymentStatusUpdateRequest(String status, String transactionId) {
        this.status = status;
        this.transactionId = transactionId;
    }
    
    // Getters and Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getGatewayResponse() {
        return gatewayResponse;
    }
    
    public void setGatewayResponse(String gatewayResponse) {
        this.gatewayResponse = gatewayResponse;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
    
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
    
    public String getGatewayName() {
        return gatewayName;
    }
    
    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }
    
    @Override
    public String toString() {
        return "PaymentStatusUpdateRequest{" +
                "status='" + status + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", gatewayResponse='" + gatewayResponse + '\'' +
                ", failureReason='" + failureReason + '\'' +
                ", gatewayName='" + gatewayName + '\'' +
                '}';
    }
}
