package com.momentsmanager.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/guest")
public class GuestDashboardController {

    @PreAuthorize("hasRole('GUEST')")
    @GetMapping("/dashboard")
    public String guestDashboard(Model model) {
        // Simple guest dashboard - can be enhanced later with guest-specific data
        return "guest_dashboard";
    }
}

