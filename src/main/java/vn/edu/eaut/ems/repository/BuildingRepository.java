package vn.edu.eaut.ems.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.eaut.ems.entity.Building;

public interface BuildingRepository extends JpaRepository<Building, String> {
}