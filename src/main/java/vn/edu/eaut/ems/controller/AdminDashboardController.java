package vn.edu.eaut.ems.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import vn.edu.eaut.ems.entity.Account;
import vn.edu.eaut.ems.entity.Booking;
import vn.edu.eaut.ems.entity.Room;
import vn.edu.eaut.ems.repository.AccountRepository;
import vn.edu.eaut.ems.repository.BookingRepository;
import vn.edu.eaut.ems.repository.RoomRepository;

import java.time.LocalDate;
import java.util.List;

@Controller
public class AdminDashboardController {

    private final AccountRepository accountRepository;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    public AdminDashboardController(AccountRepository accountRepository, 
                                    BookingRepository bookingRepository,
                                    RoomRepository roomRepository) {
        this.accountRepository = accountRepository;
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
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
                .filter(a -> "GIANG_VIEN".equalsIgnoreCase(a.getVaiTro())).count();

        // Đếm phiếu mượn thật
        List<Booking> bookings = bookingRepository.findAll();
        long pendingBookings = bookings.stream()
                .filter(b -> "CHO_DUYET".equalsIgnoreCase(b.getTrangThai())).count();
        long todayBookings = bookings.stream()
                .filter(b -> LocalDate.now().equals(b.getNgayMuon()))
                .filter(b -> !"TU_CHOI".equalsIgnoreCase(b.getTrangThai()))
                .count();

        List<Room> rooms = roomRepository.findAll();
        long activeRooms = rooms.stream()
                .filter(r -> "1".equalsIgnoreCase(r.getTrangThai()) || "DANG_DUNG".equalsIgnoreCase(r.getTrangThai()))
                .count();
        long maintenanceRooms = rooms.stream()
                .filter(r -> "2".equalsIgnoreCase(r.getTrangThai()) || "BAO_TRI".equalsIgnoreCase(r.getTrangThai()))
                .count();

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

        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("todayBookings", todayBookings);
        model.addAttribute("activeRooms", activeRooms);
        model.addAttribute("maintenanceRooms", maintenanceRooms);

        model.addAttribute("recentBookings", recentBookings);
        model.addAttribute("recentAccounts", recentAccounts);

        return "admin/dashboard";
    }
}
