package com.wedknots.web;

import com.wedknots.model.WeddingEvent;
import com.wedknots.repository.WeddingEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/host")
public class HostDashboardController {

    @Autowired
    private WeddingEventRepository weddingEventRepository;

    @PreAuthorize("hasRole('HOST')")
    @GetMapping("/dashboard")
    public String hostDashboard(Model model) {
        // Get all events (in a production system, filter by host user)
        List<WeddingEvent> events = weddingEventRepository.findAll();
        model.addAttribute("events", events);
        return "host_dashboard";
    }
}

