package com.wedknots.web;

import com.wedknots.model.WeddingEvent;
import com.wedknots.repository.WeddingEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for managing RSVP invitation UI and template configuration
 * Handles the creation and sending of WhatsApp RSVP template invitations
 */
@Slf4j
@Controller
@RequestMapping("/event/{eventId}/invitation")
@RequiredArgsConstructor
public class InvitationUIController {

    private final WeddingEventRepository weddingEventRepository;
    private final WhatsAppFlowController whatsAppFlowController;

    /**
     * Display invitation template configuration UI
     * GET /event/{eventId}/invitation
     */
    @GetMapping
    public String showInvitationUI(@PathVariable Long eventId, Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);

        if (eventOpt.isEmpty()) {
            model.addAttribute("error", "Event not found");
            return "error";
        }

        WeddingEvent event = eventOpt.get();

        // Prepare template configuration
        Map<String, Object> templateConfig = prepareTemplateConfig(event);

        model.addAttribute("event", event);
        model.addAttribute("templateConfig", templateConfig);
        model.addAttribute("bodyParameters", templateConfig.get("bodyParameters"));
        model.addAttribute("flowButtonConfig", templateConfig.get("flowButtonConfig"));

        return "invitation_template_config";
    }

    /**
     * Send invitation to a single guest
     * POST /event/{eventId}/invitation/send/{guestId}
     */
    @PostMapping("/send/{guestId}")
    public String sendInvitationToGuest(
            @PathVariable Long eventId,
            @PathVariable Long guestId,
            @RequestParam(required = false) String guestPhone,
            Model model) {
        try {
            Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
            if (eventOpt.isEmpty()) {
                model.addAttribute("error", "Event not found");
                return "error";
            }

            WeddingEvent event = eventOpt.get();

            // Trigger sending via WhatsAppFlowController
            whatsAppFlowController.triggerRsvpFlow(eventId, guestId, null);

            model.addAttribute("success", "Invitation sent successfully");
            model.addAttribute("guestId", guestId);

        } catch (Exception e) {
            log.error("Error sending invitation to guest {}", guestId, e);
            model.addAttribute("error", "Failed to send invitation: " + e.getMessage());
        }

        return "redirect:/event/" + eventId + "/invitation";
    }

    /**
     * Send invitations to multiple guests
     * POST /event/{eventId}/invitation/send-batch
     */
    @PostMapping("/send-batch")
    public String sendInvitationBatch(
            @PathVariable Long eventId,
            @RequestParam String guestIds,
            Model model) {
        try {
            Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
            if (eventOpt.isEmpty()) {
                model.addAttribute("error", "Event not found");
                return "error";
            }

            // Parse guest IDs
            String[] ids = guestIds.split(",");

            for (String idStr : ids) {
                try {
                    Long guestId = Long.parseLong(idStr.trim());
                    whatsAppFlowController.triggerRsvpFlow(eventId, guestId, null);
                } catch (NumberFormatException e) {
                    log.warn("Invalid guest ID: {}", idStr);
                }
            }

            model.addAttribute("success", "Invitations sent successfully to " + ids.length + " guests");

        } catch (Exception e) {
            log.error("Error sending batch invitations", e);
            model.addAttribute("error", "Failed to send invitations: " + e.getMessage());
        }

        return "redirect:/event/" + eventId + "/invitation";
    }

    /**
     * Preview invitation template
     * GET /event/{eventId}/invitation/preview
     */
    @GetMapping("/preview")
    @ResponseBody
    public Map<String, Object> previewInvitation(@PathVariable Long eventId) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);

        if (eventOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Event not found");
            return error;
        }

        WeddingEvent event = eventOpt.get();
        return prepareTemplateConfig(event);
    }

    /**
     * Prepare template configuration with all components
     */
    private Map<String, Object> prepareTemplateConfig(WeddingEvent event) {
        Map<String, Object> config = new HashMap<>();

        // Body parameters configuration
        Map<String, Object> bodyParams = new HashMap<>();
        bodyParams.put("param1_name", "Guest Name");
        bodyParams.put("param1_example", "John Doe");
        bodyParams.put("param2_name", "Couple Names");
        bodyParams.put("param2_example", event.getBrideName() + " & " + event.getGroomName());
        bodyParams.put("param3_name", "Wedding Date");
        bodyParams.put("param3_example", event.getDate() != null ? event.getDate().toString() : "TBD");
        bodyParams.put("param4_name", "Wedding Location");
        bodyParams.put("param4_example", event.getPlace() != null ? event.getPlace() : "TBD");
        config.put("bodyParameters", bodyParams);

        // Flow button configuration
        Map<String, Object> flowButton = new HashMap<>();
        flowButton.put("button_text", "Start RSVP");
        flowButton.put("button_type", "flow");
        flowButton.put("flow_id", event.getWhatsappPhoneNumberId() != null ? "rsvp_flow" : "Not Configured");
        flowButton.put("initial_screen", "WELCOME_SCREEN");
        flowButton.put("flow_data_model", Map.of(
                "guest_id", "${data.guest_id}",
                "event_id", "${data.event_id}",
                "guest_name", "${data.guest_name}",
                "bride_name", "${data.bride_name}",
                "groom_name", "${data.groom_name}",
                "wedding_date", "${data.wedding_date}",
                "wedding_location", "${data.wedding_location}"
        ));
        config.put("flowButtonConfig", flowButton);

        // Template metadata
        config.put("template_name", "rsvp_flow");
        config.put("template_language", "en");
        config.put("api_version", "v24.0");
        config.put("data_api_version", "3.0");

        return config;
    }
}

