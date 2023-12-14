package com.iot.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.iot.entity.User;
import com.iot.repo.UserRepo;
import com.iot.service.RoleService;
import com.iot.service.UserService;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleService roleService;

    @Override
    public User findByUsername(String username) {
        return userRepo.findByUsername(username).get();
    }

    @Override
    public User save(User user) {
        return userRepo.save(user);
    }

    @Override
    public User addNewUser(User user) throws Exception {
        Optional<User> user1 = userRepo.findByUsername(user.getUsername());
        if(user1.isPresent()){
            throw new Exception("Username already exists!");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(new ArrayList<>(List.of(roleService.getOrCreate("ROLE_USER"))));
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true);
        return userRepo.save(user);
    }

}
