package vn.edu.eaut.ems.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AdminBuildingController {

    // Model Tòa nhà lưu tạm trong RAM
    public static class BuildingDto {
        private String maToaNha;
        private String tenToaNha;
        private Integer soTang;
        private Integer soPhong;
        private String trangThai; // HOAT_DONG, BAO_TRI

        public BuildingDto(String maToaNha, String tenToaNha, Integer soTang, Integer soPhong, String trangThai) {
            this.maToaNha = maToaNha;
            this.tenToaNha = tenToaNha;
            this.soTang = soTang;
            this.soPhong = soPhong;
            this.trangThai = trangThai;
        }

        public String getMaToaNha() { return maToaNha; }
        public void setMaToaNha(String maToaNha) { this.maToaNha = maToaNha; }
        public String getTenToaNha() { return tenToaNha; }
        public void setTenToaNha(String tenToaNha) { this.tenToaNha = tenToaNha; }
        public Integer getSoTang() { return soTang; }
        public void setSoTang(Integer soTang) { this.soTang = soTang; }
        public Integer getSoPhong() { return soPhong; }
        public void setSoPhong(Integer soPhong) { this.soPhong = soPhong; }
        public String getTrangThai() { return trangThai; }
        public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    }

    private final List<BuildingDto> buildingList = new ArrayList<>();

    @PostConstruct
    public void initMockData() {
        buildingList.add(new BuildingDto("TN01", "Polyco", 7, 28, "HOAT_DONG"));
        buildingList.add(new BuildingDto("TN02", "Đinh Trọng Dật", 9, 36, "HOAT_DONG"));
        buildingList.add(new BuildingDto("TN03", "EAUT", 5, 20, "HOAT_DONG"));
        buildingList.add(new BuildingDto("TN04", "Việt Nam", 4, 12, "BAO_TRI"));
         buildingList.add(new BuildingDto("TN05", "Thuận Thành", 5, 20, "HOAT_DONG"));
    }

    @GetMapping("/admin/buildings")
    public String getBuildings(Model model) {
        model.addAttribute("buildings", buildingList);
        model.addAttribute("totalBuildings", buildingList.size());
        model.addAttribute("activeBuildings", buildingList.stream().filter(b -> "HOAT_DONG".equals(b.getTrangThai())).count());
        model.addAttribute("maintenanceBuildings", buildingList.stream().filter(b -> "BAO_TRI".equals(b.getTrangThai())).count());
        return "admin/buildings";
    }

    @PostMapping("/admin/buildings/create")
    public String createBuilding(@RequestParam("maToaNha") String maToaNha,
                                 @RequestParam("tenToaNha") String tenToaNha,
                                 @RequestParam("soTang") Integer soTang,
                                 @RequestParam("soPhong") Integer soPhong,
                                 RedirectAttributes redirectAttributes) {
        buildingList.add(0, new BuildingDto(maToaNha, tenToaNha, soTang, soPhong, "HOAT_DONG"));
        redirectAttributes.addFlashAttribute("successMessage", "Thêm tòa nhà [" + tenToaNha + "] thành công!");
        return "redirect:/admin/buildings";
    }

    @PostMapping("/admin/buildings/update")
    public String updateBuilding(@RequestParam("maToaNha") String maToaNha,
                                 @RequestParam("tenToaNha") String tenToaNha,
                                 @RequestParam("soTang") Integer soTang,
                                 @RequestParam("soPhong") Integer soPhong,
                                 @RequestParam("trangThai") String trangThai,
                                 RedirectAttributes redirectAttributes) {
        for (BuildingDto b : buildingList) {
            if (b.getMaToaNha().equalsIgnoreCase(maToaNha)) {
                b.setTenToaNha(tenToaNha);
                b.setSoTang(soTang);
                b.setSoPhong(soPhong);
                b.setTrangThai(trangThai);
                break;
            }
        }
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tòa nhà thành công!");
        return "redirect:/admin/buildings";
    }

    @PostMapping("/admin/buildings/delete")
    public String deleteBuilding(@RequestParam("maToaNha") String maToaNha, RedirectAttributes redirectAttributes) {
        buildingList.removeIf(b -> b.getMaToaNha().equalsIgnoreCase(maToaNha));
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa tòa nhà thành công!");
        return "redirect:/admin/buildings";
    }
}