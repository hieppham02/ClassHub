package vn.edu.eaut.ems.controller;

import java.time.LocalDate;
import java.util.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.eaut.ems.entity.*;
import vn.edu.eaut.ems.repository.*;
import vn.edu.eaut.ems.service.MqttService;
import jakarta.servlet.http.HttpSession;


@RestController
@RequestMapping("/api")
public class BookingController {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final MqttService mqttService;
    private int currentOtp;

    public BookingController(RoomRepository roomRepository, BookingRepository bookingRepository, MqttService mqttService) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.mqttService = mqttService;
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

        Room room = roomRepository.findById(request.getMaPhong()).orElse(null);
        if (room == null || !room.getTrangThai().equals("0")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Phòng không tồn tại hoặc đã có người mượn!"));
        }

        room.setTrangThai("1");
        roomRepository.save(room);

        Booking booking = new Booking();
        booking.setAccount(currentUser);
        booking.setRoom(room);
        booking.setNgayMuon(request.getNgayMuon());
        booking.setCaMuon(request.getCaMuon());
        booking.setTrangThai("ACTIVE");
        bookingRepository.save(booking);

        booking.setTrangThai("ACTIVE");
        
        String randomOtp = String.format("%06d", new Random().nextInt(999999));
        booking.setOtp(randomOtp);
        bookingRepository.save(booking);
        // otp
        System.out.println("Mã OTP: " + randomOtp);
        String command = "{\"otp\": " + randomOtp + "}";
        mqttService.sendCommandToESP32("otp", command);

        currentOtp = Integer.parseInt(randomOtp);

        return ResponseEntity.ok(Map.of("message", "Đăng ký mượn phòng thành công!"));
    }

    @PostMapping("/bookings/{id}/return")
    public ResponseEntity<?> returnBooking(@PathVariable Integer id, HttpSession session) {
        Account currentUser = (Account) session.getAttribute("loggedInUser");
        if (currentUser == null) return ResponseEntity.status(401).build();

        Booking booking = bookingRepository.findById(id).orElse(null);
        
        if (booking == null || !booking.getAccount().getMaSv().equals(currentUser.getMaSv())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Không hợp lệ!"));
        }

        if ("ACTIVE".equals(booking.getTrangThai())) {
            
            booking.setTrangThai("COMPLETED");
            bookingRepository.save(booking);

            Room room = booking.getRoom();
            if (room != null) {
                room.setTrangThai("0");
                roomRepository.save(room);
            }
            return ResponseEntity.ok(Map.of("message", "Trả phòng thành công!"));
        }

        return ResponseEntity.badRequest().body(Map.of("message", "Phiếu mượn này đã kết thúc!"));
    }

    @PostMapping("/bookings/{id}/refresh-otp")
    @ResponseBody
    public ResponseEntity<?> refreshOtp(@PathVariable Integer id, HttpSession session) {
        Account currentUser = (Account) session.getAttribute("loggedInUser");
        if (currentUser == null) return ResponseEntity.status(401).build();

        Booking booking = bookingRepository.findById(id).orElse(null);
        // if (booking == null || !booking.getAccount().getMaSv().equals(currentUser.getMaSv()) || !"ACTIVE".equals(booking.getTrangThai())) {   
        //     return ResponseEntity.badRequest().body(Map.of("message", "Không thể làm mới mã!"));
        // }

        String newOtp = String.format("%06d", new Random().nextInt(999999));
        booking.setOtp(newOtp);
        bookingRepository.save(booking);
        System.out.println("Mã OTP: " + newOtp);
        currentOtp = Integer.parseInt(newOtp);

        String command = "{\"otp\": " + newOtp + "}";
        mqttService.sendCommandToESP32("otp", command);

        return ResponseEntity.ok(Map.of("newOtp", newOtp));
    }

    @PostMapping("/bookings/{id}/verify-otp")
    @ResponseBody
    public ResponseEntity<?> verifyOtp(@PathVariable Integer id, @RequestBody Map<String, String> request, HttpSession session) {
        Account currentUser = (Account) session.getAttribute("loggedInUser");
        if (currentUser == null) return ResponseEntity.status(401).build();

        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null || !booking.getAccount().getMaSv().equals(currentUser.getMaSv())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Lỗi!"));
        }

        String inputOtp = request.get("otp");
        if (String.valueOf(currentOtp).equals(inputOtp) && booking.getOtp().equals(inputOtp)) {
            String value  = "open";
            String command = "{\"action\": " + value + "}";
            mqttService.sendCommandToESP32("action", command);
            System.out.println(command);
            return ResponseEntity.ok(Map.of("message", "Mã chính xác"));
        }

        return ResponseEntity.badRequest().body(Map.of("message", "Mã OTP không chính xác!"));
    }
}