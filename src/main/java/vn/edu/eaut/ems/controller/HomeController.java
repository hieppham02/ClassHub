package vn.edu.eaut.ems.controller;

import java.util.*;
import java.util.stream.*;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import vn.edu.eaut.ems.entity.Account;
import vn.edu.eaut.ems.entity.Booking;
import vn.edu.eaut.ems.entity.Building;
import vn.edu.eaut.ems.repository.BuildingRepository;
import vn.edu.eaut.ems.repository.BookingRepository;

@Controller // Lưu ý: Dùng @Controller, KHÔNG dùng @RestController
public class HomeController {

    private final BuildingRepository buildingRepository;
    private final BookingRepository bookingRepository;

    public HomeController(BuildingRepository buildingRepository, BookingRepository bookingRepository) {
        this.buildingRepository = buildingRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/")
    public String showIndex(HttpSession session, Model model) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login"; 
        }

        List<Building> buildingList = buildingRepository.findAll();
        model.addAttribute("buildings", buildingList);
        return "index";
    }

    @GetMapping("/lich-su")
    public String showHistoryPage(HttpSession session, Model model) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        return "history"; 
    }

    @GetMapping("/api/lich-su")
    @ResponseBody // BẮT BUỘC có dòng này để báo cho Java biết đây là API nhả JSON
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
}
