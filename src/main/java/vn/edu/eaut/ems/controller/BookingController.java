package vn.edu.eaut.ems.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.eaut.ems.entity.*;
import vn.edu.eaut.ems.repository.*;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BookingController {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public BookingController(RoomRepository roomRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    @PostMapping("/bookings")
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request, HttpSession session) {

        // 1. Kiểm tra xem Sinh viên đã đăng nhập chưa
        Account currentUser = (Account) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập!"));
        }

        // 2. Tìm phòng trong Database
        Room room = roomRepository.findById(request.getMaPhong()).orElse(null);
        if (room == null || !room.getTrangThai().equals("0")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Phòng không tồn tại hoặc đã có người mượn!"));
        }

        // 3. Cập nhật trạng thái phòng thành Đang mượn ('1')
        room.setTrangThai("1");
        roomRepository.save(room);

        // 4. Ghi vào lịch sử mượn
        Booking booking = new Booking();
        booking.setAccount(currentUser);
        booking.setRoom(room);
        booking.setNgayMuon(request.getNgayMuon());
        booking.setCaMuon(request.getCaMuon());
        booking.setTrangThai("ACTIVE");
        bookingRepository.save(booking);

        return ResponseEntity.ok(Map.of("message", "Đăng ký mượn phòng thành công!"));
    }

    @GetMapping("/lich-su")
    public ResponseEntity<?> getMyHistory(HttpSession session) {
        Account currentUser = (Account) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập!"));
        }

        List<Booking> histories = bookingRepository.findByAccountOrderByThoiGianTaoDesc(currentUser);

        List<Map<String, Object>> responseData = histories.stream().map(b -> {
            Map<String, Object> record = new HashMap<>();
            record.put("id", b.getId());
            record.put("ngayMuon", b.getNgayMuon());
            record.put("caMuon", b.getCaMuon());
            record.put("trangThai", b.getTrangThai());
            record.put("thoiGianTao", b.getThoiGianTao());

            Map<String, Object> roomData = new HashMap<>();
            roomData.put("tenPhong", b.getRoom().getTenPhong());

            Map<String, Object> buildingData = new HashMap<>();
            buildingData.put("tenToaNha", b.getRoom().getBuilding().getTenToaNha());

            roomData.put("building", buildingData); 
            record.put("room", roomData);

            return record;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/bookings/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Integer id, HttpSession session) {
        Account currentUser = (Account) session.getAttribute("loggedInUser");
        if (currentUser == null)
            return ResponseEntity.status(401).build();

        Booking booking = bookingRepository.findById(id).orElse(null);

        // Kiểm tra xem phiếu có tồn tại và có đúng là của sinh viên này không
        if (booking == null || !booking.getAccount().getMaSv().equals(currentUser.getMaSv())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Không hợp lệ!"));
        }

        if ("ACTIVE".equals(booking.getTrangThai())) {
            // Đổi trạng thái phiếu mượn thành CANCELED
            booking.setTrangThai("CANCELED");
            bookingRepository.save(booking);

            // Trả lại phòng (Đổi trạng thái phòng về 0 - Trống)
            Room room = booking.getRoom();
            if (room != null) {
                room.setTrangThai("0");
                roomRepository.save(room);
            }
            return ResponseEntity.ok(Map.of("message", "Hủy thành công!"));
        }

        return ResponseEntity.badRequest().body(Map.of("message", "Phiếu này không thể hủy!"));
    }
}