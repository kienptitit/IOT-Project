package com.iot.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.iot.entity.Device;
import com.iot.entity.Plant;
import com.iot.security.UserDetailsCusom;
import com.iot.service.DeviceService;
import com.iot.service.PlantService;

@Controller
public class DeviceController {
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private PlantService plantService;

    @GetMapping(value = "/device/add")
    public String goAddDevicePage(@RequestParam(name = "deviceCode") Optional<String> deviceCode, Model model,
            @AuthenticationPrincipal UserDetailsCusom userDetails) {
        if (deviceCode.isPresent()) {
            Device device = deviceService.findByDeviceCode(deviceCode.get()).orElse(null);
            if (device == null) {
                model.addAttribute("errorMsg", "Kiểm tra lại Code");
            } else if (device.getUser() == null) {
                model.addAttribute("device", device);
            } else {
                // loi trung ip
                model.addAttribute("errorMsg", "Kiểm tra lại Code hoặc đã có người đăng ký");
            }
        }
        model.addAttribute("fullname", userDetails.getFullName());
        return "add-device";
    }

    @GetMapping(value = "/device/{id}")
    public String goDevicePage(@PathVariable Long id, Model model,
            @AuthenticationPrincipal UserDetailsCusom userDetails) {
        Device device = deviceService.findById(id).get();
        List<Plant> plants = plantService.findAll();
        model.addAttribute("plants", plants);
        model.addAttribute("device", device);
        model.addAttribute("fullname", userDetails.getFullName());
        return "manage-device";

    }

    @GetMapping(value = "/devices")
    public String goDevicesPage(Model model, @AuthenticationPrincipal UserDetailsCusom userDetails) {
        System.out.println("59: " + userDetails.getFullName());
        if (userDetails != null) {
            List<Device> devices = deviceService.findAllByUserId(userDetails.getId());
            if (devices.size() > 0) {
                model.addAttribute("devices", devices);
                System.out.println("64: " + userDetails.getFullName());
            }
            model.addAttribute("fullname", userDetails.getFullName());
        }

        model.addAttribute("page", "device");
        return "devices";
    }

}
