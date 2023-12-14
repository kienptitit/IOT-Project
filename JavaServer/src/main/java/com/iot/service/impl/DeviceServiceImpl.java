package com.iot.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iot.entity.Device;
import com.iot.entity.TimeCheck;
import com.iot.repo.DeviceRepo;
import com.iot.service.DeviceService;

@Service
public class DeviceServiceImpl implements DeviceService {
    @Autowired
    private DeviceRepo deviceRepo;

    @Override
    public List<Device> findAllByUserId(Long userid) {
        return deviceRepo.findAllByUserId(userid);
    }

    @Override
    public Device save(Device device1) {
        Device device = deviceRepo.findById(device1.getId()).get();
        device.setUser(device1.getUser());
        device.setName(device1.getName());
        device.setDescription(device1.getDescription());
        
        List<TimeCheck> check = device.getTimeChecks();
        if (check == null || check.isEmpty() || check.size() == 0) {
            System.out.println("time check null");
            List<TimeCheck> timeChecks = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                timeChecks.add(TimeCheck.builder().device(device).build());
            }
            device.setTimeChecks(timeChecks);
            return deviceRepo.save(device);
        } else {
            // update
            List<TimeCheck> timeChecks = device.getTimeChecks();
            timeChecks.forEach(tc -> tc.setDevice(device));
            device.setTimeChecks(timeChecks);
            return deviceRepo.save(device);
        }
    }

    @Override
    public Optional<Device> findByIp(String ip) {
        return deviceRepo.findByIp(ip);
    }

    @Override
    public Optional<Device> findById(Long id) {
        return deviceRepo.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        Device device = deviceRepo.findById(id).get();
        System.out.println(device.toString());
        device.setPumpTimeMinute(1);
        device.setSoilThreshold(50);
        device.setTemperatureThreshold(20);
        device.setPlant(null);
        device.setUser(null);
        device.setDescription(null);
        device.setName("unknown");
        deviceRepo.save(device);
    }

    @Override
    public List<Device> findAll() {
        return deviceRepo.findAll();
    }

    @Override
    public Optional<Device> findByDeviceCode(String deviceCode) {
        return deviceRepo.findByDeviceCode(deviceCode);
    }

    @Override
    public Device update(Device device) {
        List<TimeCheck> timeChecks = device.getTimeChecks();
        timeChecks.forEach(tc -> tc.setDevice(device));
        device.setTimeChecks(timeChecks);
        return deviceRepo.save(device);
    }

}
