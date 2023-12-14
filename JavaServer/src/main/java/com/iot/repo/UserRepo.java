package com.iot.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iot.entity.User;

@Repository
public interface UserRepo extends JpaRepository<User, Long>{
    public Optional<User> findByUsername(String username);
}
