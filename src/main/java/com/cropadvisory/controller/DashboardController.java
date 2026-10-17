package com.cropadvisory.controller;

import com.cropadvisory.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Access is restricted to ADMIN in SecurityConfig (/dashboard/**). */
@Controller
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("summary", service.summary());
        return "dashboard";
    }
}