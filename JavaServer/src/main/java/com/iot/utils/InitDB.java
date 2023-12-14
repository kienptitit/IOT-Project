package com.iot.utils;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.iot.entity.Device;
import com.iot.entity.Plant;
import com.iot.entity.Role;
import com.iot.entity.User;
import com.iot.repo.DeviceRepo;
import com.iot.repo.PlantRepo;
import com.iot.repo.RoleRepo;
import com.iot.repo.UserRepo;

@Service
public class InitDB implements CommandLineRunner {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private RoleRepo roleRepo;
    @Autowired
    private PlantRepo plantRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private DeviceRepo deviceRepo;

    @Override
    public void run(String... args) throws Exception {
        // init a role and a user
        Role roleuser = Role.builder().name("ROLE_USER").build();
        roleRepo.save(roleuser);
        User user = User.builder().fullName("Nguyễn Duy Hiếu").username("user")
                .password(passwordEncoder.encode("user"))
                .accountNonExpired(true).accountNonLocked(true)
                .credentialsNonExpired(true).isEnabled(true)
                .roles(List.of(roleuser)).build();
        userRepo.save(user);

        // // init database plant
        String relativePath = System.getProperty("user.dir") + File.separator + "data";
        File dataPath = new File(relativePath);
        String[] classPath = dataPath.list();

        Arrays.sort(classPath);
        Map<String, String> map = new LinkedHashMap<>();
        for (String s : classPath) {
            String[] splt = s.split("(-)");
            map.put(splt[0].trim(), splt[1].trim());
        }
        for (Map.Entry<String, String> entry : map.entrySet()) {
            Plant plant = Plant.builder().name(entry.getKey()).scienceName(entry.getValue()).build();
            // plantService.fillData(plant);
            plantRepo.save(plant);
        }

        Device device1 = Device.builder().ip("192.168.0.150").deviceCode("device1").build();
        deviceRepo.save(device1);
    }

}
