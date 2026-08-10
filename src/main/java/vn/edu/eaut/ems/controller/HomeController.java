package vn.edu.eaut.ems.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller // Lưu ý: Dùng @Controller, KHÔNG dùng @RestController
public class HomeController {

    @GetMapping("/")
    public String showIndex(HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login"; 
        }
        return "index";
    }
}