
package com.iot.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iot.dto.WeatherDTO;
import com.iot.service.WeatherService;

@Service
public class WeatherServiceImpl implements WeatherService {
    private RestTemplate restTemplate;
    private String URI = "https://api.openweathermap.org/data/2.5/weather";
    private String API_ID = "92989c39e40d60d3a17088f2407ac3eb";

    public WeatherServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public WeatherDTO getWeatherByLocation(String loc) {
        String resp = this.restTemplate.getForObject(this.url(loc), String.class);
        Map<String, Object> respMap = jsonToMap(resp);
        Map<String, Object> mainMap = jsonToMap(respMap.get("main").toString());

        int temp = (int) Math.round((double) mainMap.get("temp") - 273);
        int humi = (int) Math.round((double) mainMap.get("humidity"));
        return WeatherDTO.builder().temperature(temp)
                .humidity(humi)
                .build();
    }

    private String url(String loc) {
        return String.format(URI.concat("?q=%s").concat("&appid=%s"), loc, API_ID);
    }

    private Map<String, Object> jsonToMap(String str) {
        Map<String, Object> map = new Gson().fromJson(str, new TypeToken<HashMap<String, Object>>() {
        }.getType());
        return map;
    }
}
