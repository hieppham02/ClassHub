package vn.edu.eaut.ems.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.eaut.ems.entity.Booking;
import vn.edu.eaut.ems.entity.Building;
import vn.edu.eaut.ems.repository.BookingRepository;

import java.util.List;

@Controller
public class AdminHistoryController {

    private final BookingRepository bookingRepository;

    public AdminHistoryController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    private void fixBuildingForBooking(Booking b) {
        if (b.getRoom() != null) {
            String code = (b.getRoom().getMaPhong() != null ? b.getRoom().getMaPhong() : b.getRoom().getTenPhong()).toUpperCase();
            Building bld = new Building();
            if (code.startsWith("EAUT")) {
                bld.setTenToaNha("EAUT");
            } else if (code.startsWith("PLC") || code.contains("POLYCO")) {
                bld.setTenToaNha("POLYCO");
            } else if (code.startsWith("TT") || code.contains("THUẬN THÀNH")) {
                bld.setTenToaNha("Thuận Thành");
            } else if (code.startsWith("VNB") || code.contains("VIỆT NAM")) {
                bld.setTenToaNha("Việt Nam Building");
            } else {
                bld.setTenToaNha("Đinh Trọng Dật");
            }
            b.getRoom().setBuilding(bld);
        }
    }

    @GetMapping("/admin/history")
    public String getHistory(Model model) {
        List<Booking> allBookings = bookingRepository.findAllByOrderByThoiGianTaoDesc();

        for (Booking b : allBookings) {
            fixBuildingForBooking(b);
        }

        long totalLogs = allBookings.size();
        long completedLogs = allBookings.stream()
                .filter(b -> "DA_TRA".equalsIgnoreCase(b.getTrangThai()) || "DA_TRA_HO".equalsIgnoreCase(b.getTrangThai())).count();
        long delegatedLogs = allBookings.stream()
                .filter(b -> "DA_UY_QUYEN".equalsIgnoreCase(b.getTrangThai()) || "UY_QUYEN".equalsIgnoreCase(b.getTrangThai())).count();
        long rejectedLogs = allBookings.stream()
                .filter(b -> "TU_CHOI".equalsIgnoreCase(b.getTrangThai())).count();

        model.addAttribute("historyList", allBookings);
        model.addAttribute("totalLogs", totalLogs);
        model.addAttribute("completedLogs", completedLogs);
        model.addAttribute("delegatedLogs", delegatedLogs);
        model.addAttribute("rejectedLogs", rejectedLogs);

        return "admin/history";
    }

    @PostMapping("/admin/history/delete")
    public String deleteHistory(@RequestParam("id") Integer id, RedirectAttributes redirectAttributes) {
        if (bookingRepository.existsById(id)) {
            bookingRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa bản ghi lịch sử #" + id + " khỏi CSDL!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy bản ghi lịch sử để xóa!");
        }
        return "redirect:/admin/history";
    }
}