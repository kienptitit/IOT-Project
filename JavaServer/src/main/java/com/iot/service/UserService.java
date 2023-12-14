package com.iot.service;

import com.iot.entity.User;

public interface UserService {
    public User findByUsername(String username);
    public User save(User user);
    public User addNewUser(User user) throws Exception;
}
