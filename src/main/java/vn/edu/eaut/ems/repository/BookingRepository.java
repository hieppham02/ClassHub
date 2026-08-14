package vn.edu.eaut.ems.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.eaut.ems.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

}
