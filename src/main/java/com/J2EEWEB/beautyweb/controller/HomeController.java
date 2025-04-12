package com.J2EEWEB.beautyweb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller

public class HomeController {
    @GetMapping("/contact")
    public String contact() {
        return "redirect:/contact.html";
    }
    @GetMapping("/about")
    public String about() {
        return "redirect:/about.html";
    }
    @GetMapping("/blog")
    public String blog() {
        return "redirect:/blog.html";
    }
    @GetMapping("/service")
    public String service() {
        return "redirect:/service.html";
    }
    @GetMapping("/home")
    public String home() {
        return "redirect:/index.html";
    }

}