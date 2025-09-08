package com.example.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class QRCodeGeneratorService {
    
    private static final int QR_CODE_WIDTH = 300;
    private static final int QR_CODE_HEIGHT = 300;
    
    /**
     * Tạo QR code từ text và trả về dạng byte array
     * @param text Nội dung cần mã hóa thành QR code
     * @return byte array của hình ảnh QR code
     * @throws WriterException
     * @throws IOException
     */
    public byte[] generateQRCodeImage(String text) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT);
        
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        
        return pngOutputStream.toByteArray();
    }
    
    /**
     * Tạo QR code cho invoice
     * @param invoiceId ID của invoice
     * @param type Loại QR code (PICKUP/RETURN)
     * @return byte array của hình ảnh QR code
     */
    public byte[] generateInvoiceQRCode(Long invoiceId, String type) throws WriterException, IOException {
        // Tạo URL dẫn đến trang chi tiết invoice
        String qrText = String.format("http://localhost:3000/invoiceDetail.html?id=%d&type=%s", invoiceId, type);
        return generateQRCodeImage(qrText);
    }
    
    /**
     * Tạo QR code với ID đơn giản (dễ encode hơn)
     * @param invoiceId ID của invoice
     * @param type Loại QR code (PICKUP/RETURN)
     * @return byte array của hình ảnh QR code
     */
    public byte[] generateSimpleInvoiceQRCode(Long invoiceId, String type) throws WriterException, IOException {
        // Tạo QR code với format đơn giản: INVOICE:ID:TYPE
        String qrText = String.format("INVOICE:%d:%s", invoiceId, type);
        return generateQRCodeImage(qrText);
    }
    
    /**
     * Tạo QR code cho booking
     * @param bookingId ID của booking
     * @return byte array của hình ảnh QR code
     */
    public byte[] generateBookingQRCode(Long bookingId) throws WriterException, IOException {
        String qrText = String.format("BOOKING:%d", bookingId);
        return generateQRCodeImage(qrText);
    }
}
