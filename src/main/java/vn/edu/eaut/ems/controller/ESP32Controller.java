package vn.edu.eaut.ems.controller;

import java.util.Random;

import org.springframework.web.bind.annotation.*;

import vn.edu.eaut.ems.repository.BookingRepository;
import vn.edu.eaut.ems.repository.BuildingRepository;
import vn.edu.eaut.ems.service.MqttService;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class ESP32Controller {

    private final BuildingRepository buildingRepository;
    private final BookingRepository bookingRepository;
    private final MqttService mqttService;

    public ESP32Controller(BuildingRepository buildingRepository, BookingRepository bookingRepository, MqttService mqttService) {
        this.buildingRepository = buildingRepository;
        this.bookingRepository = bookingRepository;
        this.mqttService = mqttService;
    }

    @PostMapping("/led/{state}")
    public String controlLed(@PathVariable int state) {
        String command = "{\"led\": " + state + "}";
        mqttService.sendCommandToESP32("data", command);
        return command;
    }

    @PostMapping("/otp")
    public String controlOTP() {
        String newOtp = String.format("%06d", new Random().nextInt(999999));
        System.out.println("Mã OTP: " + newOtp);
        String command = "{\"otp\": " + newOtp + "}";
        mqttService.sendCommandToESP32("otp", command);
        return command;
    }
}
