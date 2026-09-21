package vn.edu.eaut.ems.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.eaut.ems.entity.Equipment;

import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Integer> {
    boolean existsByTenThietBiIgnoreCase(String tenThietBi);
    Optional<Equipment> findByTenThietBiIgnoreCase(String tenThietBi);
}
