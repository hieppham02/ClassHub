package vn.edu.eaut.ems.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.eaut.ems.entity.Account;
import vn.edu.eaut.ems.repository.AccountRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminSettingController {

    private final AccountRepository accountRepository;

    // Lưu tạm các thông số cấu hình hệ thống
    public static String systemName = "ClassHub - Hệ thống Quản lý Phòng học EAUT";
    public static String contactEmail = "support.ems@eaut.edu.vn";
    public static String hotline = "024 3784 7666";
    public static int maxShiftsPerDay = 2;       // Tối đa 2 ca/ngày cho sinh viên
    public static int maxAdvanceDays = 7;        // Đặt trước tối đa 7 ngày
    public static boolean autoApprove = false;   // Tự động duyệt đơn: Tắt

    public AdminSettingController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // 1. HIỂN THỊ TRANG CÀI ĐẶT
    @GetMapping("/admin/settings")
    public String getSettingsPage(Model model) {
        model.addAttribute("systemName", systemName);
        model.addAttribute("contactEmail", contactEmail);
        model.addAttribute("hotline", hotline);
        model.addAttribute("maxShiftsPerDay", maxShiftsPerDay);
        model.addAttribute("maxAdvanceDays", maxAdvanceDays);
        model.addAttribute("autoApprove", autoApprove);

        return "admin/settings";
    }

    // 2. LƯU CÀI ĐẶT THÔNG TIN HỆ THỐNG
    @PostMapping("/admin/settings/general")
    public String updateGeneralSettings(@RequestParam("systemName") String newSystemName,
                                        @RequestParam("contactEmail") String newEmail,
                                        @RequestParam("hotline") String newHotline,
                                        RedirectAttributes redirectAttributes) {
        systemName = newSystemName;
        contactEmail = newEmail;
        hotline = newHotline;

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin hệ thống thành công!");
        return "redirect:/admin/settings";
    }

    // 3. LƯU QUY ĐỊNH MƯỢN PHÒNG
    @PostMapping("/admin/settings/rules")
    public String updateBookingRules(@RequestParam("maxShiftsPerDay") int shifts,
                                     @RequestParam("maxAdvanceDays") int days,
                                     @RequestParam(value = "autoApprove", defaultValue = "false") boolean auto,
                                     RedirectAttributes redirectAttributes) {
        maxShiftsPerDay = shifts;
        maxAdvanceDays = days;
        autoApprove = auto;

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật quy định mượn phòng thành công!");
        return "redirect:/admin/settings";
    }

    // 4. ĐỔI MẬT KHẨU ADMIN
    @PostMapping("/admin/settings/password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới và xác nhận mật khẩu không khớp!");
            return "redirect:/admin/settings";
        }

        // Lấy tài khoản admin (20231206 hoặc tài khoản trong session)
        Account admin = (Account) session.getAttribute("loggedInUser");
        if (admin == null) {
            admin = accountRepository.findById("20231206").orElse(null);
        }

        if (admin != null) {
            if (!admin.getMatKhau().equals(currentPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu hiện tại không chính xác!");
                return "redirect:/admin/settings";
            }

            admin.setMatKhau(newPassword);
            accountRepository.save(admin);
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu tài trị viên thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy tài khoản quản trị!");
        }

        return "redirect:/admin/settings";
    }
}