package vn.edu.eaut.ems.controller;

import java.util.List;
import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.edu.eaut.ems.entity.Building;
import vn.edu.eaut.ems.entity.Room;
import vn.edu.eaut.ems.repository.BuildingRepository;
import vn.edu.eaut.ems.repository.RoomRepository;

@Controller
public class AdminRoomController {

    private final RoomRepository roomRepository;
    private final BuildingRepository buildingRepository;

    public AdminRoomController(RoomRepository roomRepository, BuildingRepository buildingRepository) {
        this.roomRepository = roomRepository;
        this.buildingRepository = buildingRepository;
    }

    @GetMapping("/admin/rooms")
    public String getRoomManagement(
            @RequestParam(value = "toaNha", required = false) String toaNha,
            @RequestParam(value = "tang", required = false) Integer tang,
            Model model) {

        List<Room> allRooms = roomRepository.findAll(
                Sort.by("building.maToaNha").ascending().and(Sort.by("maPhong").ascending()));

        List<Integer> floors = allRooms.stream()
                .map(Room::getTang)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        List<Room> filteredRooms = allRooms.stream()
                .filter(room -> toaNha == null || toaNha.isBlank()
                        || (room.getBuilding() != null
                                && toaNha.equalsIgnoreCase(room.getBuilding().getMaToaNha())))
                .filter(room -> tang == null || tang.equals(room.getTang()))
                .toList();

        long availableRooms = allRooms.stream().filter(this::isAvailable).count();
        long occupiedRooms = allRooms.stream().filter(this::isOccupied).count();
        long maintenanceRooms = allRooms.stream().filter(this::isMaintenance).count();

        List<Building> buildings = buildingRepository.findAll(Sort.by("maToaNha").ascending());

        model.addAttribute("rooms", filteredRooms);
        model.addAttribute("buildings", buildings);
        model.addAttribute("floors", floors);
        model.addAttribute("selectedBuilding", toaNha == null ? "" : toaNha);
        model.addAttribute("selectedFloor", tang);
        model.addAttribute("filteredRoomCount", filteredRooms.size());
        model.addAttribute("totalRooms", allRooms.size());
        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("occupiedRooms", occupiedRooms);
        model.addAttribute("maintenanceRooms", maintenanceRooms);

        return "admin/rooms";
    }

    @PostMapping("/admin/rooms/create")
    public String createRoom(@RequestParam("maPhong") String maPhong,
            @RequestParam("tenPhong") String tenPhong,
            @RequestParam("sucChua") Integer sucChua,
            @RequestParam("toaNha") String toaNha,
            @RequestParam(value = "trangThai", defaultValue = "0") String trangThai,
            RedirectAttributes redirectAttributes) {

        String normalizedCode = maPhong.trim().toUpperCase();
        if (roomRepository.existsById(normalizedCode)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Mã phòng [" + normalizedCode + "] đã tồn tại!");
            return "redirect:/admin/rooms";
        }

        Building building = buildingRepository.findById(toaNha).orElse(null);
        if (building == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tòa nhà được chọn không tồn tại!");
            return "redirect:/admin/rooms";
        }

        roomRepository.save(new Room(normalizedCode, tenPhong.trim(), sucChua, trangThai, building));
        redirectAttributes.addFlashAttribute("successMessage",
                "Thêm phòng học [" + normalizedCode + "] thành công!");
        return "redirect:/admin/rooms";
    }

    @PostMapping("/admin/rooms/update")
    public String updateRoom(@RequestParam("maPhong") String maPhong,
            @RequestParam("tenPhong") String tenPhong,
            @RequestParam("sucChua") Integer sucChua,
            @RequestParam("toaNha") String toaNha,
            @RequestParam("trangThai") String trangThai,
            RedirectAttributes redirectAttributes) {

        Room room = roomRepository.findById(maPhong).orElse(null);
        Building building = buildingRepository.findById(toaNha).orElse(null);
        if (room == null || building == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy phòng hoặc tòa nhà cần cập nhật!");
            return "redirect:/admin/rooms";
        }

        room.setTenPhong(tenPhong.trim());
        room.setSucChua(sucChua);
        room.setTrangThai(trangThai);
        room.setBuilding(building);
        roomRepository.save(room);

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phòng [" + maPhong + "] thành công!");
        return "redirect:/admin/rooms";
    }

    @PostMapping("/admin/rooms/delete")
    public String deleteRoom(@RequestParam("maPhong") String maPhong, RedirectAttributes redirectAttributes) {
        if (!roomRepository.existsById(maPhong)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy phòng cần xóa!");
            return "redirect:/admin/rooms";
        }

        try {
            roomRepository.deleteById(maPhong);
            roomRepository.flush();
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa phòng [" + maPhong + "] thành công!");
        } catch (DataIntegrityViolationException exception) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể xóa phòng [" + maPhong + "] vì đang có dữ liệu mượn/trả liên quan.");
        }
        return "redirect:/admin/rooms";
    }

    private boolean isAvailable(Room room) {
        return hasStatus(room, "0", "KHA_DUNG", "HOAT_DONG");
    }

    private boolean isOccupied(Room room) {
        return hasStatus(room, "1", "DANG_DUNG");
    }

    private boolean isMaintenance(Room room) {
        return hasStatus(room, "2", "BAO_TRI");
    }

    private boolean hasStatus(Room room, String... statuses) {
        if (room.getTrangThai() == null) {
            return false;
        }
        for (String status : statuses) {
            if (status.equalsIgnoreCase(room.getTrangThai())) {
                return true;
            }
        }
        return false;
    }
}
