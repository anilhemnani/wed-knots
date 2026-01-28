package com.wedknots.web;

import com.wedknots.model.Guest;
import com.wedknots.model.WeddingEvent;
import com.wedknots.repository.GuestRepository;
import com.wedknots.repository.WeddingEventRepository;
import com.wedknots.template.TemplateVariableProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API for template variable operations
 */
@RestController
@RequestMapping("/api/templates")
public class TemplateVariableController {

    @Autowired
    private TemplateVariableProcessor templateProcessor;

    @Autowired
    private WeddingEventRepository eventRepository;

    @Autowired
    private GuestRepository guestRepository;

    /**
     * Get available template variables for an event
     */
    @GetMapping("/variables/event/{eventId}")
    public ResponseEntity<?> getEventVariables(@PathVariable Long eventId) {
        WeddingEvent event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, String> variables = templateProcessor.getAvailableVariables(event, null);

        Map<String, Object> response = new HashMap<>();
        response.put("variables", variables);
        response.put("examples", getVariableExamples());

        return ResponseEntity.ok(response);
    }

    /**
     * Get available template variables for an event and guest
     */
    @GetMapping("/variables/event/{eventId}/guest/{guestId}")
    public ResponseEntity<?> getEventGuestVariables(
            @PathVariable Long eventId,
            @PathVariable Long guestId) {

        WeddingEvent event = eventRepository.findById(eventId).orElse(null);
        Guest guest = guestRepository.findById(guestId).orElse(null);

        if (event == null || guest == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, String> variables = templateProcessor.getAvailableVariables(event, guest);

        Map<String, Object> response = new HashMap<>();
        response.put("variables", variables);
        response.put("examples", getVariableExamples());

        return ResponseEntity.ok(response);
    }

    /**
     * Preview template with variables replaced
     */
    @PostMapping("/preview/event/{eventId}/guest/{guestId}")
    public ResponseEntity<?> previewTemplate(
            @PathVariable Long eventId,
            @PathVariable Long guestId,
            @RequestBody Map<String, String> request) {

        WeddingEvent event = eventRepository.findById(eventId).orElse(null);
        Guest guest = guestRepository.findById(guestId).orElse(null);

        if (event == null || guest == null) {
            return ResponseEntity.notFound().build();
        }

        String template = request.get("template");
        if (template == null) {
            return ResponseEntity.badRequest().body("Template is required");
        }

        String processed = templateProcessor.process(template, event, guest);

        Map<String, Object> response = new HashMap<>();
        response.put("original", template);
        response.put("processed", processed);
        response.put("variables", templateProcessor.getAvailableVariables(event, guest));

        return ResponseEntity.ok(response);
    }

    /**
     * Get list of example variables with descriptions
     */
    private Map<String, String> getVariableExamples() {
        Map<String, String> examples = new HashMap<>();

        // Event variables
        examples.put("{{event.name}}", "Event name");
        examples.put("{{event.bride}}", "Bride's name");
        examples.put("{{event.groom}}", "Groom's name");
        examples.put("{{event.date}}", "Event date (formatted)");
        examples.put("{{event.location}}", "Event location");
        examples.put("{{event.venue}}", "Event venue");
        examples.put("{{event.time}}", "Event time");
        examples.put("{{event.dress_code}}", "Dress code");

        // Guest variables
        examples.put("{{guest.name}}", "Guest's full name");
        examples.put("{{guest.first_name}}", "Guest's first name");
        examples.put("{{guest.family_name}}", "Guest's family name");
        examples.put("{{guest.email}}", "Guest's email");
        examples.put("{{guest.phone}}", "Guest's phone number");

        // Attendee variables
        examples.put("{{attendee.count}}", "Number of attendees");
        examples.put("{{attendee.names}}", "All attendee names (comma-separated)");
        examples.put("{{attendee.first}}", "First attendee name");

        return examples;
    }
}

