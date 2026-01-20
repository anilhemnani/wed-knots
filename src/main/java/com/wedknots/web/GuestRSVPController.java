package com.wedknots.web;

import com.wedknots.model.Guest;
import com.wedknots.model.WeddingEvent;
import com.wedknots.repository.GuestRepository;
import com.wedknots.repository.WeddingEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controller for guest RSVP submission via mobile web
 * Guests can enter their mobile number and submit RSVP details
 */
@Slf4j
@Controller
@RequestMapping("/rsvp")
@RequiredArgsConstructor
public class GuestRSVPController {

    private final WeddingEventRepository weddingEventRepository;
    private final GuestRepository guestRepository;

    /**
     * Display mobile RSVP page with phone number entry form
     * GET /rsvp/event/{eventId}
     */
    @GetMapping("/event/{eventId}")
    public String showRsvpPage(@PathVariable Long eventId, Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);

        if (eventOpt.isEmpty()) {
            log.warn("Event not found: {}", eventId);
            model.addAttribute("error", "Event not found");
            return "rsvp/error";
        }

        WeddingEvent event = eventOpt.get();
        model.addAttribute("eventId", eventId);
        model.addAttribute("eventName", event.getName());
        model.addAttribute("brideName", event.getBrideName());
        model.addAttribute("groomName", event.getGroomName());

        return "rsvp/mobile_rsvp";
    }

    /**
     * Validate mobile number and retrieve guest details
     * POST /rsvp/event/{eventId}/validate-phone
     * Request: { "phoneNumber": "+447878597720" }
     * Response: { "success": true, "guestId": 1, "guestName": "John Doe", ... }
     */
    @PostMapping("/event/{eventId}/validate-phone")
    @ResponseBody
    public GuestValidationResponse validatePhoneNumber(
            @PathVariable Long eventId,
            @RequestBody PhoneValidationRequest request) {

        try {
            String phoneNumber = request.getPhoneNumber().trim();

            // Validation
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                return GuestValidationResponse.error("Phone number is required");
            }

            if (!phoneNumber.matches("^\\+?[1-9]\\d{1,14}$")) {
                return GuestValidationResponse.error("Invalid phone number format");
            }

            // Check if event exists
            Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
            if (eventOpt.isEmpty()) {
                return GuestValidationResponse.error("Event not found");
            }

            WeddingEvent event = eventOpt.get();

            // Find guest with this phone number in this event
            Optional<Guest> guestOpt = guestRepository.findByEventAndPhoneNumber(event, phoneNumber);

            if (guestOpt.isEmpty()) {
                log.warn("No guest found with phone {} in event {}", phoneNumber, eventId);
                return GuestValidationResponse.error("No guest found with this phone number");
            }

            Guest guest = guestOpt.get();
            log.info("Guest validated: {} (ID: {}) for event {}", guest.getContactName(), guest.getId(), eventId);

            return GuestValidationResponse.success(
                    guest.getId(),
                    guest.getContactName(),
                    guest.getFamilyName(),
                    eventId
            );

        } catch (Exception e) {
            log.error("Error validating phone number for event {}", eventId, e);
            return GuestValidationResponse.error("An error occurred. Please try again.");
        }
    }

    /**
     * Submit RSVP details
     * POST /rsvp/event/{eventId}/submit
     * Request: { "guestId": 1, "rsvpStatus": "attending", "attendeeCount": 2, ... }
     */
    @PostMapping("/event/{eventId}/submit")
    public String submitRsvp(
            @PathVariable Long eventId,
            @ModelAttribute RsvpSubmissionRequest request,
            Model model) {

        try {
            // Validate
            if (request.getGuestId() == null || request.getGuestId() <= 0) {
                model.addAttribute("error", "Invalid guest ID");
                return "rsvp/error";
            }

            if (request.getRsvpStatus() == null || request.getRsvpStatus().isEmpty()) {
                model.addAttribute("error", "RSVP status is required");
                return "rsvp/error";
            }

            // Fetch guest and event
            Optional<Guest> guestOpt = guestRepository.findById(request.getGuestId());
            Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);

            if (guestOpt.isEmpty() || eventOpt.isEmpty()) {
                model.addAttribute("error", "Guest or event not found");
                return "rsvp/error";
            }

            Guest guest = guestOpt.get();
            WeddingEvent event = eventOpt.get();

            // Verify guest belongs to this event
            if (!guest.getEvent().getId().equals(eventId)) {
                log.warn("Guest {} does not belong to event {}", request.getGuestId(), eventId);
                model.addAttribute("error", "Guest does not belong to this event");
                return "rsvp/error";
            }

            // Update RSVP details
            updateGuestRsvpDetails(guest, request);
            guestRepository.save(guest);

            log.info("✅ RSVP submitted successfully for guest {} (ID: {}) - Status: {}",
                    guest.getContactName(), guest.getId(), request.getRsvpStatus());

            // Prepare success response
            model.addAttribute("guestName", guest.getContactName());
            model.addAttribute("eventName", event.getName());
            model.addAttribute("rsvpStatus", request.getRsvpStatus());

            return "rsvp/success";

        } catch (Exception e) {
            log.error("Error submitting RSVP for guest {} in event {}", request.getGuestId(), eventId, e);
            model.addAttribute("error", "An error occurred while submitting your RSVP. Please try again.");
            return "rsvp/error";
        }
    }

    /**
     * Update guest RSVP details from the request
     */
    private void updateGuestRsvpDetails(Guest guest, RsvpSubmissionRequest request) {
        // Update max attendees if provided
        if (request.getAttendeeCount() != null && request.getAttendeeCount() > 0) {
            guest.setMaxAttendees(request.getAttendeeCount());
        }

        // Update expected attendance
        if (request.getRsvpStatus() != null) {
            try {
                guest.setExpectedAttendance(
                        com.wedknots.model.ExpectedAttendance.valueOf(request.getRsvpStatus().toUpperCase())
                );
            } catch (IllegalArgumentException e) {
                log.warn("Invalid RSVP status: {}", request.getRsvpStatus());
            }
        }

        // Additional fields can be added here as needed
        guest.setUpdatedAt(java.time.LocalDateTime.now());
    }

    /**
     * Request DTO for phone validation
     */
    @lombok.Data
    public static class PhoneValidationRequest {
        private String phoneNumber;
    }

    /**
     * Response DTO for phone validation
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class GuestValidationResponse {
        private boolean success;
        private String message;
        private Long guestId;
        private String guestName;
        private String familyName;
        private Long eventId;

        public static GuestValidationResponse success(Long guestId, String guestName, String familyName, Long eventId) {
            return new GuestValidationResponse(true, null, guestId, guestName, familyName, eventId);
        }

        public static GuestValidationResponse error(String message) {
            return new GuestValidationResponse(false, message, null, null, null, null);
        }
    }

    /**
     * Request DTO for RSVP submission
     */
    @lombok.Data
    public static class RsvpSubmissionRequest {
        private Long guestId;
        private String rsvpStatus; // "attending" or "not_attending"
        private Integer attendeeCount;
        private String travelMode;
        private String travelDetails;
        private String dietaryRestrictions;
        private String specialRequests;
    }
}

