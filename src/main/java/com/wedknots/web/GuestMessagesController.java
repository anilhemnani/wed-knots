package com.wedknots.web;

import com.wedknots.model.Guest;
import com.wedknots.model.WeddingEvent;
import com.wedknots.repository.GuestRepository;
import com.wedknots.repository.WeddingEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Web controller for guest messaging functionality
 */
@Controller
@RequestMapping("/guest/messages")
public class GuestMessagesController {
    private static final Logger logger = LoggerFactory.getLogger(GuestMessagesController.class);

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private WeddingEventRepository weddingEventRepository;

    /**
     * Display messages for a specific event
     * GET /guest/messages/event/{eventId}
     */
    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasRole('GUEST')")
    public String viewEventMessages(@PathVariable Long eventId,
                                    @RequestParam(name = "guestId", required = false) Long guestId,
                                    Model model) {
        try {
            Guest guest = null;

            // 1) Prefer explicit guestId when provided
            if (guestId != null) {
                guest = guestRepository.findById(guestId).orElse(null);
            }

            // 2) Fallback to authenticated principal: try phone first, then email
            if (guest == null) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String principal = auth != null ? auth.getName() : null;
                if (principal != null) {
                    guest = guestRepository.findByContactPhone(principal);
                    if (guest == null) {
                        guest = guestRepository.findByContactEmail(principal);
                    }
                }
            }

            if (guest == null) {
                return "redirect:/guest/dashboard?error=Guest not found";
            }

            // Verify guest is invited to this event
            if (guest.getEventId() == null || !guest.getEventId().equals(eventId)) {
                logger.warn("Guest {} attempted to access messages for event {} they are not invited to",
                    guest.getId(), eventId);
                return "redirect:/guest/dashboard?error=You are not invited to this event";
            }

            WeddingEvent event = weddingEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

            model.addAttribute("event", event);
            model.addAttribute("guest", guest);

            logger.info("Guest {} viewing messages for event {}", guest.getId(), eventId);
            return "guest/messages";

        } catch (Exception e) {
            logger.error("Error loading messages page", e);
            return "redirect:/guest/dashboard?error=Error loading messages";
        }
    }
}
