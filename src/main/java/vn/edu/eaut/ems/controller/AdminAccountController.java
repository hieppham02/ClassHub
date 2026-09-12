package vn.edu.eaut.ems.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.eaut.ems.entity.Account;
import vn.edu.eaut.ems.repository.AccountRepository;

import java.util.List;

@Controller
public class AdminAccountController {

    private final AccountRepository accountRepository;

    public AdminAccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // =========================================================================
    // 1. HIỂN THỊ DANH SÁCH TÀI KHOẢN THẬT TỪ BẢNG tai_khoan TRONG MYSQL
    // =========================================================================
    @GetMapping("/admin/accounts")
    public String getAccountManagement(Model model) {
        // Lấy tất cả tài khoản thật từ MySQL
        List<Account> accounts = accountRepository.findAll();

        // Đếm số lượng thật từ CSDL
        long totalAccounts = accounts.size();
        long totalStudents = accounts.stream()
                .filter(a -> "SINHVIEN".equalsIgnoreCase(a.getVaiTro()) || "SINH_VIEN".equalsIgnoreCase(a.getVaiTro()))
                .count();
        long totalTeachers = accounts.stream()
                .filter(a -> "ADMIN".equalsIgnoreCase(a.getVaiTro()) || "GIANG_VIEN".equalsIgnoreCase(a.getVaiTro()))
                .count();

        model.addAttribute("accounts", accounts);
        model.addAttribute("totalAccounts", totalAccounts);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalTeachers", totalTeachers);

        return "admin/accounts";
    }

    // =========================================================================
    // 2. THÊM TÀI KHOẢN MỚI THẲNG VÀO MYSQL
    // =========================================================================
    @PostMapping("/admin/accounts/create")
    public String createAccount(@ModelAttribute Account newAccount, RedirectAttributes redirectAttributes) {
        if (newAccount.getMaSv() == null || newAccount.getMaSv().isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mã tài khoản không được để trống!");
            return "redirect:/admin/accounts";
        }

        String cleanMaSv = newAccount.getMaSv().trim();

        // Kiểm tra xem mã sinh viên / cán bộ đã có trong CSDL chưa
        if (accountRepository.existsById(cleanMaSv)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mã tài khoản [" + cleanMaSv + "] đã tồn tại trong CSDL!");
            return "redirect:/admin/accounts";
        }

        // Đặt giá trị mặc định nếu để trống
        if (newAccount.getMatKhau() == null || newAccount.getMatKhau().isBlank()) {
            newAccount.setMatKhau("123456");
        }
        if (newAccount.getVaiTro() == null || newAccount.getVaiTro().isBlank()) {
            newAccount.setVaiTro("SINHVIEN");
        }

        newAccount.setMaSv(cleanMaSv);
        accountRepository.save(newAccount); // Lưu trực tiếp vào MySQL

        redirectAttributes.addFlashAttribute("successMessage", "Thêm mới tài khoản [" + cleanMaSv + "] vào CSDL thành công!");
        return "redirect:/admin/accounts";
    }

    // =========================================================================
    // 3. CẬP NHẬT THÔNG TIN TÀI KHOẢN TRONG MYSQL
    // =========================================================================
    @PostMapping("/admin/accounts/update")
    public String updateAccount(@ModelAttribute Account updatedAccount, RedirectAttributes redirectAttributes) {
        if (updatedAccount.getMaSv() != null && accountRepository.existsById(updatedAccount.getMaSv())) {
            Account existing = accountRepository.findById(updatedAccount.getMaSv()).orElse(null);
            if (existing != null) {
                existing.setHoTen(updatedAccount.getHoTen().trim());
                existing.setEmail(updatedAccount.getEmail().trim());
                existing.setSdt(updatedAccount.getSdt() != null ? updatedAccount.getSdt().trim() : null);
                existing.setTenLop(updatedAccount.getTenLop() != null ? updatedAccount.getTenLop().trim() : null);
                existing.setVaiTro(updatedAccount.getVaiTro());

                accountRepository.save(existing); // Lưu đè cập nhật vào MySQL
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tài khoản [" + updatedAccount.getMaSv() + "] thành công!");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy tài khoản cần cập nhật trong CSDL!");
        }
        return "redirect:/admin/accounts";
    }

    // =========================================================================
    // 4. XÓA TÀI KHOẢN KHỎI MYSQL
    // =========================================================================
    @PostMapping("/admin/accounts/delete")
    public String deleteAccount(@RequestParam("maSv") String maSv, RedirectAttributes redirectAttributes) {
        if (accountRepository.existsById(maSv)) {
            try {
                accountRepository.deleteById(maSv); // Xóa khỏi MySQL
                redirectAttributes.addFlashAttribute("successMessage", "Đã xóa tài khoản [" + maSv + "] khỏi CSDL!");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa tài khoản này vì đã có lịch sử mượn phòng trong CSDL!");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy tài khoản cần xóa!");
        }
        return "redirect:/admin/accounts";
    }
}