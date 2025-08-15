package com.example.repository;

import com.example.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // Tìm payment theo invoice ID
    List<Payment> findByInvoiceIdOrderByCreatedAtDesc(Long invoiceId);
    
    // Tìm payment theo status
    List<Payment> findByStatus(String status);
    
    // Tìm payment theo payment method
    List<Payment> findByPaymentMethod(String paymentMethod);
    
    // Tìm payment theo gateway ID
    Optional<Payment> findByGatewayId(String gatewayId);
    
    // Tìm payment theo transaction ID
    Optional<Payment> findByTransactionId(String transactionId);
    
    // Tìm payment theo customer email
    List<Payment> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail);
    
    // Tìm payment theo customer phone
    List<Payment> findByCustomerPhoneOrderByCreatedAtDesc(String customerPhone);
    
    // Tìm payment theo khoảng thời gian
    List<Payment> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    // Tìm payment đã hết hạn
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.expiredAt < :now")
    List<Payment> findExpiredPayments(@Param("now") LocalDateTime now);
    
    // Tìm payment đang pending
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING'")
    List<Payment> findPendingPayments();
    
    // Tìm payment thành công theo khoảng thời gian
    @Query("SELECT p FROM Payment p WHERE p.status = 'SUCCESS' AND p.completedAt BETWEEN :startTime AND :endTime")
    List<Payment> findSuccessfulPaymentsByDateRange(@Param("startTime") LocalDateTime startTime, 
                                                   @Param("endTime") LocalDateTime endTime);
    
    // Đếm payment theo status
    long countByStatus(String status);
    
    // Đếm payment theo payment method
    long countByPaymentMethod(String paymentMethod);
    
    // Tìm payment theo user ID (thông qua invoice)
    @Query("SELECT p FROM Payment p JOIN Invoice i ON p.invoiceId = i.id WHERE i.user.id = :userId")
    List<Payment> findByUserId(@Param("userId") Long userId);
    
    // Tìm payment theo station ID (thông qua invoice)
    @Query("SELECT p FROM Payment p JOIN Invoice i ON p.invoiceId = i.id WHERE i.station.id = :stationId")
    List<Payment> findByStationId(@Param("stationId") Long stationId);
}

