package com.iot.repo;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iot.entity.Reading;

@Repository
public interface ReadingRepo extends JpaRepository<Reading, Long> {
    List<Reading> findAllByDeviceId(Long deviceId);
    Optional<Reading> findByTimeAndDeviceId(LocalTime time, Long deviceId);
}
