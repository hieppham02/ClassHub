package vn.edu.eaut.ems.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AdminEquipmentController {

    public static class EquipmentDto {
        private String maTb;
        private String tenTb;
        private String loaiTb;
        private String viTri; // Phòng nào, Tòa nào
        private Integer soLuong;
        private String tinhTrang; // TOT, DANG_DUNG, HONG

        public EquipmentDto(String maTb, String tenTb, String loaiTb, String viTri, Integer soLuong, String tinhTrang) {
            this.maTb = maTb;
            this.tenTb = tenTb;
            this.loaiTb = loaiTb;
            this.viTri = viTri;
            this.soLuong = soLuong;
            this.tinhTrang = tinhTrang;
        }

        public String getMaTb() { return maTb; }
        public void setMaTb(String maTb) { this.maTb = maTb; }
        public String getTenTb() { return tenTb; }
        public void setTenTb(String tenTb) { this.tenTb = tenTb; }
        public String getLoaiTb() { return loaiTb; }
        public void setLoaiTb(String loaiTb) { this.loaiTb = loaiTb; }
        public String getViTri() { return viTri; }
        public void setViTri(String viTri) { this.viTri = viTri; }
        public Integer getSoLuong() { return soLuong; }
        public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
        public String getTinhTrang() { return tinhTrang; }
        public void setTinhTrang(String tinhTrang) { this.tinhTrang = tinhTrang; }
    }

    private final List<EquipmentDto> equipmentList = new ArrayList<>();

    @PostConstruct
    public void initMockData() {
        equipmentList.add(new EquipmentDto("TB01", "Máy chiếu Sony Laser 4K", "Trình chiếu", "P401 - Polyco", 1, "DANG_DUNG"));
        equipmentList.add(new EquipmentDto("TB02", "Bộ Micro không dây Shure", "Âm thanh", "Hội trường Lớn - EAUT", 2, "TOT"));
        equipmentList.add(new EquipmentDto("TB03", "Điều hòa Daikin 24000BTU", "Điện lạnh", "LAB302 - Đinh Trọng Dật", 2, "TOT"));
        equipmentList.add(new EquipmentDto("TB04", "Switch mạng Cisco 48 Cổng", "Mạng/CNTT", "LAB301 - Đinh Trọng Dật", 1, "DANG_DUNG"));
        equipmentList.add(new EquipmentDto("TB05", "Máy chiếu Panasonic PT-LB", "Trình chiếu", "P205 - Polyco", 1, "HONG"));
    }

    @GetMapping("/admin/equipment")
    public String getEquipment(Model model) {
        model.addAttribute("equipments", equipmentList);
        model.addAttribute("totalEquipments", equipmentList.size());
        model.addAttribute("goodEquipments", equipmentList.stream().filter(e -> "TOT".equals(e.getTinhTrang())).count());
        model.addAttribute("inUseEquipments", equipmentList.stream().filter(e -> "DANG_DUNG".equals(e.getTinhTrang())).count());
        model.addAttribute("brokenEquipments", equipmentList.stream().filter(e -> "HONG".equals(e.getTinhTrang())).count());
        return "admin/equipment";
    }

    @PostMapping("/admin/equipment/create")
    public String createEquipment(@RequestParam("maTb") String maTb,
                                  @RequestParam("tenTb") String tenTb,
                                  @RequestParam("loaiTb") String loaiTb,
                                  @RequestParam("viTri") String viTri,
                                  @RequestParam("soLuong") Integer soLuong,
                                  RedirectAttributes redirectAttributes) {
        equipmentList.add(0, new EquipmentDto(maTb, tenTb, loaiTb, viTri, soLuong, "TOT"));
        redirectAttributes.addFlashAttribute("successMessage", "Thêm thiết bị mới thành công!");
        return "redirect:/admin/equipment";
    }

    @PostMapping("/admin/equipment/update")
    public String updateEquipment(@RequestParam("maTb") String maTb,
                                  @RequestParam("tenTb") String tenTb,
                                  @RequestParam("loaiTb") String loaiTb,
                                  @RequestParam("viTri") String viTri,
                                  @RequestParam("tinhTrang") String tinhTrang,
                                  RedirectAttributes redirectAttributes) {
        for (EquipmentDto e : equipmentList) {
            if (e.getMaTb().equalsIgnoreCase(maTb)) {
                e.setTenTb(tenTb);
                e.setLoaiTb(loaiTb);
                e.setViTri(viTri);
                e.setTinhTrang(tinhTrang);
                break;
            }
        }
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thiết bị thành công!");
        return "redirect:/admin/equipment";
    }

    @PostMapping("/admin/equipment/delete")
    public String deleteEquipment(@RequestParam("maTb") String maTb, RedirectAttributes redirectAttributes) {
        equipmentList.removeIf(e -> e.getMaTb().equalsIgnoreCase(maTb));
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa thiết bị!");
        return "redirect:/admin/equipment";
    }
}