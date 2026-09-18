package vn.edu.eaut.ems.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.eaut.ems.entity.Booking;
import vn.edu.eaut.ems.entity.Room;
import vn.edu.eaut.ems.repository.BookingRepository;
import vn.edu.eaut.ems.repository.RoomRepository;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AdminReportController {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    public AdminReportController(BookingRepository bookingRepository, RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
    }

    // DTO chứa thông tin phòng trống
    public static class FreeRoomDto {
        private String maPhong;
        private String tenPhong;
        private String toaNha;
        private int sucChua;
        private String soCaTrong;
        private String trangThaiText;

        public FreeRoomDto(String maPhong, String tenPhong, String toaNha, int sucChua, String soCaTrong, String trangThaiText) {
            this.maPhong = maPhong;
            this.tenPhong = tenPhong;
            this.toaNha = toaNha;
            this.sucChua = sucChua;
            this.soCaTrong = soCaTrong;
            this.trangThaiText = trangThaiText;
        }

        public String getMaPhong() { return maPhong; }
        public String getTenPhong() { return tenPhong; }
        public String getToaNha() { return toaNha; }
        public int getSucChua() { return sucChua; }
        public String getSoCaTrong() { return soCaTrong; }
        public String getTrangThaiText() { return trangThaiText; }
    }

    // Nhận diện chuẩn xác tên tòa nhà từ mã phòng
    private String resolveBuildingName(Room room) {
        if (room == null) return "Đinh Trọng Dật";
        String code = (room.getMaPhong() != null ? room.getMaPhong() : room.getTenPhong()).toUpperCase();
        if (code.startsWith("EAUT")) return "EAUT";
        if (code.startsWith("PLC") || code.contains("POLYCO")) return "POLYCO";
        if (code.startsWith("TT") || code.contains("THUẬN THÀNH")) return "Thuận Thành";
        if (code.startsWith("VNB") || code.contains("VIỆT NAM")) return "Việt Nam Building";
        return "Đinh Trọng Dật";
    }

    @GetMapping("/admin/reports")
    public String getReports(Model model) {
        // 1. TÍNH TOÁN CÁC CHỈ SỐ KPI ĐƠN MƯỢN
        List<Booking> bookings = bookingRepository.findAll();
        long totalBookings = bookings.size();

        long activeBookings = bookings.stream()
                .filter(b -> "DA_DUYET".equalsIgnoreCase(b.getTrangThai()) || "UY_QUYEN".equalsIgnoreCase(b.getTrangThai())).count();
        long completedBookings = bookings.stream()
                .filter(b -> "DA_TRA".equalsIgnoreCase(b.getTrangThai()) || "DA_TRA_HO".equalsIgnoreCase(b.getTrangThai())).count();
        long approvedCount = bookings.stream()
                .filter(b -> !"TU_CHOI".equalsIgnoreCase(b.getTrangThai()) && !"CHO_DUYET".equalsIgnoreCase(b.getTrangThai())).count();

        String approvalRate = totalBookings > 0 
                ? String.format("%.1f%%", (double) approvedCount / totalBookings * 100) 
                : "100%";

        model.addAttribute("totalMonthlyBookings", totalBookings);
        model.addAttribute("activeBookings", activeBookings);
        model.addAttribute("completedBookings", completedBookings);
        model.addAttribute("approvalRate", approvalRate);

        // 2. BIỂU ĐỒ CỘT: TẦN SUẤT MƯỢN THEO 5 CA HỌC
        List<Long> caData = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            final int ca = i;
            long countCa = bookings.stream().filter(b -> b.getCaMuon() != null && b.getCaMuon() == ca).count();
            caData.add(countCa);
        }
        model.addAttribute("chartCaData", caData);

        // =========================================================================
        // 3. [ĐÃ ĐỔI] BIỂU ĐỒ TRÒN: TỶ LỆ (%) SỐ LƯỢNG PHÒNG HỌC MỖI TÒA NHÀ CHIẾM
        // =========================================================================
        List<Room> allRooms = roomRepository.findAll();

        long countDtdRooms = 0;
        long countPlcRooms = 0;
        long countEautRooms = 0;
        long countTtRooms = 0;
        long countVnbRooms = 0;

        // Quét từng phòng học trong CSDL để đếm quy mô phòng của từng tòa nhà
        for (Room r : allRooms) {
            String bld = resolveBuildingName(r);
            switch (bld) {
                case "EAUT" -> countEautRooms++;
                case "POLYCO" -> countPlcRooms++;
                case "Thuận Thành" -> countTtRooms++;
                case "Việt Nam Building" -> countVnbRooms++;
                default -> countDtdRooms++;
            }
        }

        List<String> buildingLabels = List.of("Đinh Trọng Dật", "POLYCO", "EAUT", "Thuận Thành", "Việt Nam Building");
        List<Long> buildingData = List.of(countDtdRooms, countPlcRooms, countEautRooms, countTtRooms, countVnbRooms);

        model.addAttribute("chartBuildingLabels", buildingLabels);
        model.addAttribute("chartBuildingData", buildingData);

        // 4. DANH SÁCH TOP PHÒNG HỌC CÒN TRỐNG / SẴN SÀNG
        Set<String> busyRoomCodes = bookings.stream()
                .filter(b -> !"DA_TRA".equalsIgnoreCase(b.getTrangThai()) && !"TU_CHOI".equalsIgnoreCase(b.getTrangThai()))
                .filter(b -> b.getRoom() != null)
                .map(b -> b.getRoom().getMaPhong().toUpperCase().replace("-", "").trim())
                .collect(Collectors.toSet());

        List<FreeRoomDto> freeRooms = allRooms.stream()
                .filter(r -> !busyRoomCodes.contains(r.getMaPhong().toUpperCase().replace("-", "").trim()))
                .limit(7)
                .map(r -> {
                    String toa = resolveBuildingName(r);
                    return new FreeRoomDto(
                        r.getMaPhong(), 
                        r.getTenPhong(), 
                        toa, 
                        r.getSucChua() != null ? r.getSucChua() : 70, 
                        "5/5 ca trống", 
                        "Sẵn sàng 100%"
                    );
                })
                .collect(Collectors.toList());

        model.addAttribute("freeRooms", freeRooms);

        return "admin/reports";
    }
}