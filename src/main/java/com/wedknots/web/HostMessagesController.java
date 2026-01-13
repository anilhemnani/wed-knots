package com.wedknots.web;

import com.wedknots.model.GuestMessage;
import com.wedknots.model.WeddingEvent;
import com.wedknots.repository.WeddingEventRepository;
import com.wedknots.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for hosts to view and manage WhatsApp messages from guests
 */
@Controller
@RequestMapping("/host")
public class HostMessagesController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private WeddingEventRepository weddingEventRepository;

    /**
     * View all WhatsApp messages for host's events
     * Redirects to new inbox interface
     */
    @PreAuthorize("hasRole('HOST')")
    @GetMapping("/messages")
    public String viewMessages(
            @RequestParam(required = false) Long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication,
            Model model) {

        // Redirect to new inbox interface
        return "redirect:/host/messages/inbox";
    }

    /**
     * Mark a message as read
     */
    @PreAuthorize("hasRole('HOST')")
    @PostMapping("/messages/{messageId}/mark-read")
    public String markMessageAsRead(
            @PathVariable Long messageId,
            @RequestParam(required = false) Long eventId,
            Authentication authentication) {

        messageService.markAsRead(messageId);

        String redirectUrl = "/host/messages";
        if (eventId != null) {
            redirectUrl += "?eventId=" + eventId;
        }
        return "redirect:" + redirectUrl;
    }

    /**
     * Mark all messages as read for an event
     */
    @PreAuthorize("hasRole('HOST')")
    @PostMapping("/messages/mark-all-read")
    public String markAllMessagesAsRead(
            @RequestParam Long eventId,
            Authentication authentication) {

        messageService.markAllAsReadForEvent(eventId);
        return "redirect:/host/messages?eventId=" + eventId;
    }
}

