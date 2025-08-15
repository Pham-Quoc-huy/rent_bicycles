package com.example.repository;

import com.example.entity.QRCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QRCodeReponsitory extends JpaRepository<QRCode, Long> {
    
    // Tìm QR code theo mã QR
    Optional<QRCode> findByQrCode(String qrCode);
    
    // Tìm QR code theo invoice ID
    Optional<QRCode> findByInvoiceId(Long invoiceId);
    
    // Kiểm tra xem QR code có tồn tại không
    boolean existsByQrCode(String qrCode);
    
    // Xóa QR code theo invoice ID
    void deleteByInvoiceId(Long invoiceId);
}
