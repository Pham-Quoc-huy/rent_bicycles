package com.example.entity;
import jakarta.persistence.*;

@Entity
@Table(name = "qr_codes")
public class QRCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String qrCode; // Dữ liệu mã QR
    
    @Column(nullable = false)
    private Long invoiceId; // ID của hoá đơn, liên kết với hoá đơn

    // Constructors
    public QRCode() {}
    
    public QRCode(String qrCode, Long invoiceId) {
        this.qrCode = qrCode;
        this.invoiceId = invoiceId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }
}