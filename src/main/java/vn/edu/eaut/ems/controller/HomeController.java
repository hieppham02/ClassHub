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

    // 1. TRANG CHỦ
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

        boolean hasActiveBooking = false;
        String activeRoomName = "";
        if (currentUser != null) {
            final String maSv = currentUser.getMaSv();
            Optional<Booking> activeOpt = bookingRepository.findAll().stream()
                    .filter(b -> b.getAccount() != null && b.getAccount().getMaSv().equalsIgnoreCase(maSv))
                    .filter(b -> "CHO_DUYET".equalsIgnoreCase(b.getTrangThai()) || 
                                 "DA_DUYET".equalsIgnoreCase(b.getTrangThai()) || 
                                 "UY_QUYEN".equalsIgnoreCase(b.getTrangThai()))
                    .findFirst();

            if (activeOpt.isPresent()) {
                hasActiveBooking = true;
                activeRoomName = (activeOpt.get().getRoom() != null) ? activeOpt.get().getRoom().getTenPhong() : "đang mượn";
            }
        }
        model.addAttribute("hasActiveBooking", hasActiveBooking);
        model.addAttribute("activeRoomName", activeRoomName);

        List<Building> buildingList = buildingRepository.findAll();
        model.addAttribute("buildings", buildingList);
        return "index";
    }

    // 2. API TIẾP NHẬN ĐĂNG KÝ MƯỢN
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

        // [LUẬT 1: CHẶN SINH VIÊN ĐANG MƯỢN]
        final String userMaSv = currentUser.getMaSv();
        boolean isStudentBusy = bookingRepository.findAll().stream().anyMatch(b ->
            b.getAccount() != null &&
            b.getAccount().getMaSv().equalsIgnoreCase(userMaSv) &&
            ("CHO_DUYET".equalsIgnoreCase(b.getTrangThai()) || 
             "DA_DUYET".equalsIgnoreCase(b.getTrangThai()) || 
             "UY_QUYEN".equalsIgnoreCase(b.getTrangThai()))
        );

        if (isStudentBusy) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Bạn đang có phòng mượn chưa trả (hoặc đơn đang chờ duyệt). Vui lòng trả phòng cũ trước khi mượn thêm!"
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

        // [LUẬT 2: CHẶN PHÒNG ĐÃ CÓ NGƯỜI MƯỢN TRONG CA]
        final LocalDate targetDate = ngayMuon;
        final Integer targetCa = caMuon;
        boolean isRoomBusy = bookingRepository.findAll().stream().anyMatch(b -> 
            b.getRoom() != null && 
            b.getRoom().getMaPhong().replace("-", "").equalsIgnoreCase(cleanMaPhong) &&
            b.getNgayMuon() != null && b.getNgayMuon().isEqual(targetDate) &&
            b.getCaMuon() != null && b.getCaMuon().equals(targetCa) &&
            ("CHO_DUYET".equalsIgnoreCase(b.getTrangThai()) || 
             "DA_DUYET".equalsIgnoreCase(b.getTrangThai()) || 
             "UY_QUYEN".equalsIgnoreCase(b.getTrangThai()))
        );

        if (isRoomBusy) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Phòng " + room.getTenPhong() + " đã có người mượn hoặc đang chờ duyệt trong ca này rồi!"
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

    // 3. TRANG LỊCH SỬ MƯỢN
    @GetMapping("/lich-su")
    public String showHistoryPage(HttpSession session, Model model) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        return "history"; 
    }

    // 4. API LẤY LỊCH SỬ MƯỢN
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
            buildingData.put("tenToaNha", b.getRoom().getBuilding().getTenToaNha());

            roomData.put("building", buildingData);
            record.put("room", roomData);

            return record;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(responseData);
    }

    // 5. API ỦY QUYỀN TRẢ PHÒNG
    @PostMapping("/api/bookings/{id}/delegate")
    @ResponseBody
    public ResponseEntity<?> delegateBooking(@PathVariable("id") Integer id,
                                             @RequestParam("maSv") String maSv,
                                             HttpSession session) {

        Booking oldBooking = bookingRepository.findById(id).orElse(null);
        if (oldBooking == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false, 
                "message", "Không tìm thấy phiếu mượn này!"
            ));
        }

        if ("DA_UY_QUYEN".equalsIgnoreCase(oldBooking.getTrangThai()) || "UY_QUYEN".equalsIgnoreCase(oldBooking.getTrangThai())) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false, 
                "message", "Đơn này đã được ủy quyền, không thể ủy quyền tiếp!"
            ));
        }

        Account newStudent = accountRepository.findById(maSv.trim()).orElse(null);
        if (newStudent == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false, 
                "message", "Không tìm thấy sinh viên có mã: " + maSv
            ));
        }

        oldBooking.setTrangThai("DA_UY_QUYEN");
        bookingRepository.save(oldBooking);

        Booking newBooking = new Booking();
        newBooking.setAccount(newStudent);
        newBooking.setRoom(oldBooking.getRoom());
        newBooking.setNgayMuon(oldBooking.getNgayMuon());
        newBooking.setCaMuon(oldBooking.getCaMuon());
        newBooking.setTrangThai("UY_QUYEN"); // Đơn cho người nhận
        bookingRepository.save(newBooking);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Đã ủy quyền thành công cho bạn " + newStudent.getHoTen() + " (" + newStudent.getMaSv() + ")!"
        ));
    }

    // =========================================================================
    // 6. [CẬP NHẬT] XÁC NHẬN TRẢ: NẾU LÀ ĐƠN ỦY QUYỀN THÌ GÁN LÀ "DA_TRA_HO"
    // =========================================================================
    @PostMapping("/api/bookings/{id}/return")
    @ResponseBody
    public ResponseEntity<?> returnBookingApi(@PathVariable("id") Integer id) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking != null) {
            // NẾU LÀ ĐƠN ĐƯỢC ỦY QUYỀN -> ĐÁNH DẤU TRẠNG THÁI LÀ DA_TRA_HO (TRẢ HỘ)
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