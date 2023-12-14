package com.iot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping(value = { "/", "/home" })
    public String goHome(Model model) {
        model.addAttribute("page", "home");
        return "redirect:/devices";
    }

}
