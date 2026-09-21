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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
public class AdminReportController {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    public AdminReportController(BookingRepository bookingRepository, RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
    }

    public static class VacantRoomDto {
        private String maPhong;
        private String tenPhong;
        private String toaNha;
        private int sucChua;
        private int bookedCount;
        private String vacancyRate;
        private String statusText;

        public VacantRoomDto(String maPhong, String tenPhong, String toaNha, int sucChua, int bookedCount, String vacancyRate, String statusText) {
            this.maPhong = maPhong;
            this.tenPhong = tenPhong;
            this.toaNha = toaNha;
            this.sucChua = sucChua;
            this.bookedCount = bookedCount;
            this.vacancyRate = vacancyRate;
            this.statusText = statusText;
        }

        public String getMaPhong() { return maPhong; }
        public String getTenPhong() { return tenPhong; }
        public String getToaNha() { return toaNha; }
        public int getSucChua() { return sucChua; }
        public int getBookedCount() { return bookedCount; }
        public String getVacancyRate() { return vacancyRate; }
        public String getStatusText() { return statusText; }
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
        List<Booking> bookings = bookingRepository.findAll();
        long totalBookings = bookings.size();

        // 1. TÍNH TOÁN CÁC CHỈ SỐ KPI
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

        // 2. BIỂU ĐỒ CỘT: THEO 5 CA HỌC
        List<Long> caData = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            final int ca = i;
            long countCa = bookings.stream().filter(b -> b.getCaMuon() != null && b.getCaMuon() == ca).count();
            caData.add(countCa);
        }
        model.addAttribute("chartCaData", caData);

        // Biểu đồ lượt mượn theo ngày: nạp sẵn 30 ngày để lọc nhanh 7/14/30 ngày.
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29);
        Map<LocalDate, Long> dailyBookingCounts = bookings.stream()
                .filter(b -> b.getNgayMuon() != null)
                .filter(b -> !"TU_CHOI".equalsIgnoreCase(b.getTrangThai()))
                .filter(b -> !b.getNgayMuon().isBefore(startDate) && !b.getNgayMuon().isAfter(endDate))
                .collect(Collectors.groupingBy(Booking::getNgayMuon, Collectors.counting()));

        DateTimeFormatter chartDateFormatter = DateTimeFormatter.ofPattern("dd/MM");
        List<String> dailyLabels = new ArrayList<>();
        List<Long> dailyData = new ArrayList<>();
        for (int dayOffset = 0; dayOffset < 30; dayOffset++) {
            LocalDate date = startDate.plusDays(dayOffset);
            dailyLabels.add(date.format(chartDateFormatter));
            dailyData.add(dailyBookingCounts.getOrDefault(date, 0L));
        }
        model.addAttribute("chartDailyLabels", dailyLabels);
        model.addAttribute("chartDailyData", dailyData);

        // =========================================================================
        // 3. [BIỂU ĐỒ TRÒN] ĐẾM SỐ LƯỢNG PHÒNG HỌC THỰC TẾ TRONG CSDL ĐỂ TÍNH %
        // =========================================================================
        List<Room> allRooms = roomRepository.findAll();

        long countDtdRooms = 0;
        long countPlcRooms = 0;
        long countEautRooms = 0;
        long countTtRooms = 0;
        long countVnbRooms = 0;

        // Quét từng phòng học thực tế trong bảng phong_hoc
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

        // =========================================================================
        // 4. BẢNG TOP PHÒNG TRỐNG NHIỀU NHẤT (SẮP XẾP TOP 1, 2, 3...)
        // =========================================================================
        Map<String, Long> roomBookingCounts = bookings.stream()
                .filter(b -> b.getRoom() != null)
                .map(b -> b.getRoom().getMaPhong().toUpperCase().replace("-", "").trim())
                .collect(Collectors.groupingBy(code -> code, Collectors.counting()));

        List<VacantRoomDto> topFreeRooms = allRooms.stream()
                .map(r -> {
                    String cleanCode = r.getMaPhong().toUpperCase().replace("-", "").trim();
                    int timesBooked = roomBookingCounts.getOrDefault(cleanCode, 0L).intValue();
                    String toa = resolveBuildingName(r);

                    int vacancyPercent = Math.max(10, 100 - (timesBooked * 20));
                    String statusText = (timesBooked == 0) ? "Trống 100% (Chưa ai mượn)" : ("Chỉ mượn " + timesBooked + " lần");

                    return new VacantRoomDto(
                        r.getMaPhong(),
                        r.getTenPhong(),
                        toa,
                        r.getSucChua() != null ? r.getSucChua() : 70,
                        timesBooked,
                        vacancyPercent + "%",
                        statusText
                    );
                })
                .sorted(Comparator.comparingInt(VacantRoomDto::getBookedCount))
                .limit(300)
                .collect(Collectors.toList());

        model.addAttribute("topFreeRooms", topFreeRooms);

        return "admin/reports";
    }
}
