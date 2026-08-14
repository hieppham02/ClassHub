package vn.edu.eaut.ems.controller;

import java.time.LocalDate;
import java.util.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.eaut.ems.entity.*;
import vn.edu.eaut.ems.repository.*;
import jakarta.servlet.http.HttpSession;

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

        if (!request.getNgayMuon().equals(LocalDate.now())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Lỗi: Chỉ được phép mượn trong ngày hôm nay!"));
        }

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

    // Đổi endpoint thành /return
    @PostMapping("/bookings/{id}/return")
    public ResponseEntity<?> returnBooking(@PathVariable Integer id, HttpSession session) {
        Account currentUser = (Account) session.getAttribute("loggedInUser");
        if (currentUser == null) return ResponseEntity.status(401).build();

        Booking booking = bookingRepository.findById(id).orElse(null);
        
        if (booking == null || !booking.getAccount().getMaSv().equals(currentUser.getMaSv())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Không hợp lệ!"));
        }

        // Nếu đang mượn thì cho phép TRẢ
        if ("ACTIVE".equals(booking.getTrangThai())) {
            
            // Đổi trạng thái phiếu mượn thành COMPLETED (Đã trả)
            booking.setTrangThai("COMPLETED");
            bookingRepository.save(booking);

            // Trả lại phòng (Đổi trạng thái phòng về 0 - Trống)
            Room room = booking.getRoom();
            if (room != null) {
                room.setTrangThai("0");
                roomRepository.save(room);
            }
            return ResponseEntity.ok(Map.of("message", "Trả phòng thành công!"));
        }

        return ResponseEntity.badRequest().body(Map.of("message", "Phiếu mượn này đã kết thúc!"));
    }
}