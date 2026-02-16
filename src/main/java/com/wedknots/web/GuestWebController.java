package com.wedknots.web;

import com.wedknots.model.*;
import com.wedknots.repository.*;
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
    private InvitationRepository invitationRepository;

    @Autowired
    private InvitationLogRepository invitationLogRepository;

    @Autowired
    private GuestService guestService;

    @Autowired
    private com.wedknots.repository.GuestPhoneNumberRepository guestPhoneNumberRepository;

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @GetMapping
    public String listGuests(@PathVariable Long eventId, Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return "redirect:/events";
        }
        WeddingEvent event = eventOpt.get();
        List<Guest> guests = guestRepository.findByEventIdWithPhones(eventId);

        // Fetch all invitations for this event
        List<Invitation> invitations = invitationRepository.findByEventId(eventId);

        // Fetch all invitation logs for this event to determine which guests received which invitations
        List<InvitationLog> invitationLogs = invitationLogRepository.findByEventId(eventId);

        model.addAttribute("event", event);
        model.addAttribute("guests", guests);
        model.addAttribute("invitations", invitations);
        model.addAttribute("invitationLogs", invitationLogs);
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
    public String createGuest(@PathVariable Long eventId,
                              @ModelAttribute Guest guest,
                              @RequestParam(required = false) String primaryPhoneNumber,
                              @RequestParam(required = false) String[] additionalPhoneNumber,
                              @RequestParam(required = false) String[] additionalPhoneFirstName,
                              @RequestParam(required = false) String[] additionalPhoneLastName,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        // Debug logging - check what's being received
        System.out.println("DEBUG: createGuest - primaryPhoneNumber: " + primaryPhoneNumber);
        System.out.println("DEBUG: createGuest - additionalPhoneNumber array length: " + (additionalPhoneNumber != null ? additionalPhoneNumber.length : "null"));
        if (additionalPhoneNumber != null) {
            for (int i = 0; i < additionalPhoneNumber.length; i++) {
                System.out.println("  [" + i + "] Phone: " + additionalPhoneNumber[i] +
                                 ", First: " + (additionalPhoneFirstName != null && i < additionalPhoneFirstName.length ? additionalPhoneFirstName[i] : "null") +
                                 ", Last: " + (additionalPhoneLastName != null && i < additionalPhoneLastName.length ? additionalPhoneLastName[i] : "null"));
            }
        }

        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return "redirect:/events";
        }
        WeddingEvent event = eventOpt.get();

        // Server-side validation
        String validationError = validateGuest(guest);
        if (validationError != null) {
            model.addAttribute("event", event);
            model.addAttribute("guest", guest);
            model.addAttribute("error", validationError);
            return "guest_form";
        }

        // Check for duplicate email in the same event
        if (guest.getContactEmail() != null && !guest.getContactEmail().trim().isEmpty()) {
            if (Boolean.TRUE.equals(guestRepository.existsByEmailInEvent(guest.getContactEmail(), eventId, null))) {
                model.addAttribute("event", event);
                model.addAttribute("guest", guest);
                model.addAttribute("error", "Email address '" + guest.getContactEmail() + "' is already registered for another guest in this event");
                return "guest_form";
            }
        }

        // Validate and set primary phone number
        if (primaryPhoneNumber == null || primaryPhoneNumber.trim().isEmpty()) {
            model.addAttribute("event", event);
            model.addAttribute("guest", guest);
            model.addAttribute("error", "Primary phone number is required");
            return "guest_form";
        }

        // Check if primary phone number already exists in this event
        if (Boolean.TRUE.equals(guestPhoneNumberRepository.existsPhoneAnywhere(primaryPhoneNumber.trim(), eventId, null))) {
            model.addAttribute("event", event);
            model.addAttribute("guest", guest);
            model.addAttribute("error", "Phone number '" + primaryPhoneNumber.trim() + "' is already registered to another guest in this event");
            return "guest_form";
        }

        guest.setPrimaryPhoneNumber(primaryPhoneNumber.trim());

        // Handle additional phone numbers
        if (additionalPhoneNumber != null && additionalPhoneNumber.length > 0) {
            for (int i = 0; i < additionalPhoneNumber.length; i++) {
                String phone = additionalPhoneNumber[i];
                if (phone != null && !phone.trim().isEmpty()) {
                    String firstName = (additionalPhoneFirstName != null && i < additionalPhoneFirstName.length)
                            ? additionalPhoneFirstName[i] : null;
                    String lastName = (additionalPhoneLastName != null && i < additionalPhoneLastName.length)
                            ? additionalPhoneLastName[i] : null;

                    if (firstName == null || firstName.trim().isEmpty() || lastName == null || lastName.trim().isEmpty()) {
                        model.addAttribute("event", event);
                        model.addAttribute("guest", guest);
                        model.addAttribute("error", "Contact first and last name are required for all additional phone numbers");
                        return "guest_form";
                    }

                    // Check if this additional phone already exists in the event
                    if (Boolean.TRUE.equals(guestPhoneNumberRepository.existsPhoneAnywhere(phone.trim(), eventId, null))) {
                        model.addAttribute("event", event);
                        model.addAttribute("guest", guest);
                        model.addAttribute("error", "Phone number '" + phone.trim() + "' is already registered to another guest in this event");
                        return "guest_form";
                    }

                    GuestPhoneNumber phoneNumber = GuestPhoneNumber.builder()
                            .guest(guest)
                            .eventId(eventId)
                            .phoneNumber(phone.trim())
                            .phoneType(GuestPhoneNumber.PhoneType.PERSONAL)
                            .contactFirstName(firstName.trim())
                            .contactLastName(lastName.trim())
                            .isPrimary(false)
                            .build();
                    guest.getPhoneNumbers().add(phoneNumber);
                }
            }
        }

        // Set the event relationship properly
        guest.setEvent(event);

        // Debug logging
        System.out.println("DEBUG: About to save guest with " + guest.getPhoneNumbers().size() + " additional phone numbers");
        for (GuestPhoneNumber phone : guest.getPhoneNumbers()) {
            System.out.println("  - Phone: " + phone.getPhoneNumber() + ", Contact: " + phone.getContactFirstName() + " " + phone.getContactLastName());
        }

        try {
            guestService.createGuest(guest);
            redirectAttributes.addFlashAttribute("successMessage", "Guest created successfully");
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            String msg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
            model.addAttribute("event", event);
            model.addAttribute("guest", guest);
            model.addAttribute("error", msg != null ? msg : "Data integrity violation");
            return "guest_form";
        } catch (RuntimeException e) {
            model.addAttribute("event", event);
            model.addAttribute("guest", guest);
            model.addAttribute("error", e.getMessage());
            return "guest_form";
        }
        return "redirect:/events/" + eventId + "/guests";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @GetMapping("/{guestId}/edit")
    public String editGuest(@PathVariable Long eventId, @PathVariable Long guestId, Model model) {
        Optional<Guest> guestOpt = guestRepository.findByIdWithPhones(guestId);
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

        // Debug logging
        System.out.println("DEBUG: Loading guest " + guestId + " for edit. Phone numbers loaded: " + guest.getPhoneNumbers().size());
        for (GuestPhoneNumber phone : guest.getPhoneNumbers()) {
            System.out.println("  - Phone: " + phone.getPhoneNumber() + ", Contact: " + phone.getContactFirstName() + " " + phone.getContactLastName());
        }

        model.addAttribute("event", eventOpt.get());
        model.addAttribute("guest", guest);
        model.addAttribute("rsvp", rsvpOpt.orElse(null));
        return "guest_form";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PostMapping("/{guestId}/edit")
    public String updateGuest(@PathVariable Long eventId,
                              @PathVariable Long guestId,
                              @ModelAttribute Guest guest,
                              @RequestParam(required = false) String primaryPhoneNumber,
                              @RequestParam(required = false) String[] additionalPhoneNumber,
                              @RequestParam(required = false) String[] additionalPhoneFirstName,
                              @RequestParam(required = false) String[] additionalPhoneLastName,
                              @RequestParam(required = false) Long[] additionalPhoneId,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return "redirect:/events";
        }
        WeddingEvent event = eventOpt.get();

        // Server-side validation
        String validationError = validateGuest(guest);
        if (validationError != null) {
            Optional<RSVP> rsvpOpt = rsvpRepository.findByGuestId(guestId);
            model.addAttribute("event", event);
            model.addAttribute("guest", guest);
            model.addAttribute("rsvp", rsvpOpt.orElse(null));
            model.addAttribute("error", validationError);
            return "guest_form";
        }

        // Check for duplicate email in the same event (excluding this guest)
        if (guest.getContactEmail() != null && !guest.getContactEmail().trim().isEmpty()) {
            if (Boolean.TRUE.equals(guestRepository.existsByEmailInEvent(guest.getContactEmail(), eventId, guestId))) {
                Optional<RSVP> rsvpOpt = rsvpRepository.findByGuestId(guestId);
                model.addAttribute("event", event);
                model.addAttribute("guest", guest);
                model.addAttribute("rsvp", rsvpOpt.orElse(null));
                model.addAttribute("error", "Email address '" + guest.getContactEmail() + "' is already registered for another guest in this event");
                return "guest_form";
            }
        }

        // Validate and set primary phone number
        if (primaryPhoneNumber == null || primaryPhoneNumber.trim().isEmpty()) {
            Optional<RSVP> rsvpOpt = rsvpRepository.findByGuestId(guestId);
            model.addAttribute("event", event);
            model.addAttribute("guest", guest);
            model.addAttribute("rsvp", rsvpOpt.orElse(null));
            model.addAttribute("error", "Primary phone number is required");
            return "guest_form";
        }

        // Check if primary phone number already exists in this event (excluding this guest)
        if (Boolean.TRUE.equals(guestPhoneNumberRepository.existsPhoneAnywhere(primaryPhoneNumber.trim(), eventId, guestId))) {
            Optional<RSVP> rsvpOpt = rsvpRepository.findByGuestId(guestId);
            model.addAttribute("event", event);
            model.addAttribute("guest", guest);
            model.addAttribute("rsvp", rsvpOpt.orElse(null));
            model.addAttribute("error", "Phone number '" + primaryPhoneNumber.trim() + "' is already registered to another guest in this event");
            return "guest_form";
        }

        guest.setPrimaryPhoneNumber(primaryPhoneNumber.trim());

        // Clear existing additional phone numbers and rebuild from form
        guest.getPhoneNumbers().clear();

        // Handle additional phone numbers
        if (additionalPhoneNumber != null && additionalPhoneNumber.length > 0) {
            for (int i = 0; i < additionalPhoneNumber.length; i++) {
                String phone = additionalPhoneNumber[i];
                if (phone != null && !phone.trim().isEmpty()) {
                    String firstName = (additionalPhoneFirstName != null && i < additionalPhoneFirstName.length)
                            ? additionalPhoneFirstName[i] : null;
                    String lastName = (additionalPhoneLastName != null && i < additionalPhoneLastName.length)
                            ? additionalPhoneLastName[i] : null;
                    Long phoneId = (additionalPhoneId != null && i < additionalPhoneId.length)
                            ? additionalPhoneId[i] : null;

                    if (firstName == null || firstName.trim().isEmpty() || lastName == null || lastName.trim().isEmpty()) {
                        Optional<RSVP> rsvpOpt = rsvpRepository.findByGuestId(guestId);
                        model.addAttribute("event", event);
                        model.addAttribute("guest", guest);
                        model.addAttribute("rsvp", rsvpOpt.orElse(null));
                        model.addAttribute("error", "Contact first and last name are required for all additional phone numbers");
                        return "guest_form";
                    }

                    // Check if this additional phone already exists in the event (excluding this guest)
                    if (Boolean.TRUE.equals(guestPhoneNumberRepository.existsPhoneAnywhere(phone.trim(), eventId, guestId))) {
                        Optional<RSVP> rsvpOpt = rsvpRepository.findByGuestId(guestId);
                        model.addAttribute("event", event);
                        model.addAttribute("guest", guest);
                        model.addAttribute("rsvp", rsvpOpt.orElse(null));
                        model.addAttribute("error", "Phone number '" + phone.trim() + "' is already registered to another guest in this event");
                        return "guest_form";
                    }

                    GuestPhoneNumber phoneNumber = GuestPhoneNumber.builder()
                            .id(phoneId) // Will be null for new phones
                            .guest(guest)
                            .eventId(eventId)
                            .phoneNumber(phone.trim())
                            .phoneType(GuestPhoneNumber.PhoneType.PERSONAL)
                            .contactFirstName(firstName.trim())
                            .contactLastName(lastName.trim())
                            .isPrimary(false)
                            .build();
                    guest.getPhoneNumbers().add(phoneNumber);
                }
            }
        }

        // Set the event relationship properly
        guest.setEvent(event);

        // Debug logging
        System.out.println("DEBUG: About to update guest " + guestId + " with " + guest.getPhoneNumbers().size() + " additional phone numbers");
        for (GuestPhoneNumber phone : guest.getPhoneNumbers()) {
            System.out.println("  - Phone: " + phone.getPhoneNumber() + ", Contact: " + phone.getContactFirstName() + " " + phone.getContactLastName() + ", ID: " + phone.getId());
        }

        try {
            guestService.updateGuest(guestId, guest);
            redirectAttributes.addFlashAttribute("successMessage", "Guest updated successfully");
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            String msg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
            model.addAttribute("event", event);
            model.addAttribute("guest", guest);
            model.addAttribute("error", msg != null ? msg : "Data integrity violation occurred");
            return "guest_form";
        } catch (RuntimeException e) {
            Optional<RSVP> rsvpOpt = rsvpRepository.findByGuestId(guestId);
            model.addAttribute("event", event);
            model.addAttribute("guest", guest);
            model.addAttribute("rsvp", rsvpOpt.orElse(null));
            model.addAttribute("error", e.getMessage());
            return "guest_form";
        }
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
            @RequestParam String contactFirstName,
            @RequestParam String contactLastName,
            RedirectAttributes redirectAttributes) {
        try {
            // Validate required fields
            if (contactFirstName == null || contactFirstName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Contact first name is required for phone number");
                return "redirect:/events/" + eventId + "/guests/" + guestId + "/edit";
            }
            if (contactLastName == null || contactLastName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Contact last name is required for phone number");
                return "redirect:/events/" + eventId + "/guests/" + guestId + "/edit";
            }

            GuestPhoneNumber.PhoneType type = GuestPhoneNumber.PhoneType.valueOf(phoneType);
            guestService.addPhoneNumber(guestId, phoneNumber, type, contactFirstName, contactLastName);
            redirectAttributes.addFlashAttribute("successMessage", "Phone number added successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/events/" + eventId + "/guests/" + guestId + "/edit";
    }

    /**
     * Edit/update a phone number
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PostMapping("/{guestId}/edit-phone/{phoneId}")
    public String editPhoneNumber(
            @PathVariable Long eventId,
            @PathVariable Long guestId,
            @PathVariable Long phoneId,
            @RequestParam String phoneNumber,
            @RequestParam(required = false, defaultValue = "PERSONAL") String phoneType,
            @RequestParam String contactFirstName,
            @RequestParam String contactLastName,
            RedirectAttributes redirectAttributes) {
        try {
            // Validate required fields
            if (contactFirstName == null || contactFirstName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Contact first name is required for phone number");
                return "redirect:/events/" + eventId + "/guests/" + guestId + "/edit";
            }
            if (contactLastName == null || contactLastName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Contact last name is required for phone number");
                return "redirect:/events/" + eventId + "/guests/" + guestId + "/edit";
            }

            GuestPhoneNumber.PhoneType type = GuestPhoneNumber.PhoneType.valueOf(phoneType);
            guestService.updatePhoneNumber(guestId, phoneId, phoneNumber, type, contactFirstName, contactLastName);
            redirectAttributes.addFlashAttribute("successMessage", "Phone number updated successfully");
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

    private String validateGuest(Guest guest) {
        if (guest.getFamilyName() == null || guest.getFamilyName().trim().isEmpty()) {
            return "Family name is required";
        }
        if (guest.getContactFirstName() == null || guest.getContactFirstName().trim().isEmpty()) {
            return "Contact first name is required";
        }
        if (guest.getContactLastName() == null || guest.getContactLastName().trim().isEmpty()) {
            return "Contact last name is required";
        }
        if (guest.getSide() == null || guest.getSide().trim().isEmpty()) {
            return "Side (Bride/Groom/Both) is required";
        }
        if (guest.getMaxAttendees() < 0) {
            return "Max attendees must be 0 or greater";
        }
        return null;
    }
}
