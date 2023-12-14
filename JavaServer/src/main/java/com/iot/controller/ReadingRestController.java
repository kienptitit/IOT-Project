package com.iot.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iot.entity.Device;
import com.iot.entity.Reading;
import com.iot.service.DeviceService;
import com.iot.service.ReadingService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin("*")
@RequestMapping("/reading")
public class ReadingRestController {
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private ReadingService readingService;

    private static final Logger log = LoggerFactory.getLogger(ReadingRestController.class);

    @PostMapping
    public ResponseEntity<String> doAddReading(@RequestBody Reading reading, HttpServletRequest request) {
        String deviceIP = getClientIp(request);
        Device device = deviceService.findByIp(deviceIP).get();
        Reading reading1 = readingService.findByTimeAndDeviceId(reading.getTime(), device.getId());
        if (reading1 == null) {
            reading.setDevice(device);
            readingService.add(reading);
        }
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

    private String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
            System.out.println("cach 1");
            clientIp = request.getHeader("Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
            System.out.println("cach 2");
            clientIp = request.getHeader("WL-Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.length() == 0 || "unknown".equalsIgnoreCase(clientIp)) {
            System.out.println("cach 3");
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }

    @GetMapping("/all/{id}")
    public ResponseEntity<?> getAllReadingByDeviceID(@PathVariable Long id) {
        log.info(">> GET ALL READING BY DEVICE ID: {}", id);
        List<Reading> readings = readingService.findAllByDeviceId(id);
        return new ResponseEntity<>(readings, HttpStatus.OK);
    }

}
