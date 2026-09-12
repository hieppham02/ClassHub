package vn.edu.eaut.ems.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.eaut.ems.entity.Booking;
import vn.edu.eaut.ems.repository.BookingRepository;

import java.util.List;

@Controller
public class AdminBookingController {

    private final BookingRepository bookingRepository;

    public AdminBookingController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // 1. HIỂN THỊ DANH SÁCH TẤT CẢ YÊU CẦU MƯỢN TỪ CSDL
    @GetMapping("/admin/bookings")
    public String getBookingManagement(Model model) {
        List<Booking> bookings = bookingRepository.findAllByOrderByThoiGianTaoDesc();

        // Thống kê các trạng thái
        long totalBookings = bookings.size();
        long pendingBookings = bookings.stream()
                .filter(b -> "CHO_DUYET".equalsIgnoreCase(b.getTrangThai())).count();
        long approvedBookings = bookings.stream()
                .filter(b -> "DA_DUYET".equalsIgnoreCase(b.getTrangThai())).count();
        long completedBookings = bookings.stream()
                .filter(b -> "DA_TRA".equalsIgnoreCase(b.getTrangThai())).count();
        long delegatedBookings = bookings.stream()
                .filter(b -> "DA_UY_QUYEN".equalsIgnoreCase(b.getTrangThai()) || "UY_QUYEN".equalsIgnoreCase(b.getTrangThai())).count();

        model.addAttribute("bookings", bookings);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("approvedBookings", approvedBookings);
        model.addAttribute("completedBookings", completedBookings);
        model.addAttribute("delegatedBookings", delegatedBookings);

        return "admin/bookings";
    }

    // 2. DUYỆT ĐƠN MƯỢN
    @PostMapping("/admin/bookings/approve")
    public String approveBooking(@RequestParam("id") Integer id, RedirectAttributes redirectAttributes) {
        bookingRepository.findById(id).ifPresent(b -> {
            b.setTrangThai("DA_DUYET");
            bookingRepository.save(b);
        });
        redirectAttributes.addFlashAttribute("successMessage", "Đã duyệt đơn mượn phòng #" + id + " thành công!");
        return "redirect:/admin/bookings";
    }

    // 3. HỦY ĐƠN MƯỢN
    @PostMapping("/admin/bookings/reject")
    public String rejectBooking(@RequestParam("id") Integer id, RedirectAttributes redirectAttributes) {
        bookingRepository.findById(id).ifPresent(b -> {
            b.setTrangThai("TU_CHOI");
            bookingRepository.save(b);
        });
        redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn mượn phòng #" + id + " thành công!");
        return "redirect:/admin/bookings";
    }
}