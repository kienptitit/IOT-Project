package com.iot.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iot.entity.Plant;

public interface PlantRepo extends JpaRepository<Plant, Long>{
    Optional<Plant> findByName(String name);
    Optional<Plant> findByScienceName(String scienceName);
    
}
