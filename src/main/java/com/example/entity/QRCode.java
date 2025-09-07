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
    
    @Column(nullable = false)
    private String type; // Loại QR code: PICKUP (lấy xe), RETURN (trả xe)
    
    @Column
    private String status; // Trạng thái: ACTIVE, USED, EXPIRED
    
    @Column(columnDefinition = "LONGBLOB")
    private byte[] qrCodeImage; // Hình ảnh QR code dạng byte array
    
    @Column
    private String imagePath; // Đường dẫn file hình ảnh (tùy chọn)

    // Constructors
    public QRCode() {}
    
    public QRCode(String qrCode, Long invoiceId, String type) {
        this.qrCode = qrCode;
        this.invoiceId = invoiceId;
        this.type = type;
        this.status = "ACTIVE";
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
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public byte[] getQrCodeImage() {
        return qrCodeImage;
    }
    
    public void setQrCodeImage(byte[] qrCodeImage) {
        this.qrCodeImage = qrCodeImage;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}