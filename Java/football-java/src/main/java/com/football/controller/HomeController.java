package com.football.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Home page controller.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/rankings";
    }
}
