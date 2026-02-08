package com.wedknots.web;

import com.wedknots.model.Guest;
import com.wedknots.model.WeddingEvent;
import com.wedknots.repository.GuestRepository;
import com.wedknots.repository.WeddingEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
public class EventAboutController {

    @Autowired
    private WeddingEventRepository weddingEventRepository;

    @Autowired
    private GuestRepository guestRepository;

    /**
     * Serve the about page for an event
     * URL: /events/{id}/about.html
     */
    @GetMapping("/events/{id}/about.html")
    public String getEventAbout(@PathVariable Long id, Model model, Authentication authentication) {
        return getEventPage(id, "about", model, authentication);
    }

    /**
     * Serve the travel options page for an event
     * URL: /events/{id}/travel_options.html
     */
    @GetMapping("/events/{id}/travel_options.html")
    public String getEventTravelOptions(@PathVariable Long id, Model model, Authentication authentication) {
        return getEventPage(id, "travel_options", model, authentication);
    }

    /**
     * Generic method to serve event-specific pages
     */
    private String getEventPage(Long id, String pageName, Model model, Authentication authentication) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(id);

        if (eventOpt.isEmpty()) {
            return "error/404";
        }

        WeddingEvent event = eventOpt.get();
        model.addAttribute("event", event);

        // Try to get guest info if user is authenticated
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Guest guest = guestRepository.findByContactEmail(email);
            if (guest != null && guest.getEvent().getId().equals(id)) {
                model.addAttribute("guest", guest);
            }
        }

        // Return event-specific template path
        // This will look for templates/events/{id}/{pageName}-page.html which wraps the fragment
        return "events/" + id + "/" + pageName + "-page";
    }
}



