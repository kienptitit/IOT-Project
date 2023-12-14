package com.iot.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.iot.entity.User;
import com.iot.repo.UserRepo;

public class UserDetailsServiceCustom implements UserDetailsService{
    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username).get();
        if(user == null){
            var exception = new UsernameNotFoundException("Username not found");
            exception.printStackTrace();
            throw exception;
        }else{
            UserDetailsCusom userDetailsCusom = new UserDetailsCusom(user);
            return userDetailsCusom;
        }
    }
    
    
}
