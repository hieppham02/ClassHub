package vn.edu.eaut.ems.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import vn.edu.eaut.ems.entity.Account;

@Controller
public class AdminDashboardController {

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model, HttpSession session) {
        Account currentUser = (Account) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (!"ADMIN".equalsIgnoreCase(currentUser.getVaiTro())) {
            return "redirect:/";
        }

        model.addAttribute("currentUser", currentUser);

        // ===== MOCK DATA =====
        model.addAttribute("totalStudents", 1250);
        model.addAttribute("totalTeachers", 42);
        model.addAttribute("totalClasses", 36);
        model.addAttribute("totalRevenue", "125.500.000");

        model.addAttribute("presentStudents", 1080);
        model.addAttribute("absentStudents", 120);
        model.addAttribute("lateStudents", 50);

        model.addAttribute("recentStudents", new String[][]{
                {"Nguyễn Văn An", "Lớp 10A1", "05/09/2026"},
                {"Trần Thị Bình", "Lớp 11A2", "05/09/2026"},
                {"Lê Minh Khang", "Lớp 12A1", "04/09/2026"},
                {"Phạm Hoàng Nam", "Lớp 10A3", "04/09/2026"},
                {"Đỗ Ngọc Anh", "Lớp 11A1", "03/09/2026"}
        });

        model.addAttribute("classes", new String[][]{
                {"10A1", "Nguyễn Văn Minh", "35", "08:00 - 09:30"},
                {"11A2", "Trần Thị Lan", "32", "09:45 - 11:15"},
                {"12A1", "Lê Hoàng Anh", "30", "13:30 - 15:00"},
                {"10A3", "Phạm Văn Hùng", "36", "15:15 - 16:45"}
        });

        return "admin/dashboard";
    }
}
