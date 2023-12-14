package com.iot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iot.entity.Plant;
import com.iot.service.PlantService;

@RestController
@RequestMapping("/plant")
@CrossOrigin("*")
public class PlantRestController {
    @Autowired
    private PlantService plantService;

    @GetMapping("/get-info/{namePlant}")
    public ResponseEntity<?> getInfoPlant(@PathVariable String namePlant) {
        System.out.println(namePlant);
        Plant plant = plantService.findByName(namePlant);
        if (plant.getMaxSoil() == 0) {
            plantService.fillData(plant);
            plantService.save(plant);
        }
        System.out.println(plant.toString());
        return new ResponseEntity<>(plant, HttpStatus.OK);
    }

}
