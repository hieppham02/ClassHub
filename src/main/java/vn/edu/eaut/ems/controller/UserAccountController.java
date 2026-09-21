package vn.edu.eaut.ems.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import vn.edu.eaut.ems.entity.Account;
import vn.edu.eaut.ems.repository.AccountRepository;

@Controller
public class UserAccountController {

    private final AccountRepository accountRepository;

    public UserAccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping("/tai-khoan")
    public String showAccountPage(HttpSession session, Model model) {
        Account sessionAccount = (Account) session.getAttribute("loggedInUser");
        if (sessionAccount == null) {
            return "redirect:/login";
        }

        Account account = accountRepository.findById(sessionAccount.getMaSv()).orElse(null);
        if (account == null) {
            session.invalidate();
            return "redirect:/login";
        }

        session.setAttribute("loggedInUser", account);
        model.addAttribute("account", account);
        return "account";
    }

    @PostMapping("/tai-khoan/phone")
    public String updatePhone(@RequestParam("sdt") String phone,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Account account = getCurrentAccount(session);
        if (account == null) {
            return "redirect:/login";
        }

        String normalizedPhone = phone == null ? "" : phone.replaceAll("[\\s.-]", "");
        if (!normalizedPhone.matches("0\\d{9,10}")) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Số điện thoại phải bắt đầu bằng 0 và gồm 10 đến 11 chữ số.");
            return "redirect:/tai-khoan";
        }

        account.setSdt(normalizedPhone);
        accountRepository.saveAndFlush(account);
        session.setAttribute("loggedInUser", account);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật số điện thoại thành công!");
        return "redirect:/tai-khoan";
    }

    @PostMapping("/tai-khoan/password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Account account = getCurrentAccount(session);
        if (account == null) {
            return "redirect:/login";
        }

        if (!account.getMatKhau().equals(currentPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu hiện tại không chính xác.");
            return "redirect:/tai-khoan";
        }
        if (newPassword == null || newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới phải có ít nhất 6 ký tự.");
            return "redirect:/tai-khoan";
        }
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Xác nhận mật khẩu mới không khớp.");
            return "redirect:/tai-khoan";
        }
        if (account.getMatKhau().equals(newPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới phải khác mật khẩu hiện tại.");
            return "redirect:/tai-khoan";
        }

        account.setMatKhau(newPassword);
        accountRepository.saveAndFlush(account);
        session.setAttribute("loggedInUser", account);
        redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
        return "redirect:/tai-khoan";
    }

    private Account getCurrentAccount(HttpSession session) {
        Account sessionAccount = (Account) session.getAttribute("loggedInUser");
        if (sessionAccount == null) {
            return null;
        }
        return accountRepository.findById(sessionAccount.getMaSv()).orElse(null);
    }
}
