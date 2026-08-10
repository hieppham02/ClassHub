package vn.edu.eaut.ems.controller;

import java.util.*;
import jakarta.servlet.http.HttpSession;
import vn.edu.eaut.ems.entity.Account;
import vn.edu.eaut.ems.repository.AccountRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.CookieValue;

@Controller
public class AuthController {

    private final AccountRepository accountRepository;

    public AuthController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        session.invalidate();
        Cookie cookie = new Cookie("rememberUser", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/login";
    }

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

    @PostMapping("/login")
    public String processLogin(@RequestParam String username, @RequestParam String password, HttpSession session,
            @RequestParam(name = "remember-me", required = false) String rememberMe, HttpServletResponse response,
            RedirectAttributes redirectAttributes) {
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
        Optional<Account> accountOpt = accountRepository.findById(username);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();

            if (account.getMatKhau().equals(password)) {
                session.setAttribute("loggedInUser", account);
                if (rememberMe != null) {
                    Cookie cookie = new Cookie("rememberUser", username);
                    cookie.setMaxAge(7 * 24 * 60 * 60);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                }

                return "redirect:/";
            }
        }

        redirectAttributes.addFlashAttribute("errorMessage", "Mã đăng nhập hoặc mật khẩu không đúng.");
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String showRegister() {
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute Account account, RedirectAttributes redirectAttributes) {
        System.out.println("Mã SV: " + account.getMaSv());

        if (accountRepository.existsById(account.getMaSv())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã tồn tại tài khoản với mã sinh viên này!");
            return "redirect:/register";
        }
        account.setVaiTro("ADMIN");
        accountRepository.save(account);
        redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công!");
        return "redirect:/login";
    }
    
}
