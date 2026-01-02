package com.momentsmanager.web;

import com.momentsmanager.model.RSVP;
import com.momentsmanager.model.Guest;
import com.momentsmanager.model.WeddingEvent;
import com.momentsmanager.repository.RSVPRepository;
import com.momentsmanager.repository.GuestRepository;
import com.momentsmanager.repository.WeddingEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/guests/{guestId}/rsvp")
public class RSVPWebController {

    @Autowired
    private WeddingEventRepository weddingEventRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private RSVPRepository rsvpRepository;

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST', 'GUEST')")
    @GetMapping
    public String viewRSVP(@PathVariable Long guestId, Model model) {
        Optional<Guest> guestOpt = guestRepository.findById(guestId);
        if (guestOpt.isEmpty()) {
            return "redirect:/events";
        }

        Guest guest = guestOpt.get();
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(guest.getEventId());
        if (eventOpt.isEmpty()) {
            return "redirect:/events";
        }

        Optional<RSVP> rsvpOpt = rsvpRepository.findByGuestId(guestId);
        if (rsvpOpt.isEmpty()) {
            return "redirect:/events/" + guest.getEventId() + "/guests";
        }

        model.addAttribute("event", eventOpt.get());
        model.addAttribute("guest", guest);
        model.addAttribute("rsvp", rsvpOpt.get());
        return "rsvp_view";
    }
}

