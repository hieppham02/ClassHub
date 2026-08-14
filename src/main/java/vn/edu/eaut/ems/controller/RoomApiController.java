package vn.edu.eaut.ems.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import vn.edu.eaut.ems.entity.Building;
import vn.edu.eaut.ems.repository.BuildingRepository;


@RestController
@RequestMapping("/api")
public class RoomApiController {

    private final BuildingRepository buildingRepository;

    public RoomApiController(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    @GetMapping("/buildings")
    public List<Building> getAllBuildingsAndRooms() {
        return buildingRepository.findAll(); 
    }
}
