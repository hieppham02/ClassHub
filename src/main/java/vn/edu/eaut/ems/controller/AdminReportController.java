package vn.edu.eaut.ems.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.eaut.ems.entity.Booking;
import vn.edu.eaut.ems.entity.Room;
import vn.edu.eaut.ems.repository.BookingRepository;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AdminReportController {

    private final BookingRepository bookingRepository;

    public AdminReportController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // DTO chứa thông tin Top phòng mượn nhiều nhất
    public static class TopRoomDto {
        private String maPhong;
        private String tenPhong;
        private String toaNha;
        private int soLuotMuon;
        private int tongGio;
        private String tyLe;

        public TopRoomDto(String maPhong, String tenPhong, String toaNha, int soLuotMuon, int tongGio, String tyLe) {
            this.maPhong = maPhong;
            this.tenPhong = tenPhong;
            this.toaNha = toaNha;
            this.soLuotMuon = soLuotMuon;
            this.tongGio = tongGio;
            this.tyLe = tyLe;
        }

        public String getMaPhong() { return maPhong; }
        public String getTenPhong() { return tenPhong; }
        public String getToaNha() { return toaNha; }
        public int getSoLuotMuon() { return soLuotMuon; }
        public int getTongGio() { return tongGio; }
        public String getTyLe() { return tyLe; }
    }

    @GetMapping("/admin/reports")
    public String getReports(Model model) {
        // 1. LẤY TẤT CẢ PHIẾU MƯỢN TỪ CSDL MYSQL
        List<Booking> bookings = bookingRepository.findAll();
        long totalBookings = bookings.size();

        // 2. TÍNH TOÁN CÁC CHỈ SỐ KPI THỰC TẾ
        long activeBookings = bookings.stream()
                .filter(b -> "DA_DUYET".equalsIgnoreCase(b.getTrangThai()) || "UY_QUYEN".equalsIgnoreCase(b.getTrangThai())).count();
        long completedBookings = bookings.stream()
                .filter(b -> "DA_TRA".equalsIgnoreCase(b.getTrangThai())).count();
        long approvedCount = bookings.stream()
                .filter(b -> !"TU_CHOI".equalsIgnoreCase(b.getTrangThai()) && !"CHO_DUYET".equalsIgnoreCase(b.getTrangThai())).count();

        String approvalRate = totalBookings > 0 
                ? String.format("%.1f%%", (double) approvedCount / totalBookings * 100) 
                : "100%";

        model.addAttribute("totalMonthlyBookings", totalBookings);
        model.addAttribute("activeBookings", activeBookings);
        model.addAttribute("completedBookings", completedBookings);
        model.addAttribute("approvalRate", approvalRate);

        // 3. THỐNG KÊ LƯỢT MƯỢN THEO 5 CA HỌC (Vẽ biểu đồ cột)
        List<Long> caData = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            final int ca = i;
            long countCa = bookings.stream().filter(b -> b.getCaMuon() != null && b.getCaMuon() == ca).count();
            caData.add(countCa);
        }
        model.addAttribute("chartCaData", caData);

        // 4. THỐNG KÊ LƯỢT MƯỢN THEO TÒA NHÀ (Vẽ biểu đồ tròn)
        Map<String, Long> buildingCounts = bookings.stream()
                .filter(b -> b.getRoom() != null && b.getRoom().getBuilding() != null)
                .collect(Collectors.groupingBy(b -> b.getRoom().getBuilding().getTenToaNha(), Collectors.counting()));

        List<String> buildingLabels = new ArrayList<>();
        List<Long> buildingData = new ArrayList<>();

        if (buildingCounts.isEmpty()) {
            buildingLabels = List.of("Đinh Trọng Dật", "POLYCO", "EAUT", "Thuận Thành");
            buildingData = List.of(0L, 0L, 0L, 0L);
        } else {
            for (Map.Entry<String, Long> entry : buildingCounts.entrySet()) {
                buildingLabels.add(entry.getKey());
                buildingData.add(entry.getValue());
            }
        }
        model.addAttribute("chartBuildingLabels", buildingLabels);
        model.addAttribute("chartBuildingData", buildingData);

        // 5. TỰ ĐỘNG TÍNH TOP PHÒNG HỌC ĐƯỢC MƯỢN NHIỀU NHẤT TỪ CSDL
        Map<Room, Long> roomCounts = bookings.stream()
                .filter(b -> b.getRoom() != null)
                .collect(Collectors.groupingBy(Booking::getRoom, Collectors.counting()));

        List<TopRoomDto> topRooms = roomCounts.entrySet().stream()
                .sorted(Map.Entry.<Room, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Room r = entry.getKey();
                    long count = entry.getValue();
                    String toaNha = (r.getBuilding() != null) ? r.getBuilding().getTenToaNha() : "N/A";
                    int percent = totalBookings > 0 ? (int) ((double) count / totalBookings * 100) : 100;
                    return new TopRoomDto(r.getMaPhong(), r.getTenPhong(), toaNha, (int) count, (int) (count * 2.5), percent + "%");
                })
                .collect(Collectors.toList());

        model.addAttribute("topRooms", topRooms);

        return "admin/reports";
    }
}