package vn.edu.eaut.ems.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.eaut.ems.entity.*;
import vn.edu.eaut.ems.repository.*;
import vn.edu.eaut.ems.entity.BookingRequest;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

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

        return ResponseEntity.ok(Map.of("message", "Mượn phòng thành công!"));
    }
}