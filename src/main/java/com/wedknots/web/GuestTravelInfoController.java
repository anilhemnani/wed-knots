package com.wedknots.web;

import com.wedknots.dto.TravelInfoDto;
import com.wedknots.model.Attendee;
import com.wedknots.model.Guest;
import com.wedknots.model.RSVP;
import com.wedknots.model.TravelInfo;
import com.wedknots.repository.AttendeeRepository;
import com.wedknots.repository.GuestRepository;
import com.wedknots.transformers.TravelInfoTransformer;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@Slf4j
public class GuestTravelInfoController {
    private final GuestRepository guestRepository;
    private final AttendeeRepository attendeeRepository;

    public GuestTravelInfoController(GuestRepository guestRepository, AttendeeRepository attendeeRepository) {
        this.guestRepository = guestRepository;
        this.attendeeRepository = attendeeRepository;
    }

    /**
     * REST endpoint: Get travel info and attendees for guest (AJAX)
     * GET /api/guests/{guestId}/travel
     */
    @GetMapping("/api/guests/{guestId}/travel")
    @ResponseBody
    public ResponseEntity<?> getGuestTravelInfoJson(@PathVariable Long guestId) {
        Optional<Guest> guestOpt = guestRepository.findById(guestId);
        if (guestOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Guest not found");
        }
        Guest guest = guestOpt.get();
        com.wedknots.model.TravelInfo travelInfo = guest.getTravelInfo();
        TravelInfoDto dto = new TravelInfoDto();
        dto.setGuestId(guest.getId());
        dto = TravelInfoTransformer.toDto(travelInfo, dto);

        setDefaultsOrFallbackValues(dto, guest);
        if (guest.getRsvp() != null && guest.getRsvp().getAttendees() != null) {
            dto.setAttendees(guest.getRsvp().getAttendees().stream().map(a -> {
                com.wedknots.dto.AttendeeInfo ai = new com.wedknots.dto.AttendeeInfo();
                ai.setName(a.getName());
                ai.setMobileNumber(a.getMobileNumber());
                ai.setAgeGroup(a.getAgeGroup());
                return ai;
            }).toList());
        }
        return ResponseEntity.ok(dto);
    }

    private static void setDefaultsOrFallbackValues(TravelInfoDto dto, Guest guest) {
        if (dto.getArrivalAirport() == null) {
            dto.setArrivalAirport(guest.getEvent().getPreferredTravelAirport());
        }
        if (dto.getDepartureAirport() == null) {
            dto.setDepartureAirport(guest.getEvent().getPreferredTravelAirport());
        }
        if (dto.getArrivalStation() == null) {
            dto.setArrivalStation(guest.getEvent().getPreferredTravelStation());
        }
        if (dto.getDepartureStation() == null) {
            dto.setDepartureStation(guest.getEvent().getPreferredTravelStation());
        }
        if (dto.getArrivalDateTime() == null) {
            LocalDateTime arrival = null;
            if (guest.getExpectedArrivalDate() != null) {
                arrival = guest.getExpectedArrivalDate().atStartOfDay();
            } else if (guest.getEvent().getExpectedGuestArrivalDate() != null) {
                arrival = guest.getEvent().getExpectedGuestArrivalDate().atStartOfDay();
            }
            dto.setArrivalDateTime(arrival);
        }
        if (dto.getDepartureDateTime() == null) {
            LocalDateTime departure = null;
            if (guest.getExpectedDepartureDate() != null) {
                departure = guest.getExpectedDepartureDate().atStartOfDay();
            } else if (guest.getEvent().getExpectedGuestDepartureDate() != null) {
                departure = guest.getEvent().getExpectedGuestDepartureDate().atStartOfDay();
            }
            dto.setDepartureDateTime(departure);
        }
        dto.setMaxAllowedAttendees(guest.getMaxAttendees() == 0 ? guest.getEvent().getDefaultMaxAllowedAttendees() : guest.getMaxAttendees());
    }

    /**
     * REST endpoint: Save travel info and attendees for guest (AJAX)
     * POST /api/guests/{guestId}/travel
     */
    @PostMapping("/api/guests/{guestId}/travel")
    @ResponseBody
    public ResponseEntity<?> saveGuestTravelInfoJson(
            @PathVariable Long guestId,
            @RequestBody TravelInfoDto request) {
        Optional<Guest> guestOpt = guestRepository.findById(guestId);
        if (guestOpt.isEmpty()) {
            return ResponseEntity.status(404).body(java.util.Map.of("success", false, "error", "Guest not found"));
        }
        Guest guest = guestOpt.get();
        int maxAttendees = Math.max(guest.getMaxAttendees(), guest.getEvent().getDefaultMaxAllowedAttendees());
        if (request.getAttendees() == null || request.getAttendees().isEmpty() || request.getAttendees().stream().allMatch(n -> n == null || n.getName().trim().isEmpty())) {
            return ResponseEntity.badRequest().body(java.util.Map.of("success", false, "error", "At least one attendee is required to submit travel details."));
        }
        if (request.getAttendees().size() > maxAttendees) {
            return ResponseEntity.badRequest().body(java.util.Map.of("success", false, "error", "You cannot add more than " + maxAttendees + " attendees."));
        }
        try {
            TravelInfo travelInfo = TravelInfoTransformer.toEntity(request, guest.getTravelInfo());
            travelInfo.setGuest(guest);
            guest.setTravelInfo(travelInfo);
            RSVP rsvp = guest.getRsvp();
            if (rsvp == null) {
                rsvp = new RSVP();
                rsvp.setGuest(guest);
                rsvp.setEventId(guest.getEvent().getId());
                guest.setRsvp(rsvp);
            }
            rsvp.getAttendees().clear();
            rsvp.getAttendees().addAll(request.getAttendees().stream().map(a ->
                    Attendee.builder()
                            .name(a.getName().trim())
                            .mobileNumber(a.getMobileNumber())
                            .ageGroup(a.getAgeGroup())
                            .rsvp(guest.getRsvp())
                            .build()).toList());
            rsvp.setAttendeeCount(rsvp.getAttendees().size());
            guestRepository.save(guest);
            return ResponseEntity.ok(java.util.Map.of("success", true, "message", "Travel info and attendees saved successfully"));
        } catch (OptimisticLockingFailureException | OptimisticLockException e) {
            e.printStackTrace();
            return ResponseEntity.status(409).body(java.util.Map.of("success", false, "error", "Another user has updated this guest's travel info or attendees. Please reload and try again."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(java.util.Map.of("success", false, "error", "An error occurred while saving travel info: " + e.getMessage()));
        }
    }

    /**
     * Show travel info form for guest (mobile UI)
     * GET /guests/{guestId}/travel
     */
    @GetMapping("/guests/{guestId}/travel")
    public String showGuestTravelInfoForm(@PathVariable Long guestId, Model model) {
        Optional<Guest> guestOpt = guestRepository.findById(guestId);
        if (guestOpt.isEmpty()) {
            model.addAttribute("error", "Guest not found");
            return "error";
        }
        Guest guest = guestOpt.get();
        com.wedknots.model.TravelInfo travelInfo = guest.getTravelInfo();
        if (travelInfo == null) {
            travelInfo = new com.wedknots.model.TravelInfo();
        }
        model.addAttribute("guest", guest);
        model.addAttribute("travelInfo", travelInfo);
        model.addAttribute("event", guest.getEvent());
        model.addAttribute("eventId", guest.getEvent().getId());
        return "guest_travel_info_form";
    }
}
