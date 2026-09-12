package vn.edu.eaut.ems.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.*;
import vn.edu.eaut.ems.entity.Booking;
import vn.edu.eaut.ems.entity.Building;
import vn.edu.eaut.ems.repository.BookingRepository;
import vn.edu.eaut.ems.repository.BuildingRepository;
import vn.edu.eaut.ems.repository.RoomRepository;

@RestController
@RequestMapping("/api")
public class RoomApiController {

    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public RoomApiController(BuildingRepository buildingRepository, 
                              RoomRepository roomRepository,
                              BookingRepository bookingRepository) {
        this.buildingRepository = buildingRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    // 1. API LẤY TOÀN BỘ TÒA NHÀ VÀ PHÒNG HỌC
    @GetMapping("/buildings")
    public List<Building> getAllBuildingsAndRooms() {
        return buildingRepository.findAll(); 
    }

    // 2. API LẤY MÃ CÁC PHÒNG ĐANG BẬN / ĐANG CHỜ DUYỆT TRONG CA HỌC
    @GetMapping("/busy-rooms")
    public List<String> getBusyRooms(
            @RequestParam(value = "caMuon", defaultValue = "1") Integer caMuon,
            @RequestParam(value = "ngayMuon", required = false) String ngayMuon) {

        LocalDate targetDate = (ngayMuon != null && !ngayMuon.isBlank()) 
                ? LocalDate.parse(ngayMuon) 
                : LocalDate.now();

        // Lọc tất cả các phòng đang có đơn chưa trả (Kể cả Chờ duyệt lẫn Đang mượn)
        List<Booking> activeBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getNgayMuon() != null && b.getNgayMuon().isEqual(targetDate))
                .filter(b -> b.getCaMuon() != null && b.getCaMuon().equals(caMuon))
                .filter(b -> !"DA_TRA".equalsIgnoreCase(b.getTrangThai()) 
                          && !"TU_CHOI".equalsIgnoreCase(b.getTrangThai())
                          && !"DA_UY_QUYEN".equalsIgnoreCase(b.getTrangThai()))
                .toList();

        return activeBookings.stream()
                .filter(b -> b.getRoom() != null)
                .map(b -> b.getRoom().getMaPhong().replace("-", "").trim().toUpperCase())
                .toList();
    }
}