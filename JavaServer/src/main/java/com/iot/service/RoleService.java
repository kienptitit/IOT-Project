package com.iot.service;

import com.iot.entity.Role;

public interface RoleService {
    Role getOrCreate(String name);
}
