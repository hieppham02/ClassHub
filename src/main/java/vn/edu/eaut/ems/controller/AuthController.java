package vn.edu.eaut.ems.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.eaut.ems.entity.Account;
import vn.edu.eaut.ems.repository.AccountRepository;

@Controller
public class AuthController {

    private final AccountRepository accountRepository;

    public AuthController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // 1. ĐĂNG XUẤT
    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        session.invalidate();
        Cookie cookie = new Cookie("rememberUser", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/login";
    }

    // 2. HIỂN THỊ TRANG ĐĂNG NHẬP
    @GetMapping("/login")
    public String showLogin(@CookieValue(value = "rememberUser", defaultValue = "") String rememberUser,
            HttpSession session) {
        if (!rememberUser.isEmpty()) {
            Optional<Account> accountOpt = accountRepository.findById(rememberUser);
            if (accountOpt.isPresent()) {
                session.setAttribute("loggedInUser", accountOpt.get());
                return "redirect:/";
            }
        }
        return "login";
    }

    // 3. XỬ LÝ ĐĂNG NHẬP
    @PostMapping("/login")
    public String processLogin(@RequestParam String username, @RequestParam String password, HttpSession session,
            @RequestParam(name = "remember-me", required = false) String rememberMe, HttpServletResponse response,
            RedirectAttributes redirectAttributes) {

        String loginUser = username.trim();

        // Tìm theo Mã SV hoặc Email
        Optional<Account> accountOpt = accountRepository.findById(loginUser);
        if (accountOpt.isEmpty()) {
            accountOpt = accountRepository.findAll().stream()
                    .filter(a -> a.getEmail().equalsIgnoreCase(loginUser))
                    .findFirst();
        }

        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            if (account.getMatKhau().equals(password)) {
                session.setAttribute("loggedInUser", account);

                // Ghi nhớ đăng nhập nếu có tích chọn
                if (rememberMe != null) {
                    Cookie cookie = new Cookie("rememberUser", account.getMaSv());
                    cookie.setMaxAge(7 * 24 * 60 * 60); // 7 ngày
                    cookie.setPath("/");
                    response.addCookie(cookie);
                }

                // Nếu là Admin thì chuyển hướng sang Dashboard
                if ("ADMIN".equalsIgnoreCase(account.getVaiTro())) {
                    return "redirect:/admin/dashboard";
                }

                return "redirect:/";
            }
        }

        redirectAttributes.addFlashAttribute("errorMessage", "Mã đăng nhập hoặc mật khẩu không đúng.");
        return "redirect:/login";
    }

    // 4. HIỂN THỊ TRANG ĐĂNG KÝ
    @GetMapping("/register")
    public String showRegister() {
        return "register";
    }

    // =========================================================================
    // 5. [ĐÃ SỬA TRIỆT ĐỂ] XỬ LÝ ĐĂNG KÝ TÀI KHOẢN MỚI
    // =========================================================================
    @PostMapping("/register")
    public String processRegister(@ModelAttribute Account account,
                                  @RequestParam Map<String, String> allParams,
                                  RedirectAttributes redirectAttributes) {

        // 1. TỰ ĐỘNG BẮT TÊN TRƯỜNG DÙ HTML ĐẶT LÀ GÌ (maSv hoặc username, studentCode...)
        String maSv = account.getMaSv();
        if (maSv == null || maSv.isBlank()) {
            maSv = allParams.getOrDefault("username", allParams.get("studentCode"));
        }

        String hoTen = account.getHoTen();
        if (hoTen == null || hoTen.isBlank()) {
            hoTen = allParams.getOrDefault("fullname", allParams.get("name"));
        }

        String email = account.getEmail();
        if (email == null || email.isBlank()) {
            email = allParams.get("email");
        }

        // Bắt mật khẩu (hỗ trợ cả matKhau lẫn password)
        String matKhau = account.getMatKhau();
        if (matKhau == null || matKhau.isBlank()) {
            matKhau = allParams.getOrDefault("password", allParams.get("pass"));
        }

        String sdt = account.getSdt();
        if (sdt == null || sdt.isBlank()) {
            sdt = allParams.getOrDefault("phone", "0900000000");
        }

        String tenLop = account.getTenLop();
        if (tenLop == null || tenLop.isBlank()) {
            tenLop = allParams.getOrDefault("className", "DCCNTT.14");
        }

        // 2. KIỂM TRA DỮ LIỆU BẮT BUỘC
        if (maSv == null || maSv.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập Mã sinh viên!");
            return "redirect:/register";
        }
        if (email == null || email.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập Email!");
            return "redirect:/register";
        }
        if (matKhau == null || matKhau.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập Mật khẩu!");
            return "redirect:/register";
        }

        maSv = maSv.trim();
        email = email.trim();

        // 3. KIỂM TRA TRÙNG LẶP TRONG MYSQL
        if (accountRepository.existsById(maSv)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã tồn tại tài khoản với mã sinh viên: " + maSv);
            return "redirect:/register";
        }

        final String checkEmail = email;
        boolean emailExists = accountRepository.findAll().stream()
                .anyMatch(a -> a.getEmail() != null && a.getEmail().equalsIgnoreCase(checkEmail));
        if (emailExists) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email [" + email + "] đã được sử dụng!");
            return "redirect:/register";
        }

        // 4. LƯU TÀI KHOẢN MỚI VÀO MYSQL (KHÔNG BAO GIỜ BỊ NULL CỘT BẮT BUỘC)
        Account newAcc = new Account();
        newAcc.setMaSv(maSv);
        newAcc.setHoTen(hoTen != null ? hoTen.trim() : "Sinh Viên " + maSv);
        newAcc.setEmail(email);
        newAcc.setMatKhau(matKhau);
        newAcc.setSdt(sdt);
        newAcc.setTenLop(tenLop);
        newAcc.setVaiTro("SINHVIEN"); // <--- CỐ ĐỊNH QUYỀN SINH VIÊN

        accountRepository.save(newAcc); // Lưu thẳng vào bảng tai_khoan

        redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
        return "redirect:/login";
    }
}