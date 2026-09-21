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
    public static boolean autoApprove = false;
    public static boolean autoReturn = false;

    public AdminSettingController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // 1. HIỂN THỊ TRANG CÀI ĐẶT
    @GetMapping("/admin/settings")
    public String getSettingsPage(Model model) {
        model.addAttribute("systemName", systemName);
        model.addAttribute("contactEmail", contactEmail);
        model.addAttribute("hotline", hotline);
        model.addAttribute("autoApprove", autoApprove);
        model.addAttribute("autoReturn", autoReturn);

        return "admin/settings";
    }

    // Lưu hai quy định bật/tắt trên trang cài đặt.
    @PostMapping("/admin/settings/rules")
    public String updateBookingRules(@RequestParam(value = "autoApprove", defaultValue = "false") boolean approve,
                                     @RequestParam(value = "autoReturn", defaultValue = "false") boolean returnAutomatically,
                                     RedirectAttributes redirectAttributes) {
        autoApprove = approve;
        autoReturn = returnAutomatically;

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật quy định mượn phòng thành công!");
        return "redirect:/admin/settings";
    }

    // Đổi mật khẩu thật của tài khoản admin đang đăng nhập trong CSDL.
    @PostMapping("/admin/settings/password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        if (newPassword == null || newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới phải có ít nhất 6 ký tự!");
            return "redirect:/admin/settings";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới và xác nhận mật khẩu không khớp!");
            return "redirect:/admin/settings";
        }

        Account sessionAccount = (Account) session.getAttribute("loggedInUser");
        if (sessionAccount == null || !"ADMIN".equalsIgnoreCase(sessionAccount.getVaiTro())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phiên đăng nhập quản trị không hợp lệ!");
            return "redirect:/login";
        }

        Account admin = accountRepository.findById(sessionAccount.getMaSv()).orElse(null);
        if (admin == null || !"ADMIN".equalsIgnoreCase(admin.getVaiTro())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy tài khoản quản trị trong cơ sở dữ liệu!");
            return "redirect:/admin/settings";
        }

        if (!admin.getMatKhau().equals(currentPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu hiện tại không chính xác!");
            return "redirect:/admin/settings";
        }

        if (admin.getMatKhau().equals(newPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới phải khác mật khẩu hiện tại!");
            return "redirect:/admin/settings";
        }

        admin.setMatKhau(newPassword);
        accountRepository.saveAndFlush(admin);
        session.setAttribute("loggedInUser", admin);
        redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu quản trị viên thành công!");

        return "redirect:/admin/settings";
    }
}
