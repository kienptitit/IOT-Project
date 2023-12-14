package com.iot.service;

import java.util.List;
import java.util.Optional;

import com.iot.entity.Device;

public interface DeviceService {
    public List<Device> findAllByUserId(Long userid);
    public List<Device> findAll();
    public Device save(Device device);
    public Device update(Device device);
    public Optional<Device> findByIp(String ip);
    public Optional<Device> findByDeviceCode(String deviceCode);
    public Optional<Device> findById(Long id);
    public void deleteById(Long id);
}
