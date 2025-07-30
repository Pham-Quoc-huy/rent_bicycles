package com.example.controller;

import com.example.entity.Station;
import com.example.repository.StationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/stations")
@CrossOrigin(origins = "*")
public class StationController {
    
    @Autowired
    private StationRepository stationRepository;
    
    // Lấy tất cả trạm
    @GetMapping
    public ResponseEntity<List<Station>> getAllStations() {
        List<Station> stations = stationRepository.findAll();
        return ResponseEntity.ok(stations);
    }
    
    // Lấy trạm theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Station> getStationById(@PathVariable Long id) {
        Optional<Station> station = stationRepository.findById(id);
        if (station.isPresent()) {
            return ResponseEntity.ok(station.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Lấy trạm theo thành phố
    @GetMapping("/city/{city}")
    public ResponseEntity<List<Station>> getStationsByCity(@PathVariable String city) {
        List<Station> stations = stationRepository.findByCity(city);
        return ResponseEntity.ok(stations);
    }
    
    // Lấy trạm có xe khả dụng
    @GetMapping("/available")
    public ResponseEntity<List<Station>> getAvailableStations() {
        List<Station> stations = stationRepository.findByAvailableBikesGreaterThan(0);
        return ResponseEntity.ok(stations);
    }
    
    // Tìm trạm theo địa điểm
    @GetMapping("/search")
    public ResponseEntity<List<Station>> searchStations(@RequestParam String location) {
        List<Station> stations = stationRepository.findByLocationContainingIgnoreCase(location);
        return ResponseEntity.ok(stations);
    }
    
    // Admin: Tạo trạm mới
    @PostMapping
    public ResponseEntity<Station> createStation(@RequestBody Station station) {
        Station savedStation = stationRepository.save(station);
        return ResponseEntity.ok(savedStation);
    }
    
    // Admin: Cập nhật trạm
    @PutMapping("/{id}")
    public ResponseEntity<Station> updateStation(@PathVariable Long id, @RequestBody Station station) {
        Optional<Station> existingStation = stationRepository.findById(id);
        if (existingStation.isPresent()) {
            station.setId(id);
            Station updatedStation = stationRepository.save(station);
            return ResponseEntity.ok(updatedStation);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Admin: Xóa trạm
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStation(@PathVariable Long id) {
        Optional<Station> station = stationRepository.findById(id);
        if (station.isPresent()) {
            stationRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
} 