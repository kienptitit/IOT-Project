package com.iot.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iot.entity.Role;
import com.iot.repo.RoleRepo;
import com.iot.service.RoleService;
@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleRepo roleRepo;

    @Override
    public Role getOrCreate(String name) {
        Optional<Role> role = roleRepo.findByName(name);
        if (role.isPresent()) {
            return role.get();
        } else {
            Role newRole = Role.builder().name(name).build();
            return roleRepo.save(newRole);
        }
    }

}
