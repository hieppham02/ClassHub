package vn.edu.eaut.ems.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.eaut.ems.entity.Building;
import vn.edu.eaut.ems.entity.Room;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AdminRoomController {

    private final List<Room> roomList = new ArrayList<>();

    // Helper tạo nhanh Room và Building
    private Room createRoom(String maPhong, String tenPhong, Integer sucChua, String trangThai, String tenToaNha) {
        Building b = new Building();
        b.setTenToaNha(tenToaNha);
        return new Room(maPhong, tenPhong, sucChua, trangThai, b);
    }

    // Khởi tạo danh sách phòng mẫu với các tòa nhà mới
    @PostConstruct
    public void initMockData() {
        roomList.add(createRoom("P401", "Phòng học 401", 50, "KHA_DUNG", "Polyco"));
        roomList.add(createRoom("P402", "Phòng học 402", 50, "DANG_DUNG", "Polyco"));
        roomList.add(createRoom("LAB302", "Lab AI & IoT", 40, "DANG_DUNG", "Đinh Trọng Dật"));
        roomList.add(createRoom("LAB301", "Phòng Máy Tính 1", 45, "KHA_DUNG", "Thuận Thành"));
        roomList.add(createRoom("HT01", "Hội Trường Lớn", 300, "KHA_DUNG", "EAUT"));
        roomList.add(createRoom("XTH01", "Xưởng Thực Hành 1", 60, "BAO_TRI", "VIỆT NAM "));
    }

    // 1. HIỂN THỊ DANH SÁCH & THỐNG KÊ
    @GetMapping("/admin/rooms")
    public String getRoomManagement(Model model) {
        long totalRooms = roomList.size();
        long availableRooms = roomList.stream().filter(r -> "KHA_DUNG".equalsIgnoreCase(r.getTrangThai())).count();
        long occupiedRooms = roomList.stream().filter(r -> "DANG_DUNG".equalsIgnoreCase(r.getTrangThai())).count();
        long maintenanceRooms = roomList.stream().filter(r -> "BAO_TRI".equalsIgnoreCase(r.getTrangThai())).count();

        model.addAttribute("rooms", roomList);
        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("occupiedRooms", occupiedRooms);
        model.addAttribute("maintenanceRooms", maintenanceRooms);

        return "admin/rooms";
    }

    // 2. THÊM MỚI PHÒNG HỌC
    @PostMapping("/admin/rooms/create")
    public String createRoom(@RequestParam("maPhong") String maPhong,
                             @RequestParam("tenPhong") String tenPhong,
                             @RequestParam("sucChua") Integer sucChua,
                             @RequestParam("toaNha") String toaNha,
                             @RequestParam(value = "trangThai", defaultValue = "KHA_DUNG") String trangThai,
                             RedirectAttributes redirectAttributes) {

        boolean exists = roomList.stream().anyMatch(r -> r.getMaPhong().equalsIgnoreCase(maPhong.trim()));
        if (exists) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mã phòng [" + maPhong + "] đã tồn tại!");
            return "redirect:/admin/rooms";
        }

        Room newRoom = createRoom(maPhong.trim(), tenPhong, sucChua, trangThai, toaNha);
        roomList.add(0, newRoom);

        redirectAttributes.addFlashAttribute("successMessage", "Thêm mới phòng học [" + maPhong + "] tại tòa " + toaNha + " thành công!");
        return "redirect:/admin/rooms";
    }

    // 3. CHỈNH SỬA PHÒNG HỌC
    @PostMapping("/admin/rooms/update")
    public String updateRoom(@RequestParam("maPhong") String maPhong,
                             @RequestParam("tenPhong") String tenPhong,
                             @RequestParam("sucChua") Integer sucChua,
                             @RequestParam("toaNha") String toaNha,
                             @RequestParam("trangThai") String trangThai,
                             RedirectAttributes redirectAttributes) {

        for (Room r : roomList) {
            if (r.getMaPhong().equalsIgnoreCase(maPhong)) {
                r.setTenPhong(tenPhong);
                r.setSucChua(sucChua);
                r.setTrangThai(trangThai);
                if (r.getBuilding() == null) {
                    r.setBuilding(new Building());
                }
                r.getBuilding().setTenToaNha(toaNha);
                break;
            }
        }

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phòng [" + maPhong + "] thành công!");
        return "redirect:/admin/rooms";
    }

    // 4. XÓA PHÒNG HỌC
    @PostMapping("/admin/rooms/delete")
    public String deleteRoom(@RequestParam("maPhong") String maPhong, RedirectAttributes redirectAttributes) {
        boolean removed = roomList.removeIf(r -> r.getMaPhong().equalsIgnoreCase(maPhong));
        if (removed) {
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa phòng [" + maPhong + "] thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy phòng cần xóa!");
        }
        return "redirect:/admin/rooms";
    }
}