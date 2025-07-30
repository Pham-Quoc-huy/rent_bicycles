 package com.example.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.dto.BookingRequest;
import com.example.dto.BookingResponse;
import com.example.entity.Booking;
import com.example.entity.Invoice;
import com.example.entity.Station;
import com.example.entity.User;
import com.example.repository.BookingRepository;
import com.example.repository.StationRepository;
import com.example.repository.UserRepository;

@Service
public class BookingService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StationRepository stationRepository;
    
    @Autowired
    private InvoiceService invoiceService;
    
    // Đặt xe
    public BookingResponse createBooking(String userEmail, BookingRequest request) {
        // Validation
        if (request.getStationId() == null) {
            return BookingResponse.error("ID trạm không được để trống");
        }
        if (request.getBikeQuantity() == null || request.getBikeQuantity() <= 0) {
            return BookingResponse.error("Số lượng xe phải lớn hơn 0");
        }
        
        // Tìm user
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            return BookingResponse.error("Không tìm thấy người dùng");
        }
        User user = userOpt.get();
        
        // Tìm station
        Optional<Station> stationOpt = stationRepository.findById(request.getStationId());
        if (stationOpt.isEmpty()) {
            return BookingResponse.error("Không tìm thấy trạm xe");
        }
        Station station = stationOpt.get();
        
        // Kiểm tra số lượng xe có sẵn
        if (station.getAvailableBikes() < request.getBikeQuantity()) {
            return BookingResponse.error("Không đủ xe tại trạm này. Có sẵn: " + station.getAvailableBikes() + " xe");
        }
        
        // Tạo booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setStation(station);
        booking.setBikeQuantity(request.getBikeQuantity());
        booking.setNotes(request.getNotes());
        booking.setStatus(Booking.BookingStatus.PENDING);
        
        // Tính giá ước tính (50k/giờ)
        double estimatedPrice = request.getBikeQuantity() * 50000.0;
        booking.setEstimatedPrice(estimatedPrice);
        
        // Lưu booking
        booking = bookingRepository.save(booking);
        
        // Tự động tạo invoice
        try {
            Invoice invoice = invoiceService.createInvoiceFromBooking(booking.getId());
            System.out.println("✅ Đã tạo invoice: " + invoice.getId() + " với QR code: " + invoice.getQrCode());
        } catch (Exception e) {
            System.err.println("❌ Lỗi tạo invoice: " + e.getMessage());
        }
        
        return BookingResponse.success(booking, "Đặt xe thành công! Vui lòng đến trạm và quét mã QR để lấy xe.");
    }
    
    // Lấy danh sách booking của user
    public List<BookingResponse> getUserBookings(String userEmail) {
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            return List.of();
        }
        
        List<Booking> bookings = bookingRepository.findByUserOrderByBookingTimeDesc(userOpt.get());
        return bookings.stream()
                .map(booking -> BookingResponse.success(booking, ""))
                .collect(Collectors.toList());
    }
    
    // Lấy booking theo ID
    public BookingResponse getBookingById(Long bookingId, String userEmail) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return BookingResponse.error("Không tìm thấy booking");
        }
        
        Booking booking = bookingOpt.get();
        
        // Kiểm tra quyền truy cập
        if (!booking.getUser().getEmail().equals(userEmail)) {
            return BookingResponse.error("Không có quyền truy cập booking này");
        }
        
        return BookingResponse.success(booking, "");
    }
    
    // Hủy booking
    public BookingResponse cancelBooking(Long bookingId, String userEmail) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return BookingResponse.error("Không tìm thấy booking");
        }
        
        Booking booking = bookingOpt.get();
        
        // Kiểm tra quyền truy cập
        if (!booking.getUser().getEmail().equals(userEmail)) {
            return BookingResponse.error("Không có quyền hủy booking này");
        }
        
        // Kiểm tra trạng thái
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            return BookingResponse.error("Chỉ có thể hủy booking đang chờ xác nhận");
        }
        
        // Hủy booking
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        
        return BookingResponse.success(booking, "Đã hủy booking thành công");
    }
    
    // Xác nhận booking (cho admin)
    public BookingResponse confirmBooking(Long bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return BookingResponse.error("Không tìm thấy booking");
        }
        
        Booking booking = bookingOpt.get();
        
        // Kiểm tra trạng thái
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            return BookingResponse.error("Chỉ có thể xác nhận booking đang chờ");
        }
        
        // Xác nhận booking
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        
        return BookingResponse.success(booking, "Đã xác nhận booking thành công");
    }
    
    // Hoàn thành booking (khi user lấy xe)
    public BookingResponse completeBooking(Long bookingId, String userEmail) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return BookingResponse.error("Không tìm thấy booking");
        }
        
        Booking booking = bookingOpt.get();
        
        // Kiểm tra quyền truy cập
        if (!booking.getUser().getEmail().equals(userEmail)) {
            return BookingResponse.error("Không có quyền hoàn thành booking này");
        }
        
        // Kiểm tra trạng thái
        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            return BookingResponse.error("Booking phải được xác nhận trước khi hoàn thành");
        }
        
        // Hoàn thành booking
        booking.setStatus(Booking.BookingStatus.COMPLETED);
        booking.setActualPickupTime(LocalDateTime.now());
        bookingRepository.save(booking);
        
        return BookingResponse.success(booking, "Đã lấy xe thành công!");
    }
    
    // Lấy danh sách booking cho admin
    public List<BookingResponse> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream()
                .map(booking -> BookingResponse.success(booking, ""))
                .collect(Collectors.toList());
    }
    
    // Lấy booking theo trạng thái
    public List<BookingResponse> getBookingsByStatus(Booking.BookingStatus status) {
        List<Booking> bookings = bookingRepository.findByStatus(status);
        return bookings.stream()
                .map(booking -> BookingResponse.success(booking, ""))
                .collect(Collectors.toList());
    }
    
    // Lấy booking theo station
    public List<BookingResponse> getBookingsByStation(Long stationId) {
        List<Booking> bookings = bookingRepository.findByStationIdOrderByBookingTimeDesc(stationId);
        return bookings.stream()
                .map(booking -> BookingResponse.success(booking, ""))
                .collect(Collectors.toList());
    }
}