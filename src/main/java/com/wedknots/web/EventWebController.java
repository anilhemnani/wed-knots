 package com.wedknots.web;

import com.wedknots.model.WeddingEvent;
import com.wedknots.repository.WeddingEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/events")
public class EventWebController {

    @Autowired
    private WeddingEventRepository weddingEventRepository;

    @Autowired
    private com.wedknots.repository.HostRepository hostRepository;

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @GetMapping
    public String listEvents(Model model, org.springframework.security.core.Authentication authentication) {
        List<WeddingEvent> events;

        // If user is a host, show only their events
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_HOST"))) {
            // Get host email from authentication
            String email = authentication.getName();
            events = weddingEventRepository.findByHostEmail(email);
        } else {
            // Admin can see all events
            events = weddingEventRepository.findAll();
        }

        model.addAttribute("events", events);
        return "event_list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String newEvent(Model model) {
        model.addAttribute("event", new WeddingEvent());
        return "event_form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/new")
    public String createEvent(@ModelAttribute WeddingEvent event,
                            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes,
                            Model model) {
        // Validate subdomain is provided and unique
        if (event.getSubdomain() == null || event.getSubdomain().trim().isEmpty()) {
            model.addAttribute("event", event);
            model.addAttribute("error", "Subdomain is required");
            return "event_form";
        }

        // Check if subdomain is already taken
        if (weddingEventRepository.findBySubdomain(event.getSubdomain()).isPresent()) {
            model.addAttribute("event", event);
            model.addAttribute("error", "Subdomain is already taken. Please choose a different one.");
            return "event_form";
        }

        // Validate subdomain format (alphanumeric and hyphens only)
        if (!event.getSubdomain().matches("^[a-z0-9-]+$")) {
            model.addAttribute("event", event);
            model.addAttribute("error", "Subdomain must contain only lowercase letters, numbers, and hyphens.");
            return "event_form";
        }

        weddingEventRepository.save(event);
        redirectAttributes.addFlashAttribute("successMessage",
                "Event created successfully! Public page: /public/" + event.getSubdomain());
        return "redirect:/events";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @GetMapping("/{id}")
    public String viewEvent(@PathVariable Long id, Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(id);
        if (eventOpt.isPresent()) {
            model.addAttribute("event", eventOpt.get());
            return "event_view";
        }
        return "redirect:/events";
    }

    /**
     * Edit event form - Admin only
     * Hosts cannot edit events (prevents subdomain tampering)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/edit")
    public String editEvent(@PathVariable Long id, Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(id);
        if (eventOpt.isPresent()) {
            model.addAttribute("event", eventOpt.get());
            return "event_form";
        }
        return "redirect:/events";
    }

    /**
     * Update event - Admin only
     * Hosts cannot update events (prevents subdomain tampering)
     * Subdomain is always preserved from the original event
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/edit")
    public String updateEvent(@PathVariable Long id, @ModelAttribute WeddingEvent event,
                            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Optional<WeddingEvent> existingOpt = weddingEventRepository.findById(id);
        if (existingOpt.isPresent()) {
            WeddingEvent existing = existingOpt.get();

            // Prevent subdomain modification - keep the original subdomain
            event.setId(id);
            event.setSubdomain(existing.getSubdomain());

            // Copy all editable fields
            existing.setName(event.getName());
            existing.setDate(event.getDate());
            existing.setBrideName(event.getBrideName());
            existing.setGroomName(event.getGroomName());
            existing.setExpectedGuestArrivalDate(event.getExpectedGuestArrivalDate());
            existing.setExpectedGuestDepartureDate(event.getExpectedGuestDepartureDate());
            existing.setPreferredTravelAirport(event.getPreferredTravelAirport());
            existing.setPreferredTravelStation(event.getPreferredTravelStation());
            existing.setPlace(event.getPlace());
            existing.setDefaultMaxAllowedAttendees(event.getDefaultMaxAllowedAttendees());
            existing.setAboutLocationUrl(event.getAboutLocationUrl());

            weddingEventRepository.save(existing);
            redirectAttributes.addFlashAttribute("successMessage", "Event updated successfully!");
            return "redirect:/events/" + id;
        }
        return "redirect:/events";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    public String deleteEvent(@PathVariable Long id) {
        weddingEventRepository.deleteById(id);
        return "redirect:/events";
    }
}

