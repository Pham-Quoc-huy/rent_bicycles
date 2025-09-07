package com.example.controller;

import com.example.dto.QRCodeResponse;
import com.example.entity.Invoice;
import com.example.entity.Payment;
import com.example.entity.Station;
import com.example.exception.*;
import com.example.service.InvoiceService;
import com.example.service.PaymentService;
import com.example.service.QRCodeService;
import com.example.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/qr")
@CrossOrigin(origins = "*")
public class QRCodeController {
    
    @Autowired
    private QRCodeService qrCodeService;
    
    @Autowired
    private InvoiceService invoiceService;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private StationRepository stationRepository;
    
    /**
     * Quét QR code để lấy thông tin invoice
     * Frontend sẽ gọi API này sau khi scan QR code
     */
    @GetMapping("/scan/{qrCode}")
    public ResponseEntity<?> scanQRCode(@PathVariable String qrCode) {
        Optional<QRCodeResponse> qrResponse = qrCodeService.findByQrCode(qrCode);
        
        if (qrResponse.isEmpty()) {
            throw new InvalidQRCodeException("QR code không hợp lệ");
        }
        
        QRCodeResponse response = qrResponse.get();
        
        // Kiểm tra xem đã thanh toán chưa
        List<Payment> payments = paymentService.getPaymentsByInvoiceId(response.getInvoiceId());
        boolean hasSuccessfulPayment = payments.stream()
                .anyMatch(p -> "SUCCESS".equals(p.getStatus()));
        
        if (!hasSuccessfulPayment) {
            throw new PaymentRequiredException("Chưa thanh toán thành công");
        }
        
        // Trả về thông tin invoice
        Map<String, Object> result = new HashMap<>();
        result.put("invoiceId", response.getInvoiceId());
        result.put("bikeQuantity", response.getBikeQuantity());
        result.put("stationName", response.getStation().getLocation());
        result.put("bikeStatus", response.getBikeStatus());
        result.put("totalPrice", response.getTotalPrice());
        result.put("canProceed", true);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Xác nhận lấy xe (sau khi đã thanh toán)
     * Frontend gọi API này khi user xác nhận lấy xe
     */
    @PostMapping("/pickup/{qrCode}")
    public ResponseEntity<?> confirmPickup(@PathVariable String qrCode) {
        // Kiểm tra QR code và thanh toán
        Optional<QRCodeResponse> qrResponse = qrCodeService.findByQrCode(qrCode);
        if (qrResponse.isEmpty()) {
            throw new InvalidQRCodeException("QR code không hợp lệ");
        }
        
        QRCodeResponse response = qrResponse.get();
        
        // Kiểm tra thanh toán
        List<Payment> payments = paymentService.getPaymentsByInvoiceId(response.getInvoiceId());
        boolean hasSuccessfulPayment = payments.stream()
                .anyMatch(p -> "SUCCESS".equals(p.getStatus()));
        
        if (!hasSuccessfulPayment) {
            throw new PaymentRequiredException("Chưa thanh toán thành công");
        }
        
        // Kiểm tra trạng thái xe
        if (!"NOT_PICKED_UP".equals(response.getBikeStatus())) {
            throw new BikeStatusException("Xe đã được lấy hoặc đã trả");
        }
        
        // Thực hiện lấy xe
        Invoice updatedInvoice = invoiceService.pickupBike(qrCode);
        
        // Cập nhật số lượng xe tại trạm
        // TODO: Thêm logic cập nhật availableBikes tại station
        
        return ResponseEntity.ok(Map.of(
            "message", "Lấy xe thành công!",
            "invoiceId", updatedInvoice.getId(),
            "pickupTime", updatedInvoice.getRentalStartTime(),
            "bikeStatus", updatedInvoice.getBikeStatus()
        ));
    }
    
    /**
     * Xác nhận trả xe
     * Frontend gọi API này khi user xác nhận trả xe
     */
    @PostMapping("/return/{qrCode}")
    public ResponseEntity<?> confirmReturn(@PathVariable String qrCode, 
                                         @RequestParam(required = false) Long returnStationId) {
        // Kiểm tra QR code
        Optional<QRCodeResponse> qrResponse = qrCodeService.findByQrCode(qrCode);
        if (qrResponse.isEmpty()) {
            throw new InvalidQRCodeException("QR code không hợp lệ");
        }
        
        QRCodeResponse response = qrResponse.get();
        
        // Kiểm tra trạng thái xe
        if (!"PICKED_UP".equals(response.getBikeStatus())) {
            throw new BikeStatusException("Xe chưa được lấy hoặc đã trả");
        }
        
        // Thực hiện trả xe (có thể tại trạm khác)
        Invoice updatedInvoice = invoiceService.returnBike(qrCode, returnStationId);
        
        return ResponseEntity.ok(Map.of(
            "message", "Trả xe thành công!",
            "invoiceId", updatedInvoice.getId(),
            "returnTime", updatedInvoice.getRentalEndTime(),
            "totalTime", updatedInvoice.getTotalTime(),
            "bikeStatus", updatedInvoice.getBikeStatus(),
            "returnStationName", updatedInvoice.getReturnStation() != null ? 
                updatedInvoice.getReturnStation().getLocation() : 
                updatedInvoice.getStation().getLocation()
        ));
    }
    
    /**
     * Lấy thông tin chi tiết invoice theo QR code
     */
    @GetMapping("/invoice/{qrCode}")
    public ResponseEntity<?> getInvoiceByQRCode(@PathVariable String qrCode) {
        Optional<Invoice> invoice = qrCodeService.findInvoiceByQrCode(qrCode);
        
        if (invoice.isEmpty()) {
            throw new InvalidQRCodeException("Không tìm thấy invoice cho QR code này");
        }
        
        return ResponseEntity.ok(invoice.get());
    }
    
    /**
     * Lấy danh sách trạm có thể trả xe
     * Frontend gọi API này để hiển thị danh sách trạm cho user chọn
     */
    @GetMapping("/available-return-stations")
    public ResponseEntity<?> getAvailableReturnStations() {
        List<Station> stations = stationRepository.findAll();
        
        // Lọc ra các trạm có thể trả xe (có thể thêm logic phức tạp hơn)
        List<Map<String, Object>> stationList = stations.stream()
            .map(station -> {
                Map<String, Object> stationInfo = new HashMap<>();
                stationInfo.put("id", station.getId());
                stationInfo.put("location", station.getLocation());
                stationInfo.put("city", station.getCity());
                stationInfo.put("availableBikes", station.getAvailableBikes());
                stationInfo.put("totalBikes", station.getTotalBikes());
                return stationInfo;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(Map.of(
            "stations", stationList,
            "message", "Danh sách trạm có thể trả xe"
        ));
    }
    
    /**
     * Lấy hình ảnh QR code
     * @param qrCode Mã QR code
     * @return Hình ảnh QR code dạng PNG
     */
    @GetMapping("/image/{qrCode}")
    public ResponseEntity<byte[]> getQRCodeImage(@PathVariable String qrCode) {
        Optional<com.example.entity.QRCode> qrCodeOpt = qrCodeService.findQRCodeByQrCode(qrCode);
        
        if (qrCodeOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        com.example.entity.QRCode qrCodeEntity = qrCodeOpt.get();
        byte[] imageBytes = qrCodeEntity.getQrCodeImage();
        
        if (imageBytes == null || imageBytes.length == 0) {
            return ResponseEntity.notFound().build();
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(imageBytes.length);
        headers.set("Content-Disposition", "inline; filename=\"qr-code-" + qrCode + ".png\"");
        
        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }
    
    /**
     * Tạo QR code mới cho invoice
     * @param invoiceId ID của invoice
     * @param type Loại QR code (PICKUP/RETURN)
     * @return Thông tin QR code đã tạo
     */
    @PostMapping("/generate/{invoiceId}")
    public ResponseEntity<?> generateQRCode(@PathVariable Long invoiceId, 
                                          @RequestParam(defaultValue = "PICKUP") String type) {
        try {
            String qrCodeText = String.format("INVOICE:%d:%s", invoiceId, type);
            com.example.entity.QRCode qrCode = qrCodeService.createQRCode(qrCodeText, invoiceId, type);
            
            return ResponseEntity.ok(Map.of(
                "qrCodeId", qrCode.getId(),
                "qrCode", qrCode.getQrCode(),
                "invoiceId", qrCode.getInvoiceId(),
                "type", qrCode.getType(),
                "status", qrCode.getStatus(),
                "imageUrl", "/api/qr/image/" + qrCode.getQrCode(),
                "message", "Tạo QR code thành công"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi khi tạo QR code: " + e.getMessage()
            ));
        }
    }
}
