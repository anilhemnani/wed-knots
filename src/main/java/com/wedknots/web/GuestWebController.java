package com.wedknots.web;

import com.wedknots.model.Guest;
import com.wedknots.model.GuestPhoneNumber;
import com.wedknots.model.RSVP;
import com.wedknots.model.WeddingEvent;
import com.wedknots.repository.GuestRepository;
import com.wedknots.repository.RSVPRepository;
import com.wedknots.repository.WeddingEventRepository;
import com.wedknots.service.GuestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/events/{eventId}/guests")
public class GuestWebController {

    @Autowired
    private WeddingEventRepository weddingEventRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private RSVPRepository rsvpRepository;

    @Autowired
    private GuestService guestService;

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @GetMapping
    public String listGuests(@PathVariable Long eventId, Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return "redirect:/events";
        }
        WeddingEvent event = eventOpt.get();
        List<Guest> guests = guestRepository.findAll().stream()
                .filter(g -> g.getEventId() != null && g.getEventId().equals(eventId))
                .toList();
        model.addAttribute("event", event);
        model.addAttribute("guests", guests);
        return "guest_list";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @GetMapping("/new")
    public String newGuest(@PathVariable Long eventId, Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return "redirect:/events";
        }
        model.addAttribute("event", eventOpt.get());
        model.addAttribute("guest", new Guest());
        return "guest_form";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PostMapping("/new")
    public String createGuest(@PathVariable Long eventId, @ModelAttribute Guest guest) {
        guest.setEventId(eventId);
        guestService.createGuest(guest);
        return "redirect:/events/" + eventId + "/guests";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @GetMapping("/{guestId}/edit")
    public String editGuest(@PathVariable Long eventId, @PathVariable Long guestId, Model model) {
        Optional<Guest> guestOpt = guestRepository.findById(guestId);
        if (guestOpt.isEmpty()) {
            return "redirect:/events/" + eventId + "/guests";
        }
        Guest guest = guestOpt.get();
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return "redirect:/events";
        }

        // Get RSVP for this guest
        Optional<RSVP> rsvpOpt = rsvpRepository.findByGuestId(guestId);

        model.addAttribute("event", eventOpt.get());
        model.addAttribute("guest", guest);
        model.addAttribute("rsvp", rsvpOpt.orElse(null));
        return "guest_form";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PostMapping("/{guestId}/edit")
    public String updateGuest(@PathVariable Long eventId, @PathVariable Long guestId, @ModelAttribute Guest guest) {
        guest.setEventId(eventId);  // Ensure eventId is set
        guestService.updateGuest(guestId, guest);
        return "redirect:/events/" + eventId + "/guests";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PostMapping("/{guestId}/delete")
    public String deleteGuest(@PathVariable Long eventId, @PathVariable Long guestId) {
        guestService.deleteGuest(guestId);
        return "redirect:/events/" + eventId + "/guests";
    }

    /**
     * Add a new phone number to a guest
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PostMapping("/{guestId}/add-phone")
    public String addPhoneNumber(
            @PathVariable Long eventId,
            @PathVariable Long guestId,
            @RequestParam String phoneNumber,
            @RequestParam(required = false, defaultValue = "PERSONAL") String phoneType,
            RedirectAttributes redirectAttributes) {
        try {
            GuestPhoneNumber.PhoneType type = GuestPhoneNumber.PhoneType.valueOf(phoneType);
            guestService.addPhoneNumber(guestId, phoneNumber, type);
            redirectAttributes.addFlashAttribute("successMessage", "Phone number added successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/events/" + eventId + "/guests/" + guestId + "/edit";
    }

    /**
     * Set a phone number as primary
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PostMapping("/{guestId}/set-primary-phone/{phoneId}")
    public String setPrimaryPhone(
            @PathVariable Long eventId,
            @PathVariable Long guestId,
            @PathVariable Long phoneId,
            RedirectAttributes redirectAttributes) {
        try {
            guestService.setPrimaryPhoneNumber(guestId, phoneId);
            redirectAttributes.addFlashAttribute("successMessage", "Primary phone number updated");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/events/" + eventId + "/guests/" + guestId + "/edit";
    }

    /**
     * Remove a phone number from a guest
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PostMapping("/{guestId}/remove-phone/{phoneId}")
    public String removePhoneNumber(
            @PathVariable Long eventId,
            @PathVariable Long guestId,
            @PathVariable Long phoneId,
            RedirectAttributes redirectAttributes) {
        try {
            guestService.removePhoneNumber(guestId, phoneId);
            redirectAttributes.addFlashAttribute("successMessage", "Phone number removed successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/events/" + eventId + "/guests/" + guestId + "/edit";
    }

    /**
     * WhatsApp RSVP Send Page
     * Display UI for sending WhatsApp RSVP requests to guests
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @GetMapping("/whatsapp-rsvp-send")
    public String whatsappRsvpSendPage(@PathVariable Long eventId, Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return "redirect:/events";
        }

        WeddingEvent event = eventOpt.get();
        List<Guest> guests = guestRepository.findAll().stream()
                .filter(g -> g.getEventId() != null && g.getEventId().equals(eventId))
                .toList();

        model.addAttribute("event", event);
        model.addAttribute("guests", guests);
        model.addAttribute("whatsappConfigured",
            Boolean.TRUE.equals(event.getWhatsappApiEnabled()) &&
            event.getWhatsappPhoneNumberId() != null);

        return "host/whatsapp_rsvp_send";
    }
}

