package com.example.service;

import com.example.entity.QRCode;
import com.example.entity.Invoice;
import com.example.dto.QRCodeResponse;

import java.util.Optional;

public interface QRCodeService {
    
    /**
     * Tìm QR code theo mã QR và trả về thông tin chi tiết
     * @param qrCode Mã QR cần tìm
     * @return Optional chứa thông tin chi tiết nếu tìm thấy
     */
    Optional<QRCodeResponse> findByQrCode(String qrCode);
    
    /**
     * Tìm QR code theo mã QR và trả về thông tin invoice
     * @param qrCode Mã QR cần tìm
     * @return Optional chứa thông tin invoice nếu tìm thấy
     */
    Optional<Invoice> findInvoiceByQrCode(String qrCode);
    
    /**
     * Tìm QR code theo mã QR
     * @param qrCode Mã QR cần tìm
     * @return Optional chứa QR code nếu tìm thấy
     */
    Optional<QRCode> findQRCodeByQrCode(String qrCode);
    
    /**
     * Tạo QR code mới
     * @param qrCode Mã QR
     * @param invoiceId ID của invoice
     * @param type Loại QR code (PICKUP/RETURN)
     * @return QR code đã được tạo
     */
    QRCode createQRCode(String qrCode, Long invoiceId, String type);
    
    /**
     * Tạo QR code mới (backward compatibility)
     * @param qrCode Mã QR
     * @param invoiceId ID của invoice
     * @return QR code đã được tạo
     */
    QRCode createQRCode(String qrCode, Long invoiceId);
    
    /**
     * Xóa QR code theo invoice ID
     * @param invoiceId ID của invoice
     */
    void deleteByInvoiceId(Long invoiceId);
}
