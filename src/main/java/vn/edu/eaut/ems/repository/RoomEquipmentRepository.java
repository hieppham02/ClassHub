package vn.edu.eaut.ems.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.eaut.ems.entity.RoomEquipment;
import vn.edu.eaut.ems.entity.RoomEquipmentId;

public interface RoomEquipmentRepository extends JpaRepository<RoomEquipment, RoomEquipmentId> {
}
