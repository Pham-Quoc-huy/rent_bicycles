package com.example.config;

import com.example.entity.Station;
import com.example.entity.User;
import com.example.repository.StationRepository;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StationRepository stationRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Tạo admin mặc định nếu chưa tồn tại
        createDefaultAdmin();
        
        // Tạo dữ liệu mẫu cho stations
        createSampleStations();
        
        System.out.println("✅ Data initialization completed!");
    }
    
    private void createDefaultAdmin() {
        if (!userRepository.existsByEmail("admin@rentbicycles.com")) {
            User admin = new User();
            admin.setEmail("admin@rentbicycles.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("System Administrator");
            admin.setPhone("0123456789");
            admin.setAddress("Hà Nội, Việt Nam");
            admin.setRole(User.UserRole.ADMIN);
            admin.setEnabled(true);
            
            userRepository.save(admin);
            
            System.out.println("✅ Admin account created:");
            System.out.println("   Email: admin@rentbicycles.com");
            System.out.println("   Password: admin123");
            System.out.println("   Role: ADMIN");
        } else {
            System.out.println("ℹ️ Admin account already exists");
        }
    }
    
    private void createSampleStations() {
        // Kiểm tra xem đã có stations chưa
        if (stationRepository.count() == 0) {
            // Tạo stations mẫu
            Station station1 = new Station();
            station1.setLocation("Hồ Hoàn Kiếm");
            station1.setCity("Hà Nội");
            station1.setTotalBikes(50);
            station1.setAvailableBikes(45);
            stationRepository.save(station1);
            
            Station station2 = new Station();
            station2.setLocation("Công viên Thống Nhất");
            station2.setCity("Hà Nội");
            station2.setTotalBikes(30);
            station2.setAvailableBikes(25);
            stationRepository.save(station2);
            
            Station station3 = new Station();
            station3.setLocation("Bờ Hồ Tây");
            station3.setCity("Hà Nội");
            station3.setTotalBikes(40);
            station3.setAvailableBikes(35);
            stationRepository.save(station3);
            
            Station station4 = new Station();
            station4.setLocation("Công viên Tao Đàn");
            station4.setCity("TP.HCM");
            station4.setTotalBikes(60);
            station4.setAvailableBikes(50);
            stationRepository.save(station4);
            
            Station station5 = new Station();
            station5.setLocation("Bến Bạch Đằng");
            station5.setCity("TP.HCM");
            station5.setTotalBikes(35);
            station5.setAvailableBikes(30);
            stationRepository.save(station5);
            
            System.out.println("✅ Sample stations created:");
            System.out.println("   - Hồ Hoàn Kiếm (Hà Nội): 45/50 xe");
            System.out.println("   - Công viên Thống Nhất (Hà Nội): 25/30 xe");
            System.out.println("   - Bờ Hồ Tây (Hà Nội): 35/40 xe");
            System.out.println("   - Công viên Tao Đàn (TP.HCM): 50/60 xe");
            System.out.println("   - Bến Bạch Đằng (TP.HCM): 30/35 xe");
        } else {
            System.out.println("ℹ️ Sample stations already exist");
        }
    }
} 