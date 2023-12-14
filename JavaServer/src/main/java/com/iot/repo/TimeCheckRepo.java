package com.iot.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iot.entity.TimeCheck;

@Repository
public interface TimeCheckRepo extends JpaRepository<TimeCheck, Long>{

    List<TimeCheck> findAllByDeviceId(Long id);
    
}
