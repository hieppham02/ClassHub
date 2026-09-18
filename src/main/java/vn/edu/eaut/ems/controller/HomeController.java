package vn.edu.eaut.ems.controller;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.*;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import vn.edu.eaut.ems.entity.Account;
import vn.edu.eaut.ems.entity.Booking;
import vn.edu.eaut.ems.entity.Building;
import vn.edu.eaut.ems.entity.Room;
import vn.edu.eaut.ems.repository.AccountRepository;
import vn.edu.eaut.ems.repository.BuildingRepository;
import vn.edu.eaut.ems.repository.BookingRepository;
import vn.edu.eaut.ems.repository.RoomRepository;

@Controller
public class HomeController {

    private final BuildingRepository buildingRepository;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final AccountRepository accountRepository;

    public HomeController(BuildingRepository buildingRepository, 
                          BookingRepository bookingRepository,
                          RoomRepository roomRepository,
                          AccountRepository accountRepository) {
        this.buildingRepository = buildingRepository;
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.accountRepository = accountRepository;
    }

    @GetMapping("/")
    public String showIndex(HttpSession session, Model model) {
        Account currentUser = (Account) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            currentUser = accountRepository.findById("20231049").orElse(
                accountRepository.findById("20231206").orElse(null)
            );
            session.setAttribute("loggedInUser", currentUser);
        }
        model.addAttribute("currentUser", currentUser);

        List<Building> buildingList = buildingRepository.findAll();
        model.addAttribute("buildings", buildingList);
        return "index";
    }

    @PostMapping({"/api/bookings", "/api/booking/register"})
    @ResponseBody
    public ResponseEntity<?> handleBookingApi(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(required = false) Map<String, String> params,
            HttpSession session) {

        Map<String, Object> requestData = new HashMap<>();
        if (params != null) requestData.putAll(params);
        if (body != null) requestData.putAll(body);

        String maPhong = null;
        if (requestData.containsKey("maPhong")) maPhong = String.valueOf(requestData.get("maPhong"));
        else if (requestData.containsKey("roomId")) maPhong = String.valueOf(requestData.get("roomId"));
        else if (requestData.containsKey("tenPhong")) maPhong = String.valueOf(requestData.get("tenPhong"));

        if (maPhong == null || maPhong.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thiếu mã phòng học!"));
        }

        Integer caMuon = 1;
        try {
            Object caObj = requestData.getOrDefault("caMuon", requestData.getOrDefault("caHoc", 1));
            String caStr = String.valueOf(caObj).replaceAll("\\D+", "");
            if (!caStr.isEmpty()) {
                caMuon = Integer.parseInt(caStr);
            }
        } catch (Exception ignored) {}

        LocalDate ngayMuon = LocalDate.now();
        if (requestData.containsKey("ngayMuon")) {
            try {
                ngayMuon = LocalDate.parse(String.valueOf(requestData.get("ngayMuon")));
            } catch (Exception ignored) {}
        }

        Account currentUser = (Account) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            currentUser = accountRepository.findById("20231049").orElse(
                accountRepository.findById("20231206").orElse(null)
            );
        }

        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Vui lòng đăng nhập để mượn phòng!"));
        }

        final LocalDate targetDate = ngayMuon;
        final Integer targetCa = caMuon;
        final String userMaSv = currentUser.getMaSv();

        // 1. CHỈ CHẶN NẾU TRÙNG CÙNG CA HỌC ĐÓ (Cho phép sinh viên mượn ca khác để test nhiều tòa)
        boolean isStudentBusyInShift = bookingRepository.findAll().stream().anyMatch(b ->
            b.getAccount() != null &&
            b.getAccount().getMaSv().equalsIgnoreCase(userMaSv) &&
            b.getNgayMuon() != null && b.getNgayMuon().isEqual(targetDate) &&
            b.getCaMuon() != null && b.getCaMuon().equals(targetCa) &&
            !"DA_TRA".equalsIgnoreCase(b.getTrangThai()) && 
            !"DA_TRA_HO".equalsIgnoreCase(b.getTrangThai()) &&
            !"TU_CHOI".equalsIgnoreCase(b.getTrangThai())
        );

        if (isStudentBusyInShift) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Bạn đã có phòng mượn trong Ca " + targetCa + " ngày hôm nay rồi! Vui lòng chọn Ca học khác."
            ));
        }

        String cleanMaPhong = maPhong.replace("-", "").trim();
        Room room = roomRepository.findById(cleanMaPhong)
                .orElse(roomRepository.findById(maPhong).orElse(null));

        if (room == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Không tìm thấy phòng " + maPhong + " trong hệ thống!"
            ));
        }

        // 2. CHẶN NẾU PHÒNG ĐÓ ĐÃ CÓ NGƯỜI KHÁC MƯỢN TRONG CA ĐÓ
        boolean isRoomBusy = bookingRepository.findAll().stream().anyMatch(b -> 
            b.getRoom() != null && 
            b.getRoom().getMaPhong().replace("-", "").equalsIgnoreCase(cleanMaPhong) &&
            b.getNgayMuon() != null && b.getNgayMuon().isEqual(targetDate) &&
            b.getCaMuon() != null && b.getCaMuon().equals(targetCa) &&
            !"DA_TRA".equalsIgnoreCase(b.getTrangThai()) && 
            !"DA_TRA_HO".equalsIgnoreCase(b.getTrangThai()) &&
            !"TU_CHOI".equalsIgnoreCase(b.getTrangThai())
        );

        if (isRoomBusy) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Phòng " + room.getTenPhong() + " đã có người mượn trong Ca " + targetCa + " rồi!"
            ));
        }

        Booking booking = new Booking();
        booking.setAccount(currentUser);
        booking.setRoom(room);
        booking.setNgayMuon(targetDate);
        booking.setCaMuon(targetCa);
        booking.setTrangThai("CHO_DUYET");

        bookingRepository.save(booking);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Đăng ký mượn phòng " + room.getTenPhong() + " thành công! Vui lòng chờ Admin duyệt."
        ));
    }

    @GetMapping("/lich-su")
    public String showHistoryPage(HttpSession session, Model model) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        return "history"; 
    }

    @GetMapping("/api/lich-su")
    @ResponseBody
    public ResponseEntity<?> getMyHistoryApi(HttpSession session) {
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
            String toaNha = "Đinh Trọng Dật";
            String code = b.getRoom().getMaPhong().toUpperCase();
            if (code.startsWith("EAUT")) toaNha = "EAUT";
            else if (code.startsWith("PLC")) toaNha = "POLYCO";
            else if (code.startsWith("TT")) toaNha = "Thuận Thành";
            else if (code.startsWith("VNB")) toaNha = "Việt Nam Building";

            buildingData.put("tenToaNha", toaNha);
            roomData.put("building", buildingData);
            record.put("room", roomData);

            return record;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/api/bookings/{id}/delegate")
    @ResponseBody
    public ResponseEntity<?> delegateBooking(@PathVariable("id") Integer id,
                                             @RequestParam("maSv") String maSv,
                                             HttpSession session) {

        Booking oldBooking = bookingRepository.findById(id).orElse(null);
        if (oldBooking == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy phiếu mượn này!"));
        }

        if ("DA_UY_QUYEN".equalsIgnoreCase(oldBooking.getTrangThai()) || "UY_QUYEN".equalsIgnoreCase(oldBooking.getTrangThai())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Đơn này đã được ủy quyền, không thể ủy quyền tiếp!"));
        }

        Account newStudent = accountRepository.findById(maSv.trim()).orElse(null);
        if (newStudent == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy sinh viên có mã: " + maSv));
        }

        oldBooking.setTrangThai("DA_UY_QUYEN");
        bookingRepository.save(oldBooking);

        Booking newBooking = new Booking();
        newBooking.setAccount(newStudent);
        newBooking.setRoom(oldBooking.getRoom());
        newBooking.setNgayMuon(oldBooking.getNgayMuon());
        newBooking.setCaMuon(oldBooking.getCaMuon());
        newBooking.setTrangThai("UY_QUYEN");
        bookingRepository.save(newBooking);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Đã ủy quyền thành công cho bạn " + newStudent.getHoTen() + " (" + newStudent.getMaSv() + ")!"
        ));
    }

    @PostMapping("/api/bookings/{id}/return")
    @ResponseBody
    public ResponseEntity<?> returnBookingApi(@PathVariable("id") Integer id) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking != null) {
            if ("UY_QUYEN".equalsIgnoreCase(booking.getTrangThai())) {
                booking.setTrangThai("DA_TRA_HO");
            } else {
                booking.setTrangThai("DA_TRA");
            }
            bookingRepository.save(booking);
            return ResponseEntity.ok(Map.of("success", true, "message", "Trả thiết bị thành công!"));
        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy phiếu mượn!"));
    }
}