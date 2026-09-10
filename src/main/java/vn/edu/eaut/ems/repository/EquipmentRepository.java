package vn.edu.eaut.ems.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.edu.eaut.ems.entity.Equipment;

public interface EquipmentRepository extends JpaRepository<Equipment, Integer> {
}
