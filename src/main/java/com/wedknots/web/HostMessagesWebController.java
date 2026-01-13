package com.wedknots.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Host web controller for WhatsApp message management UI (new inbox interface)
 */
@Controller
@RequestMapping("/host/messages")
@PreAuthorize("hasRole('HOST')")
public class HostMessagesWebController {

    /**
     * Display host messages inbox page (new conversation-style interface)
     */
    @GetMapping("/inbox")
    public String showMessagesInbox(Model model) {
        return "host/messages_inbox";
    }
}

