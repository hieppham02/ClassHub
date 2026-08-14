package vn.edu.eaut.ems.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import vn.edu.eaut.ems.entity.Building;
import vn.edu.eaut.ems.repository.BuildingRepository;

@Controller // Lưu ý: Dùng @Controller, KHÔNG dùng @RestController
public class HomeController {

    private final BuildingRepository buildingRepository;

    public HomeController(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    @GetMapping("/")
    public String showIndex(HttpSession session, Model model) {
        List<Building> buildingList = buildingRepository.findAll();
        model.addAttribute("buildings", buildingList);
        return "index";
    }
}
