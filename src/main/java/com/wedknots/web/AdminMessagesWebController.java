package com.wedknots.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Admin web controller for message management UI
 */
@Controller
@RequestMapping("/admin/messages")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMessagesWebController {

    /**
     * Display admin messages page
     */
    @GetMapping
    public String showMessagesPage(Model model) {
        return "admin/messages";
    }
}

