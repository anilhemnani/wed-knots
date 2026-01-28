package com.wedknots.web;

import com.wedknots.model.WeddingEvent;
import com.wedknots.repository.WeddingEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Admin controller for managing wedding events
 */
@Controller
@RequestMapping("/admin/events")
public class AdminEventController {

    @Autowired
    private WeddingEventRepository weddingEventRepository;

    /**
     * List all wedding events
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public String listEvents(Model model) {
        List<WeddingEvent> events = weddingEventRepository.findAll();
        model.addAttribute("events", events);
        return "admin_event_list";
    }

    /**
     * Show form to create new event
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String newEvent(Model model) {
        model.addAttribute("event", new WeddingEvent());
        return "admin_event_form";
    }

    /**
     * Create new event with subdomain validation
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/new")
    public String createEvent(@ModelAttribute WeddingEvent event,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        if (!isValid(event, model)) {
            model.addAttribute("event", event);
            return "admin_event_form";
        }

        event.setSubdomain(event.getSubdomain().trim());

        if (weddingEventRepository.findBySubdomain(event.getSubdomain()).isPresent()) {
            model.addAttribute("event", event);
            model.addAttribute("error", "Subdomain is already taken. Please choose a different one.");
            return "admin_event_form";
        }

        weddingEventRepository.save(event);
        redirectAttributes.addFlashAttribute("successMessage",
                "Event created successfully! Public page: /public/" + event.getSubdomain());
        return "redirect:/admin/events";
    }

    /**
     * Show form to edit event
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/edit")
    public String editEvent(@PathVariable Long id, Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(id);
        if (eventOpt.isPresent()) {
            model.addAttribute("event", eventOpt.get());
            return "admin_event_form";
        }
        return "redirect:/admin/events";
    }

    /**
     * Update event (subdomain is immutable)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/edit")
    public String updateEvent(@PathVariable Long id, @ModelAttribute WeddingEvent event,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        Optional<WeddingEvent> existingOpt = weddingEventRepository.findById(id);
        if (existingOpt.isPresent()) {
            WeddingEvent existing = existingOpt.get();
            event.setId(id);
            event.setSubdomain(existing.getSubdomain());

            if (!isValid(event, model)) {
                model.addAttribute("event", event);
                return "admin_event_form";
            }

            weddingEventRepository.save(event);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Event updated successfully! (Subdomain cannot be changed after creation)");
            return "redirect:/admin/events/" + id;
        }
        return "redirect:/admin/events";
    }

    /**
     * View event details
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public String viewEvent(@PathVariable Long id, Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(id);
        if (eventOpt.isPresent()) {
            model.addAttribute("event", eventOpt.get());
            return "admin_event_view";
        }
        return "redirect:/admin/events";
    }

    /**
     * Delete event
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    public String deleteEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(id);
        if (eventOpt.isPresent()) {
            String eventName = eventOpt.get().getName();
            weddingEventRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Event '" + eventName + "' deleted successfully.");
        }
        return "redirect:/admin/events";
    }

    private boolean isValid(WeddingEvent event, Model model) {
        if (event.getName() == null || event.getName().trim().isEmpty()) {
            model.addAttribute("error", "Event name is required");
            return false;
        }
        if (event.getBrideName() == null || event.getBrideName().trim().isEmpty()) {
            model.addAttribute("error", "Bride name is required");
            return false;
        }
        if (event.getGroomName() == null || event.getGroomName().trim().isEmpty()) {
            model.addAttribute("error", "Groom name is required");
            return false;
        }
        if (event.getDate() == null) {
            model.addAttribute("error", "Event date is required");
            return false;
        }
        if (event.getSubdomain() == null || event.getSubdomain().trim().isEmpty()) {
            model.addAttribute("error", "Subdomain is required");
            return false;
        }
        if (!event.getSubdomain().trim().matches("^[a-z0-9-]+$")) {
            model.addAttribute("error", "Subdomain must contain only lowercase letters, numbers, and hyphens.");
            return false;
        }
        return true;
    }
}
