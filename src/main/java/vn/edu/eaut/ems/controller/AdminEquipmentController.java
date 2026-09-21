package vn.edu.eaut.ems.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.eaut.ems.entity.Equipment;
import vn.edu.eaut.ems.entity.Room;
import vn.edu.eaut.ems.entity.RoomEquipment;
import vn.edu.eaut.ems.entity.RoomEquipmentId;
import vn.edu.eaut.ems.repository.BuildingRepository;
import vn.edu.eaut.ems.repository.EquipmentRepository;
import vn.edu.eaut.ems.repository.RoomEquipmentRepository;
import vn.edu.eaut.ems.repository.RoomRepository;

import java.util.Comparator;
import java.util.List;

@Controller
public class AdminEquipmentController {

    private final EquipmentRepository equipmentRepository;
    private final RoomEquipmentRepository roomEquipmentRepository;
    private final RoomRepository roomRepository;
    private final BuildingRepository buildingRepository;

    public AdminEquipmentController(EquipmentRepository equipmentRepository,
                                    RoomEquipmentRepository roomEquipmentRepository,
                                    RoomRepository roomRepository,
                                    BuildingRepository buildingRepository) {
        this.equipmentRepository = equipmentRepository;
        this.roomEquipmentRepository = roomEquipmentRepository;
        this.roomRepository = roomRepository;
        this.buildingRepository = buildingRepository;
    }

    @GetMapping("/admin/equipment")
    public String getEquipment(Model model) {
        List<Equipment> equipments = equipmentRepository.findAll().stream()
                .sorted(Comparator.comparing(Equipment::getTenThietBi, String.CASE_INSENSITIVE_ORDER))
                .toList();
        List<Room> rooms = roomRepository.findAll().stream()
                .sorted(Comparator.comparing(Room::getMaPhong, String.CASE_INSENSITIVE_ORDER))
                .toList();
        List<RoomEquipment> allocations = roomEquipmentRepository.findAll().stream()
                .sorted(Comparator.comparing((RoomEquipment item) -> item.getRoom().getMaPhong(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(RoomEquipment::getTenThietBi, String.CASE_INSENSITIVE_ORDER))
                .toList();

        int totalAllocatedQuantity = allocations.stream()
                .map(RoomEquipment::getSoLuong)
                .filter(quantity -> quantity != null)
                .mapToInt(Integer::intValue)
                .sum();
        long allocatedRooms = allocations.stream()
                .map(item -> item.getRoom().getMaPhong())
                .distinct()
                .count();

        model.addAttribute("equipments", equipments);
        model.addAttribute("rooms", rooms);
        model.addAttribute("buildings", buildingRepository.findAll());
        model.addAttribute("allocations", allocations);
        model.addAttribute("totalEquipmentTypes", equipments.size());
        model.addAttribute("totalAllocatedQuantity", totalAllocatedQuantity);
        model.addAttribute("allocatedRooms", allocatedRooms);
        return "admin/equipment";
    }

    @PostMapping("/admin/equipment/catalog/create")
    public String createEquipment(@RequestParam String tenThietBi, RedirectAttributes redirectAttributes) {
        String normalizedName = normalizeName(tenThietBi);
        if (normalizedName.isEmpty()) {
            return redirectWithError(redirectAttributes, "Tên thiết bị không được để trống.");
        }
        if (equipmentRepository.existsByTenThietBiIgnoreCase(normalizedName)) {
            return redirectWithError(redirectAttributes, "Thiết bị này đã tồn tại trong kho chung.");
        }

        Equipment equipment = new Equipment();
        equipment.setTenThietBi(normalizedName);
        equipmentRepository.save(equipment);
        redirectAttributes.addFlashAttribute("successMessage", "Đã thêm thiết bị vào kho chung.");
        return "redirect:/admin/equipment";
    }

    @PostMapping("/admin/equipment/catalog/update")
    public String updateEquipment(@RequestParam Integer id,
                                  @RequestParam String tenThietBi,
                                  RedirectAttributes redirectAttributes) {
        Equipment equipment = equipmentRepository.findById(id).orElse(null);
        if (equipment == null) {
            return redirectWithError(redirectAttributes, "Không tìm thấy thiết bị cần sửa.");
        }

        String normalizedName = normalizeName(tenThietBi);
        if (normalizedName.isEmpty()) {
            return redirectWithError(redirectAttributes, "Tên thiết bị không được để trống.");
        }
        if (equipmentRepository.findByTenThietBiIgnoreCase(normalizedName)
                .filter(existing -> !existing.getId().equals(id))
                .isPresent()) {
            return redirectWithError(redirectAttributes, "Tên thiết bị đã tồn tại trong kho chung.");
        }

        equipment.setTenThietBi(normalizedName);
        equipmentRepository.save(equipment);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật thiết bị trong kho chung.");
        return "redirect:/admin/equipment";
    }

    @PostMapping("/admin/equipment/catalog/delete")
    public String deleteEquipment(@RequestParam Integer id, RedirectAttributes redirectAttributes) {
        if (!equipmentRepository.existsById(id)) {
            return redirectWithError(redirectAttributes, "Thiết bị không còn tồn tại.");
        }
        if (roomEquipmentRepository.existsByEquipment_Id(id)) {
            return redirectWithError(redirectAttributes, "Không thể xóa vì thiết bị đang được phân bổ cho phòng học.");
        }
        try {
            equipmentRepository.deleteById(id);
        } catch (DataIntegrityViolationException exception) {
            return redirectWithError(redirectAttributes, "Không thể xóa vì thiết bị đang được sử dụng.");
        }
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa thiết bị khỏi kho chung.");
        return "redirect:/admin/equipment";
    }

    @PostMapping("/admin/equipment/allocation/save")
    public String saveAllocation(@RequestParam String maPhong,
                                 @RequestParam Integer thietBiId,
                                 @RequestParam Integer soLuong,
                                 RedirectAttributes redirectAttributes) {
        Room room = roomRepository.findById(maPhong).orElse(null);
        Equipment equipment = equipmentRepository.findById(thietBiId).orElse(null);
        if (room == null || equipment == null) {
            return redirectWithError(redirectAttributes, "Phòng học hoặc thiết bị không tồn tại.");
        }
        if (soLuong == null || soLuong < 1) {
            return redirectWithError(redirectAttributes, "Số lượng phân bổ phải lớn hơn 0.");
        }

        RoomEquipmentId allocationId = new RoomEquipmentId(room.getMaPhong(), equipment.getId());
        RoomEquipment allocation = roomEquipmentRepository.findById(allocationId).orElseGet(RoomEquipment::new);
        allocation.setId(allocationId);
        allocation.setRoom(room);
        allocation.setEquipment(equipment);
        allocation.setSoLuong(soLuong);
        roomEquipmentRepository.save(allocation);

        redirectAttributes.addFlashAttribute("successMessage", "Đã lưu phân bổ thiết bị cho phòng " + room.getMaPhong() + ".");
        return "redirect:/admin/equipment?room=" + room.getMaPhong();
    }

    @PostMapping("/admin/equipment/allocation/delete")
    public String deleteAllocation(@RequestParam String maPhong,
                                   @RequestParam Integer thietBiId,
                                   RedirectAttributes redirectAttributes) {
        RoomEquipmentId allocationId = new RoomEquipmentId(maPhong, thietBiId);
        if (roomEquipmentRepository.existsById(allocationId)) {
            roomEquipmentRepository.deleteById(allocationId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã gỡ thiết bị khỏi phòng " + maPhong + ".");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy phân bổ cần xóa.");
        }
        return "redirect:/admin/equipment?room=" + maPhong;
    }

    private String normalizeName(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    private String redirectWithError(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("errorMessage", message);
        return "redirect:/admin/equipment";
    }

}
