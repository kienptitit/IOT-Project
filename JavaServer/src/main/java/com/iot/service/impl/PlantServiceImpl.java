package com.iot.service.impl;

import java.util.List;
import java.util.Optional;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iot.entity.Plant;
import com.iot.repo.PlantRepo;
import com.iot.service.PlantService;

@Service
public class PlantServiceImpl implements PlantService {
    @Autowired
    private PlantRepo plantRepo;

    @Override
    public Plant findByName(String name) {
        Optional<Plant> plantOptional = plantRepo.findByName(name);
        if (plantOptional.isPresent())
            return plantOptional.get();
        else
            return null;
    }

    @Override
    public Plant findByScienceName(String scienceName) {
        Optional<Plant> plantOptional = plantRepo.findByScienceName(scienceName);
        if (plantOptional.isPresent())
            return plantOptional.get();
        else
            return null;
    }

    @Override
    public List<Plant> findAll() {
        List<Plant> plants = plantRepo.findAll();
        return plants;
    }

    @Override
    public Plant findById(Long id) {
        Optional<Plant> plantOptional = plantRepo.findById(id);
        return plantOptional.get();
    }

    @Override
    public void fillData(Plant plant) {
        String authToken = "Token 924e7bb02dadb8b57741f7c2eb5a5aea915cad13";
        // dau cach = %20
        String plantId = plant.getScienceName().replaceAll("\\s+", "%20");
        System.out.println(plantId);
        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {
            String apiUrl = "https://open.plantbook.io/api/v1/plant/detail/" + plantId + "/";
            HttpGet httpGet = new HttpGet(apiUrl);
            httpGet.setHeader("Authorization", authToken);

            CloseableHttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();

            if (entity != null) {

                String responseString = EntityUtils.toString(entity);
                System.out.println(responseString);
                JsonObject jsonObject = JsonParser.parseString(responseString).getAsJsonObject();
                if (jsonObject.get("errors") == null) {

                    int max_soil_moist = jsonObject.get("max_soil_moist").getAsInt();
                    int min_soil_moist = jsonObject.get("min_soil_moist").getAsInt();
                    int max_temp = jsonObject.get("max_temp").getAsInt();
                    int min_temp = jsonObject.get("min_temp").getAsInt();
                    plant.setMaxSoil(max_soil_moist);
                    plant.setMinSoil(min_soil_moist);
                    plant.setMaxTemperature(max_temp);
                    plant.setMinTemperature(min_temp);
                } else {
                    plant.setMaxSoil(60);
                    plant.setMinSoil(15);
                    plant.setMaxTemperature(32);
                    plant.setMinTemperature(8);
                }
            }
        } catch (UnsupportedOperationException ex) {
            System.out.println("khong tim thay");
            plant.setMaxSoil(60);
            plant.setMinSoil(15);
            plant.setMaxTemperature(32);
            plant.setMinTemperature(8);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void save(Plant plant) {
        plantRepo.save(plant);
    }

}
