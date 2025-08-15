package com.example.service.impl;

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
import com.example.exception.*;
import com.example.repository.BookingRepository;
import com.example.repository.StationRepository;
import com.example.repository.UserRepository;
import com.example.service.BookingService;
import com.example.service.InvoiceService;

@Service
public class BookingServiceImpl implements BookingService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StationRepository stationRepository;
    
    @Autowired
    private InvoiceService invoiceService;
    
    @Override
    public BookingResponse createBooking(String userEmail, BookingRequest request) {
        // Validation đã được xử lý bởi @Valid annotation
        
        // Tìm user
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("Không tìm thấy người dùng với email: " + userEmail);
        }
        User user = userOpt.get();
        
        // Tìm station
        Optional<Station> stationOpt = stationRepository.findById(request.getStationId());
        if (stationOpt.isEmpty()) {
            throw new StationNotFoundException("Không tìm thấy trạm xe với ID: " + request.getStationId());
        }
        Station station = stationOpt.get();
        
        // Kiểm tra số lượng xe có sẵn
        if (station.getAvailableBikes() < request.getBikeQuantity()) {
            throw new InsufficientBikesException("Không đủ xe tại trạm này. Có sẵn: " + station.getAvailableBikes() + " xe");
        }
        
        // Tạo booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setStation(station);
        booking.setBikeQuantity(request.getBikeQuantity());
        booking.setNotes(request.getNotes());
        booking.setStatus(Booking.BookingStatus.PENDING);
        
        double estimatedPrice = request.getBikeQuantity() * 500.0;
        booking.setEstimatedPrice(estimatedPrice);
        
        // Lưu booking
        booking = bookingRepository.save(booking);
        
        // Tự động tạo invoice
        Invoice invoice = invoiceService.createInvoiceFromBooking(booking.getId());
        System.out.println("✅ Đã tạo invoice: " + invoice.getId());
        
        return BookingResponse.success(booking, "Đặt xe thành công! Vui lòng đến trạm và quét mã QR để lấy xe.");
    }
    
    @Override
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
    
    @Override
    public BookingResponse getBookingById(Long bookingId, String userEmail) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            throw new BookingNotFoundException("Không tìm thấy booking với ID: " + bookingId);
        }
        
        Booking booking = bookingOpt.get();
        
        // Kiểm tra quyền truy cập
        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedAccessException("Không có quyền truy cập booking này");
        }
        
        return BookingResponse.success(booking, "");
    }
    
    @Override
    public BookingResponse cancelBooking(Long bookingId, String userEmail) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            throw new BookingNotFoundException("Không tìm thấy booking với ID: " + bookingId);
        }
        
        Booking booking = bookingOpt.get();
        
        // Kiểm tra quyền truy cập
        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedAccessException("Không có quyền hủy booking này");
        }
        
        // Kiểm tra trạng thái
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new BikeStatusException("Chỉ có thể hủy booking đang chờ xác nhận");
        }
        
        // Hủy booking
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        
        return BookingResponse.success(booking, "Đã hủy booking thành công");
    }
    
    @Override
    public BookingResponse confirmBooking(Long bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            throw new BookingNotFoundException("Không tìm thấy booking với ID: " + bookingId);
        }
        
        Booking booking = bookingOpt.get();
        
        // Kiểm tra trạng thái
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new BikeStatusException("Chỉ có thể xác nhận booking đang chờ");
        }
        
        // Xác nhận booking
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        
        return BookingResponse.success(booking, "Đã xác nhận booking thành công");
    }
    
    @Override
    public BookingResponse completeBooking(Long bookingId, String userEmail) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            throw new BookingNotFoundException("Không tìm thấy booking với ID: " + bookingId);
        }
        
        Booking booking = bookingOpt.get();
        
        // Kiểm tra quyền truy cập
        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedAccessException("Không có quyền hoàn thành booking này");
        }
        
        // Kiểm tra trạng thái
        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new BikeStatusException("Booking phải được xác nhận trước khi hoàn thành");
        }
        
        // Hoàn thành booking
        booking.setStatus(Booking.BookingStatus.COMPLETED);
        booking.setActualPickupTime(LocalDateTime.now());
        bookingRepository.save(booking);
        
        return BookingResponse.success(booking, "Đã lấy xe thành công!");
    }
    
    @Override
    public List<BookingResponse> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream()
                .map(booking -> BookingResponse.success(booking, ""))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<BookingResponse> getBookingsByStatus(Booking.BookingStatus status) {
        List<Booking> bookings = bookingRepository.findByStatus(status);
        return bookings.stream()
                .map(booking -> BookingResponse.success(booking, ""))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<BookingResponse> getBookingsByStation(Long stationId) {
        List<Booking> bookings = bookingRepository.findByStationIdOrderByBookingTimeDesc(stationId);
        return bookings.stream()
                .map(booking -> BookingResponse.success(booking, ""))
                .collect(Collectors.toList());
    }
}