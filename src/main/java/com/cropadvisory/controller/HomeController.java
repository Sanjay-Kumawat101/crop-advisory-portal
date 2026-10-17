package com.cropadvisory.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /** Environment label, set with APP_ENV (for example dev or test). */
    @Value("${app.env:dev}")
    private String appEnv;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping({"/", "/home"})
    public String home(Authentication authentication, Model model) {
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("USER");
        model.addAttribute("username", authentication.getName());
        model.addAttribute("role", role);
        model.addAttribute("env", appEnv);
        return "home";
    }
}
