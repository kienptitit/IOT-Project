package com.iot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iot.dto.WeatherDTO;
import com.iot.service.WeatherService;

@RestController
@RequestMapping("/weather")
public class WeatherRestController {
    @Autowired
    private WeatherService weatherService;

    @GetMapping
    public ResponseEntity<?> getWeather(@RequestParam String loc) {
        WeatherDTO weatherDTO = weatherService.getWeatherByLocation(loc);
        return new ResponseEntity<WeatherDTO>(weatherDTO, HttpStatus.OK);
    }
}
