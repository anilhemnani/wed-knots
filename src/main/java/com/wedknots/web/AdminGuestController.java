package com.wedknots.web;

import com.wedknots.model.Guest;
import com.wedknots.model.GuestPhoneNumber;
import com.wedknots.model.WeddingEvent;
import com.wedknots.repository.GuestRepository;
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
@RequestMapping("/admin/events/{eventId}/guests")
public class AdminGuestController {

    @Autowired
    private WeddingEventRepository weddingEventRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private GuestService guestService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public String listGuests(@PathVariable Long eventId, Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return "redirect:/admin/events";
        }
        WeddingEvent event = eventOpt.get();
        List<Guest> guests = guestRepository.findAll().stream()
                .filter(g -> g.getEventId() != null && g.getEventId().equals(eventId))
                .toList();
        model.addAttribute("event", event);
        model.addAttribute("guests", guests);
        return "admin_event_guests";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String newGuest(@PathVariable Long eventId, Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return "redirect:/admin/events";
        }
        model.addAttribute("event", eventOpt.get());
        model.addAttribute("guest", new Guest());
        return "admin_guest_form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/new")
    public String createGuest(@PathVariable Long eventId, @ModelAttribute Guest guest) {
        guest.setEventId(eventId);
        guestService.createGuest(guest);
        return "redirect:/admin/events/" + eventId + "/guests";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{guestId}/edit")
    public String editGuest(@PathVariable Long eventId, @PathVariable Long guestId, Model model) {
        Optional<Guest> guestOpt = guestRepository.findById(guestId);
        if (guestOpt.isEmpty()) {
            return "redirect:/admin/events/" + eventId + "/guests";
        }
        Guest guest = guestOpt.get();
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return "redirect:/admin/events";
        }

        model.addAttribute("event", eventOpt.get());
        model.addAttribute("guest", guest);
        return "admin_guest_form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{guestId}/edit")
    public String updateGuest(@PathVariable Long eventId, @PathVariable Long guestId, @ModelAttribute Guest guest) {
        guest.setEventId(eventId);
        guestService.updateGuest(guestId, guest);
        return "redirect:/admin/events/" + eventId + "/guests";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{guestId}/delete")
    public String deleteGuest(@PathVariable Long eventId, @PathVariable Long guestId) {
        guestService.deleteGuest(guestId);
        return "redirect:/admin/events/" + eventId + "/guests";
    }

    /**
     * Add a new phone number to a guest (Admin)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{guestId}/add-phone")
    public String addPhoneNumber(
            @PathVariable Long eventId,
            @PathVariable Long guestId,
            @RequestParam String phoneNumber,
            @RequestParam(required = false, defaultValue = "PERSONAL") String phoneType,
            @RequestParam(required = false) String contactFirstName,
            @RequestParam(required = false) String contactLastName,
            RedirectAttributes redirectAttributes) {
        try {
            GuestPhoneNumber.PhoneType type = GuestPhoneNumber.PhoneType.valueOf(phoneType);
            guestService.addPhoneNumber(guestId, phoneNumber, type, contactFirstName, contactLastName);
            redirectAttributes.addFlashAttribute("successMessage", "Phone number added successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/events/" + eventId + "/guests/" + guestId + "/edit";
    }

    /**
     * Edit/update a phone number (Admin)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{guestId}/edit-phone/{phoneId}")
    public String editPhoneNumber(
            @PathVariable Long eventId,
            @PathVariable Long guestId,
            @PathVariable Long phoneId,
            @RequestParam String phoneNumber,
            @RequestParam(required = false, defaultValue = "PERSONAL") String phoneType,
            @RequestParam(required = false) String contactFirstName,
            @RequestParam(required = false) String contactLastName,
            RedirectAttributes redirectAttributes) {
        try {
            GuestPhoneNumber.PhoneType type = GuestPhoneNumber.PhoneType.valueOf(phoneType);
            guestService.updatePhoneNumber(guestId, phoneId, phoneNumber, type, contactFirstName, contactLastName);
            redirectAttributes.addFlashAttribute("successMessage", "Phone number updated successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/events/" + eventId + "/guests/" + guestId + "/edit";
    }

    /**
     * Remove a phone number from a guest (Admin)
     */
    @PreAuthorize("hasRole('ADMIN')")
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
        return "redirect:/admin/events/" + eventId + "/guests/" + guestId + "/edit";
    }
}
