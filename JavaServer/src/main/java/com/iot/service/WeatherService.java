package com.iot.service;

import com.iot.dto.WeatherDTO;

public interface WeatherService {
    public WeatherDTO getWeatherByLocation(String loc);
}
