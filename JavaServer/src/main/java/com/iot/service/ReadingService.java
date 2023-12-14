package com.iot.service;

import java.time.LocalTime;
import java.util.List;

import com.iot.entity.Reading;

public interface ReadingService {
    List<Reading> findAllByDeviceId(Long deviceId);
    void add(Reading reading);
    Reading findByTimeAndDeviceId(LocalTime time, Long deviceId);
}
