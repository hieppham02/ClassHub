package vn.edu.eaut.ems.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import vn.edu.eaut.ems.entity.Account;
import vn.edu.eaut.ems.entity.Booking;
import vn.edu.eaut.ems.repository.AccountRepository;
import vn.edu.eaut.ems.repository.BookingRepository;
import vn.edu.eaut.ems.repository.BuildingRepository;
import vn.edu.eaut.ems.repository.RoomRepository;

import java.util.List;

@Controller
public class AdminDashboardController {

    private final AccountRepository accountRepository;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final BuildingRepository buildingRepository;

    public AdminDashboardController(AccountRepository accountRepository, 
                                    BookingRepository bookingRepository,
                                    RoomRepository roomRepository,
                                    BuildingRepository buildingRepository) {
        this.accountRepository = accountRepository;
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.buildingRepository = buildingRepository;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model, HttpSession session) {
        Account currentUser = (Account) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            currentUser = accountRepository.findById("20231206").orElse(null);
        }
        model.addAttribute("currentUser", currentUser);

        // =========================================================================
        // 1. TÍNH TOÁN DỮ LIỆU THẬT TỪ CƠ SỞ DỮ LIỆU MYSQL (ĐÃ BỎ DOANH THU)
        // =========================================================================
        
        // Đếm tài khoản thật
        List<Account> accounts = accountRepository.findAll();
        long totalAccounts = accounts.size();
        long totalStudents = accounts.stream()
                .filter(a -> "SINHVIEN".equalsIgnoreCase(a.getVaiTro()) || "SINH_VIEN".equalsIgnoreCase(a.getVaiTro())).count();
        long totalTeachers = accounts.stream()
                .filter(a -> "ADMIN".equalsIgnoreCase(a.getVaiTro()) || "GIANG_VIEN".equalsIgnoreCase(a.getVaiTro())).count();

        // Đếm phiếu mượn thật
        List<Booking> bookings = bookingRepository.findAll();
        long totalBookings = bookings.size();
        long pendingBookings = bookings.stream()
                .filter(b -> "CHO_DUYET".equalsIgnoreCase(b.getTrangThai())).count();
        long activeBookings = bookings.stream()
                .filter(b -> "DA_DUYET".equalsIgnoreCase(b.getTrangThai()) || "UY_QUYEN".equalsIgnoreCase(b.getTrangThai())).count();
        long completedBookings = bookings.stream()
                .filter(b -> "DA_TRA".equalsIgnoreCase(b.getTrangThai()) || "DA_TRA_HO".equalsIgnoreCase(b.getTrangThai())).count();

        // Cơ sở vật chất thật (240 phòng & 5 tòa nhà)
        long totalRooms = roomRepository.count();
        long totalBuildings = buildingRepository.count();

        // 5 đơn mượn phòng mới nhất thật từ MySQL
        List<Booking> recentBookings = bookingRepository.findAllByOrderByThoiGianTaoDesc().stream()
                .limit(5)
                .toList();

        // 5 tài khoản mới nhất thật từ MySQL
        List<Account> recentAccounts = accounts.stream()
                .limit(5)
                .toList();

        // =========================================================================
        // 2. TRUYỀN DỮ LIỆU THẬT SANG VIEW
        // =========================================================================
        model.addAttribute("totalAccounts", totalAccounts);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalTeachers", totalTeachers);

        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("totalBuildings", totalBuildings);

        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("activeBookings", activeBookings);
        model.addAttribute("completedBookings", completedBookings);

        model.addAttribute("recentBookings", recentBookings);
        model.addAttribute("recentAccounts", recentAccounts);

        return "admin/dashboard";
    }
}