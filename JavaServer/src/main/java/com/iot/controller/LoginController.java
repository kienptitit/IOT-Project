package com.iot.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.iot.security.UserDetailsCusom;

@Controller
public class LoginController {
    @GetMapping("/login-page")
    public String loginPage() {
        return "login";
    }

    @GetMapping(value = "/403")
    public String page403() {
        return "403";
    }

    @GetMapping("/login-success")
    public String loginSuccess(@AuthenticationPrincipal UserDetailsCusom user) {
        if (user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"))) {
            System.out.println("login success");
            return "redirect:/devices";
        }
        return null;
    }

    @GetMapping("/register")
    public String registerPage(){
        return "register";
    }

}
