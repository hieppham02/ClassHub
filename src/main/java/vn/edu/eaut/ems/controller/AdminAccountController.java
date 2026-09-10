package vn.edu.eaut.ems.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.eaut.ems.entity.Account;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminAccountController {

    // Danh sách lưu trữ tạm thời trong RAM (Mock Data)
    private final List<Account> accountList = new ArrayList<>();

    // Khởi tạo dữ liệu mẫu khi ứng dụng khởi chạy
    @PostConstruct
    public void initMockData() {
        Account acc1 = new Account("SV2023001", "123456", "Nguyễn Văn An", "an.nv@eaut.edu.vn", "0912 345 678");
        acc1.setTenLop("CNTT14-01");
        acc1.setVaiTro("SINH_VIEN");
        accountList.add(acc1);

        Account acc2 = new Account("GV001", "123456", "ThS. Lê Hoàng Long", "long.lh@eaut.edu.vn", "0987 654 321");
        acc2.setTenLop("Khoa CNTT");
        acc2.setVaiTro("GIANG_VIEN");
        accountList.add(acc2);

        Account acc3 = new Account("SV2023002", "123456", "Trần Thị Bích", "bich.tt@eaut.edu.vn", "0905 112 233");
        acc3.setTenLop("DTVT14-02");
        acc3.setVaiTro("SINH_VIEN");
        accountList.add(acc3);

        Account acc4 = new Account("AD001", "admin123", "Phạm Minh Đức", "admin@eaut.edu.vn", "0934 889 900");
        acc4.setTenLop("Phòng Đào Tạo");
        acc4.setVaiTro("ADMIN");
        accountList.add(acc4);

        Account acc5 = new Account("GV002", "123456", "TS. Hoàng Mỹ Linh", "linh.hm@eaut.edu.vn", "0978 223 344");
        acc5.setTenLop("Khoa Ngoại Ngữ");
        acc5.setVaiTro("GIANG_VIEN");
        accountList.add(acc5);

        Account acc6 = new Account("SV2023003", "123456", "Vũ Tuấn Kiệt", "kiet.vt@eaut.edu.vn", "0918 556 677");
        acc6.setTenLop("QTKD14-01");
        acc6.setVaiTro("SINH_VIEN");
        accountList.add(acc6);
    }

    // 1. Hiển thị danh sách tài khoản
    @GetMapping("/accounts")
    public String getAccountManagement(Model model) {
        model.addAttribute("totalAccounts", accountList.size());
        model.addAttribute("totalStudents", 1250);
        model.addAttribute("totalTeachers", 42);
        model.addAttribute("totalClasses", 36);

        model.addAttribute("accounts", accountList);
        return "admin/accounts";
    }

    // 2. Xử lý lưu thông tin chỉnh sửa tài khoản
    @PostMapping("/accounts/update")
    public String updateAccount(@ModelAttribute Account updatedAccount, RedirectAttributes redirectAttributes) {
        for (Account acc : accountList) {
            if (acc.getMaSv().equalsIgnoreCase(updatedAccount.getMaSv())) {
                acc.setHoTen(updatedAccount.getHoTen());
                acc.setEmail(updatedAccount.getEmail());
                acc.setSdt(updatedAccount.getSdt());
                acc.setTenLop(updatedAccount.getTenLop());
                acc.setVaiTro(updatedAccount.getVaiTro());
                break;
            }
        }
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tài khoản [" + updatedAccount.getMaSv() + "] thành công!");
        return "redirect:/admin/accounts";
    }

    // 3. Xử lý xóa tài khoản (MỚI THÊM ĐẦY ĐỦ)
    @PostMapping("/accounts/delete")
    public String deleteAccount(@RequestParam("maSv") String maSv, RedirectAttributes redirectAttributes) {
        // Tìm và xóa phần tử có mã trùng khớp
        boolean isRemoved = accountList.removeIf(acc -> acc.getMaSv().equalsIgnoreCase(maSv));

        if (isRemoved) {
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa tài khoản [" + maSv + "] thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy mã tài khoản [" + maSv + "] để xóa!");
        }

        return "redirect:/admin/accounts";
    }
}