package com.example.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class PaymentRequest {
    
    @NotNull(message = "Invoice ID không được để trống")
    private Long invoiceId;
    
    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "1000", message = "Số tiền tối thiểu là 1,000 VND")
    private BigDecimal amount;
    
    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod; // STRIPE, VNPAY, MOMO, BANK_TRANSFER
    
    @NotBlank(message = "Email khách hàng không được để trống")
    private String customerEmail;
    
    private String customerPhone;
    
    private String description;
    
    private String returnUrl;
    
    private String cancelUrl;
    
    private String callbackUrl;
    
    // Constructors
    public PaymentRequest() {}
    
    public PaymentRequest(Long invoiceId, BigDecimal amount, String paymentMethod, String customerEmail) {
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.customerEmail = customerEmail;
    }
    
    // Getters and Setters
    public Long getInvoiceId() {
        return invoiceId;
    }
    
    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getCustomerEmail() {
        return customerEmail;
    }
    
    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
    
    public String getCustomerPhone() {
        return customerPhone;
    }
    
    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getReturnUrl() {
        return returnUrl;
    }
    
    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }
    
    public String getCancelUrl() {
        return cancelUrl;
    }
    
    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }
    
    public String getCallbackUrl() {
        return callbackUrl;
    }
    
    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }
    
    @Override
    public String toString() {
        return "PaymentRequest{" +
                "invoiceId=" + invoiceId +
                ", amount=" + amount +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", customerEmail='" + customerEmail + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
