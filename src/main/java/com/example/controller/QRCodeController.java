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
import jakarta.servlet.http.HttpServletRequest;

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
     * Lấy hình ảnh QR code bằng Base64 encoded ID
     * @param encodedQrCode Base64 encoded QR code
     * @return Hình ảnh QR code dạng PNG
     */
    @GetMapping("/image/{encodedQrCode}")
    public ResponseEntity<byte[]> getQRCodeImage(@PathVariable String encodedQrCode) {
        try {
            // Decode Base64
            String qrCode = new String(java.util.Base64.getDecoder().decode(encodedQrCode));
            
            System.out.println("Looking for QR code: " + qrCode);
            
            Optional<com.example.entity.QRCode> qrCodeOpt = qrCodeService.findQRCodeByQrCode(qrCode);
            
            if (qrCodeOpt.isEmpty()) {
                System.out.println("QR code not found: " + qrCode);
                return ResponseEntity.notFound().build();
            }
            
            com.example.entity.QRCode qrCodeEntity = qrCodeOpt.get();
            byte[] imageBytes = qrCodeEntity.getQrCodeImage();
            
            if (imageBytes == null || imageBytes.length == 0) {
                System.out.println("QR code image is empty for: " + qrCode);
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("Found QR code image, size: " + imageBytes.length + " bytes");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(imageBytes.length);
            headers.set("Content-Disposition", "inline; filename=\"qr-code.png\"");
            
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            System.err.println("Error getting QR code image: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
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
            String qrCodeUrl = String.format("http://localhost:3000/invoiceDetail.html?id=%d&type=%s", invoiceId, type);
            com.example.entity.QRCode qrCode = qrCodeService.createQRCode(qrCodeUrl, invoiceId, type);
            
            return ResponseEntity.ok(Map.of(
                "qrCodeId", qrCode.getId(),
                "qrCode", qrCode.getQrCode(),
                "qrCodeUrl", qrCodeUrl,
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
    
    /**
     * API cho frontend lấy thông tin invoice theo ID
     * @param invoiceId ID của invoice
     * @return Thông tin chi tiết invoice
     */
    @GetMapping("/invoice-detail/{invoiceId}")
    public ResponseEntity<?> getInvoiceDetail(@PathVariable Long invoiceId) {
        try {
            // Lấy thông tin invoice
            Optional<Invoice> invoiceOpt = invoiceService.getInvoiceById(invoiceId);
            
            if (invoiceOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Invoice invoice = invoiceOpt.get();
            
            // Lấy thông tin payment
            List<Payment> payments = paymentService.getPaymentsByInvoiceId(invoiceId);
            
            // Tìm payment thành công
            Payment successfulPayment = null;
            for (Payment p : payments) {
                if ("SUCCESS".equals(p.getStatus())) {
                    successfulPayment = p;
                    break;
                }
            }
            
            // Tạo response với thông tin đầy đủ
            Map<String, Object> response = new HashMap<>();
            
            // Thông tin invoice
            Map<String, Object> invoiceInfo = new HashMap<>();
            invoiceInfo.put("id", invoice.getId());
            invoiceInfo.put("userName", invoice.getUser() != null ? invoice.getUser().getFullName() : "N/A");
            invoiceInfo.put("userEmail", invoice.getUser() != null ? invoice.getUser().getEmail() : "N/A");
            invoiceInfo.put("stationName", invoice.getStation() != null ? invoice.getStation().getLocation() : "N/A");
            invoiceInfo.put("bikeQuantity", invoice.getBikeQuantity());
            invoiceInfo.put("totalPrice", invoice.getTotalPrice());
            invoiceInfo.put("bikeStatus", invoice.getBikeStatus());
            invoiceInfo.put("rentalStartTime", invoice.getRentalStartTime());
            invoiceInfo.put("rentalEndTime", invoice.getRentalEndTime());
            invoiceInfo.put("totalTime", invoice.getTotalTime());
            invoiceInfo.put("createdAt", invoice.getCreatedAt());
            
            response.put("invoice", invoiceInfo);
            
            // Thông tin payment
            if (successfulPayment != null) {
                Map<String, Object> paymentInfo = new HashMap<>();
                paymentInfo.put("id", successfulPayment.getId());
                paymentInfo.put("amount", successfulPayment.getAmount());
                paymentInfo.put("currency", successfulPayment.getCurrency());
                paymentInfo.put("paymentMethod", successfulPayment.getPaymentMethod());
                paymentInfo.put("status", successfulPayment.getStatus());
                paymentInfo.put("transactionId", successfulPayment.getTransactionId());
                paymentInfo.put("completedAt", successfulPayment.getCompletedAt());
                paymentInfo.put("description", successfulPayment.getDescription());
                
                response.put("payment", paymentInfo);
                response.put("paymentStatus", "PAID");
            } else {
                response.put("payment", null);
                response.put("paymentStatus", "UNPAID");
            }
            
            // Tổng số payment
            response.put("totalPayments", payments.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi khi lấy thông tin invoice: " + e.getMessage()
            ));
        }
    }
}
