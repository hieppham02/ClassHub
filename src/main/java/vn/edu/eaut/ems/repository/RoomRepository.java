package vn.edu.eaut.ems.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.eaut.ems.entity.Room;

public interface RoomRepository extends JpaRepository<Room, String> {
    
}
