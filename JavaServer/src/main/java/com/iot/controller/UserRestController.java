package com.iot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.iot.entity.User;
import com.iot.service.UserService;

@RestController
@CrossOrigin("*")
public class UserRestController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> doRegister(@RequestBody User user) {
        try {
            User addedUser = userService.addNewUser(user);
            return ResponseEntity.ok(addedUser);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
