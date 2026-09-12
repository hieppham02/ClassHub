package vn.edu.eaut.ems.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.edu.eaut.ems.entity.Account;
import vn.edu.eaut.ems.entity.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    // 1. Dùng cho SINH VIÊN xem lịch sử của chính mình (Code cũ của bạn giữ nguyên)
    List<Booking> findByAccountOrderByThoiGianTaoDesc(Account account);

    // 2. Dùng cho ADMIN: Lấy tất cả đơn mượn, đưa đơn mới đăng ký lên đầu danh sách
    List<Booking> findAllByOrderByThoiGianTaoDesc();
}