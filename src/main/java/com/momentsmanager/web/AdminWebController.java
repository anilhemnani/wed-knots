package com.momentsmanager.web;

import com.momentsmanager.model.*;
import com.momentsmanager.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminWebController {

    @Autowired
    private WeddingEventRepository weddingEventRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        List<WeddingEvent> events = weddingEventRepository.findAll();
        model.addAttribute("events", events);
        return "admin_dashboard";
    }
}
