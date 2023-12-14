package com.iot.controller;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iot.entity.Device;
import com.iot.service.DeviceService;
import com.iot.service.PlantService;
import com.iot.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/device")
@CrossOrigin("*")
public class DeviceRestController {
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private UserService userService;
    @Autowired
    private PlantService plantService;

    @PostMapping
    public ResponseEntity<?> doAddDevice(@RequestBody @Valid Device device, Principal principal) {
        System.out.println(device.toString());
        device.setUser(userService.findByUsername(principal.getName()));
        return new ResponseEntity<Device>(device = deviceService.save(device), HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<?> doUpdateDevice(@RequestBody @Valid Device device, Principal principal) {
        device.setUser(userService.findByUsername(principal.getName()));
        Device device2 = deviceService.update(device);
        return ResponseEntity.ok(device2);
    }

    @PutMapping("/change-plant")
    public ResponseEntity<?> doChangePlant(@RequestBody Map<String, Long> data) {
        Optional<Device> deviceOptional = deviceService.findById(data.get("deviceId"));
        if (deviceOptional.isPresent()) {
            Device device = deviceOptional.get();
            device.setPlant(plantService.findById(data.get("plantId")));
            deviceService.update(device);
        }
        return ResponseEntity.ok("OK");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> doDeleteDevice(@PathVariable Long id) {
        Optional<Device> optional = deviceService.findById(id);
        return optional.map((dev) -> {
            deviceService.deleteById(id);
            return ResponseEntity.ok(dev);

        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
