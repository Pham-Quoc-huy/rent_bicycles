package com.example.service.impl;

import com.example.entity.QRCode;
import com.example.entity.Invoice;
import com.example.dto.QRCodeResponse;
import com.example.repository.QRCodeReponsitory;
import com.example.repository.InvoiceRepository;
import com.example.service.QRCodeService;
import com.example.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class QRCodeServiceImpl implements QRCodeService {
    
    @Autowired
    private QRCodeReponsitory qrCodeRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Override
    public Optional<QRCodeResponse> findByQrCode(String qrCode) {
        // Tìm QR code theo mã QR
        Optional<QRCode> qrCodeOpt = qrCodeRepository.findByQrCode(qrCode);
        
        if (qrCodeOpt.isPresent()) {
            // Nếu tìm thấy QR code, lấy invoice ID và tìm thông tin invoice
            QRCode qrCodeEntity = qrCodeOpt.get();
            Long invoiceId = qrCodeEntity.getInvoiceId();
            Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
            
            if (invoiceOpt.isPresent()) {
                // Tạo response DTO với thông tin chi tiết
                QRCodeResponse response = new QRCodeResponse(
                    qrCodeEntity.getId(),
                    qrCodeEntity.getQrCode(),
                    qrCodeEntity.getInvoiceId(),
                    invoiceOpt.get()
                );
                return Optional.of(response);
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<Invoice> findInvoiceByQrCode(String qrCode) {
        // Tìm QR code theo mã QR
        Optional<QRCode> qrCodeOpt = qrCodeRepository.findByQrCode(qrCode);
        
        if (qrCodeOpt.isPresent()) {
            // Nếu tìm thấy QR code, lấy invoice ID và tìm thông tin invoice
            Long invoiceId = qrCodeOpt.get().getInvoiceId();
            return invoiceRepository.findById(invoiceId);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<QRCode> findQRCodeByQrCode(String qrCode) {
        return qrCodeRepository.findByQrCode(qrCode);
    }
    
    @Override
    public QRCode createQRCode(String qrCode, Long invoiceId, String type) {
        // Kiểm tra xem QR code đã tồn tại chưa
        if (qrCodeRepository.existsByQrCode(qrCode)) {
            throw new InvalidQRCodeException("QR code đã tồn tại: " + qrCode);
        }
        
        // Kiểm tra xem invoice có tồn tại không
        if (!invoiceRepository.existsById(invoiceId)) {
            throw new InvoiceNotFoundException("Invoice không tồn tại với ID: " + invoiceId);
        }
        
        // Tạo QR code mới với type
        QRCode newQRCode = new QRCode(qrCode, invoiceId, type);
        return qrCodeRepository.save(newQRCode);
    }
    
    @Override
    public QRCode createQRCode(String qrCode, Long invoiceId) {
        // Backward compatibility - tạo QR code với type mặc định là PICKUP
        return createQRCode(qrCode, invoiceId, "PICKUP");
    }
    
    @Override
    public void deleteByInvoiceId(Long invoiceId) {
        qrCodeRepository.deleteByInvoiceId(invoiceId);
    }
}
