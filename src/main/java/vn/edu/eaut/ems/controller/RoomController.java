package vn.edu.eaut.ems.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoomController {

    @GetMapping("/api/test")
    public String testApp() {
        return "Hệ thống mượn trả thiết bị EAUT đã chạy thành công!";
    }
}