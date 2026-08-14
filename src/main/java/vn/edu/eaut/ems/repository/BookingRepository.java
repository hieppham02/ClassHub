package vn.edu.eaut.ems.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.eaut.ems.entity.Account;
import vn.edu.eaut.ems.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByAccountOrderByThoiGianTaoDesc(Account account);
}
