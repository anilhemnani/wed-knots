package com.wedknots.web;

import com.wedknots.dto.AttendeeInfo;
import com.wedknots.dto.GuestValidationResponse;
import com.wedknots.dto.PhoneValidationRequest;
import com.wedknots.dto.RsvpSubmissionRequest;
import com.wedknots.model.Guest;
import com.wedknots.model.RSVP;
import com.wedknots.model.RSVPStatus;
import com.wedknots.model.WeddingEvent;
import com.wedknots.repository.GuestRepository;
import com.wedknots.repository.WeddingEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
     * Get current authenticated guest data with RSVP details
     * GET /api/guests/current
     */
    @GetMapping("/api/guests/current")
    @ResponseBody
    public Map<String, Object> getCurrentGuest(java.security.Principal principal) {
        try {
            if (principal == null) {
                log.warn("No principal found - user not authenticated");
                return Map.of("error", "Not authenticated");
            }

            log.info("Principal name: {}", principal.getName());

            // Extract phone number from principal (format: "LastName+PhoneNumber")
            String phoneNumber = principal.getName();

            // Find guest by phone number
            Optional<Guest> guestOpt = guestRepository.findByPrimaryPhoneNumber(phoneNumber);
            log.info("Primary phone lookup result: {}", guestOpt.isPresent() ? "Found guest " + guestOpt.get().getId() : "No guest found");

            if (guestOpt.isEmpty()) {
                log.info("No guest found by primary phone, trying additional phones");
                // Try additional phone numbers
                guestOpt = guestRepository.findByAdditionalPhoneNumber(phoneNumber);
                log.info("Additional phone lookup result: {}", guestOpt.isPresent() ? "Found guest " + guestOpt.get().getId() : "No guest found");
            }

            if (guestOpt.isEmpty()) {
                log.warn("No guest found with phone number: {}", phoneNumber);
                return Map.of("error", "Guest not found");
            }

            Guest guest = guestOpt.get();
            log.info("Found guest: {} (ID: {})", guest.getContactFirstName(), guest.getId());

            // Build response with guest and RSVP data
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("id", guest.getId());
            response.put("contactFirstName", guest.getContactFirstName());
            response.put("contactLastName", guest.getContactLastName());
            response.put("maxAttendees", guest.getMaxAttendees());

            log.info("Response will include guest ID: {}", guest.getId());

            // Include RSVP data if exists
            if (guest.getRsvp() != null) {
                Map<String, Object> rsvpData = new java.util.HashMap<>();
                rsvpData.put("attendeeCount", guest.getRsvp().getAttendeeCount());

                // Include attendees
                if (guest.getRsvp().getAttendees() != null && !guest.getRsvp().getAttendees().isEmpty()) {
                    List<Map<String, Object>> attendees = guest.getRsvp().getAttendees().stream()
                            .map(attendee -> Map.of(
                                    "name", (Object) attendee.getName(),
                                    "mobileNumber", (Object) (attendee.getMobileNumber() != null ? attendee.getMobileNumber() : ""),
                                    "ageGroup", (Object) (attendee.getAgeGroup() != null ? attendee.getAgeGroup() : "Adult")
                            ))
                            .collect(Collectors.toList());
                    rsvpData.put("attendees", attendees);
                }

                response.put("rsvp", rsvpData);
                // Map RSVP status string for client prefill (attending/not_attending/maybe/pending)
                response.put("rsvpStatus", guest.getRsvp().getStatus());
            } else {
                response.put("rsvpStatus", RSVPStatus.PENDING.name());
            }

            log.info("Returning guest data for ID: {}", guest.getId());
            return response;

        } catch (Exception e) {
            log.error("Error fetching current guest", e);
            return Map.of("error", "Failed to fetch guest data");
        }
    }

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
            String guestName = (guest.getContactFirstName() != null ? guest.getContactFirstName() : "") +
                    " " + (guest.getContactLastName() != null ? guest.getContactLastName() : "");
            log.info("Guest validated: {} (ID: {}) for event {}", guestName.trim(), guest.getId(), eventId);

            return GuestValidationResponse.success(
                    guest.getId(),
                    guestName.trim(),
                    guest.getFamilyName(),
                    eventId,
                    guest.getMaxAttendees()
            );

        } catch (Exception e) {
            log.error("Error validating phone number for event {}", eventId, e);
            return GuestValidationResponse.error("An error occurred. Please try again.");
        }
    }

    /**
     * Submit RSVP details
     * POST /rsvp/event/{eventId}/submit
     * Request: { "guestId": 1, "rsvpStatus": "attending", "attendees": [...], "attendeeCount": 2 }
     * Response: { "success": true, "message": "RSVP submitted successfully" }
     */
    @PostMapping("/event/{eventId}/submit")
    @ResponseBody
    public Map<String, Object> submitRsvp(
            @PathVariable Long eventId,
            @RequestBody RsvpSubmissionRequest request) {

        try {
            // Validate
            if (request.getGuestId() == null || request.getGuestId() <= 0) {
                return Map.of("success", false, "error", "Invalid guest ID");
            }

            if (request.getRsvpStatus() == null || request.getRsvpStatus().isEmpty()) {
                return Map.of("success", false, "error", "RSVP status is required");
            }

            // Fetch guest and event
            Optional<Guest> guestOpt = guestRepository.findById(request.getGuestId());
            Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);

            if (guestOpt.isEmpty() || eventOpt.isEmpty()) {
                return Map.of("success", false, "error", "Guest or event not found");
            }

            Guest guest = guestOpt.get();
            WeddingEvent event = eventOpt.get();

            // Verify guest belongs to this event
            if (!guest.getEvent().getId().equals(eventId)) {
                log.warn("Guest {} does not belong to event {}", request.getGuestId(), eventId);
                return Map.of("success", false, "error", "Guest does not belong to this event");
            }

            // Update RSVP details
            updateGuestRsvpDetails(guest, request);
            guestRepository.save(guest);

            String guestName = (guest.getContactFirstName() != null ? guest.getContactFirstName() : "") +
                    " " + (guest.getContactLastName() != null ? guest.getContactLastName() : "");
            log.info("✅ RSVP submitted successfully for guest {} (ID: {}) - Status: {}",
                    guestName.trim(), guest.getId(), request.getRsvpStatus());

            return Map.of("success", true, "message", "RSVP submitted successfully", "redirect", "/guest/dashboard");

        } catch (Exception e) {
            log.error("Error submitting RSVP for guest {} in event {}", request.getGuestId(), eventId, e);
            return Map.of("success", false, "error", "An error occurred while submitting your RSVP. Please try again.");
        }
    }

    /**
    /**
     * Update guest RSVP details from the request
     */
    private void updateGuestRsvpDetails(Guest guest, RsvpSubmissionRequest request) {
        // Update expected attendance
        com.wedknots.model.RSVP rsvp = guest.getRsvp();
        if (rsvp == null) {
            rsvp = new RSVP();
            rsvp.setGuest(guest);
            rsvp.setEventId(guest.getEventId());
            guest.setRsvp(rsvp);
        }
        rsvp.setStatus(RSVPStatus.fromString(request.getRsvpStatus()));

        rsvp.getAttendees().clear();
        // Create attendees from JSON payload (new format)
        if (request.getAttendees() != null && !request.getAttendees().isEmpty()) {
            // Get or create RSVP
            rsvp.setAttendeeCount(request.getAttendees().size());
            for (AttendeeInfo attendeeInfo : request.getAttendees()) {
                if (attendeeInfo.getName() != null && !attendeeInfo.getName().trim().isEmpty()) {
                    com.wedknots.model.Attendee attendee = com.wedknots.model.Attendee.builder()
                            .name(attendeeInfo.getName().trim())
                            .mobileNumber(attendeeInfo.getMobileNumber())
                            .ageGroup(attendeeInfo.getAgeGroup() != null ? attendeeInfo.getAgeGroup() : "Adult")
                            .rsvp(rsvp)
                            .build();
                    rsvp.getAttendees().add(attendee);
                }
            }
        }
        // Additional fields can be added here as needed
        guest.setUpdatedAt(java.time.LocalDateTime.now());
    }
}
