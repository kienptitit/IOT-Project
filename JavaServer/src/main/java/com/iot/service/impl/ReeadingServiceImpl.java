package com.iot.service.impl;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iot.entity.Reading;
import com.iot.repo.ReadingRepo;
import com.iot.service.ReadingService;
@Service
public class ReeadingServiceImpl implements ReadingService {
    @Autowired
    private ReadingRepo readingRepo;

    @Override
    public List<Reading> findAllByDeviceId(Long deviceId) {
        return readingRepo.findAllByDeviceId(deviceId);
    }

    @Override
    public void add(Reading reading) {
        readingRepo.save(reading);
    }

    @Override
    public Reading findByTimeAndDeviceId(LocalTime time, Long deviceId) {
        Optional<Reading> reading = readingRepo.findByTimeAndDeviceId(time, deviceId);
        if(reading.isPresent()){
            return reading.get();
        }else{
            return null;
        }
    }

}
