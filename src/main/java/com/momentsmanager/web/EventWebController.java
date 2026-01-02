package com.momentsmanager.web;

import com.momentsmanager.model.WeddingEvent;
import com.momentsmanager.repository.WeddingEventRepository;
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
    private com.momentsmanager.repository.HostRepository hostRepository;

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
    public String createEvent(@ModelAttribute WeddingEvent event) {
        weddingEventRepository.save(event);
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

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/edit")
    public String updateEvent(@PathVariable Long id, @ModelAttribute WeddingEvent event) {
        event.setId(id);
        weddingEventRepository.save(event);
        return "redirect:/events";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    public String deleteEvent(@PathVariable Long id) {
        weddingEventRepository.deleteById(id);
        return "redirect:/events";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @GetMapping("/{id}/whatsapp-config")
    public String showWhatsAppConfig(@PathVariable Long id, Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(id);
        if (eventOpt.isPresent()) {
            model.addAttribute("event", eventOpt.get());
            return "whatsapp_config";
        }
        return "redirect:/events";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PostMapping("/{id}/whatsapp-config")
    public String updateWhatsAppConfig(@PathVariable Long id,
                                       @ModelAttribute WeddingEvent updatedEvent,
                                       Model model,
                                       org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(id);
        if (eventOpt.isPresent()) {
            WeddingEvent event = eventOpt.get();

            // Update WhatsApp configuration fields
            event.setWhatsappApiEnabled(updatedEvent.getWhatsappApiEnabled());
            event.setWhatsappPhoneNumberId(updatedEvent.getWhatsappPhoneNumberId());
            event.setWhatsappBusinessAccountId(updatedEvent.getWhatsappBusinessAccountId());
            event.setWhatsappAccessToken(updatedEvent.getWhatsappAccessToken());
            event.setWhatsappApiVersion(updatedEvent.getWhatsappApiVersion());
            event.setWhatsappVerifyToken(updatedEvent.getWhatsappVerifyToken());

            weddingEventRepository.save(event);

            redirectAttributes.addFlashAttribute("successMessage",
                "WhatsApp Cloud API configuration saved successfully!");
            return "redirect:/events/" + id + "/whatsapp-config";
        }
        return "redirect:/events";
    }
}

