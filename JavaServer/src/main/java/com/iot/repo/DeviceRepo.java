package com.iot.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iot.entity.Device;

@Repository
public interface DeviceRepo extends JpaRepository<Device, Long>{
    public List<Device> findAllByUserId(Long userid);
    public Optional<Device> findByIp(String ip);
    public Optional<Device> findByDeviceCode(String deviceCode);
}
