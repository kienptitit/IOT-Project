package com.iot.service;



import java.util.List;

import com.iot.entity.Plant;

public interface PlantService {
    Plant findByName(String name);
    Plant findByScienceName(String scienceName);
    List<Plant> findAll();
    Plant findById(Long id);
    void fillData(Plant plant);
    void save(Plant plant);
}
