package com.wedknots.web;

import com.wedknots.repository.GuestRepository;
import com.wedknots.repository.InvitationLogRepository;
import com.wedknots.service.InvitationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import com.wedknots.model.InvitationLog;
import com.wedknots.model.Invitation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/guest")
public class GuestDashboardController {

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private InvitationLogRepository invitationLogRepository;

    @Autowired
    private InvitationLogService invitationLogService;

    /**
     * Guest dashboard - redirects to invitation if only one exists,
     * otherwise shows list of invitations
     */
    @PreAuthorize("hasRole('GUEST')")
    @GetMapping("/dashboard")
    public String guestDashboard(Model model, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return "redirect:/login";
        }

        // Resolve guest (session guestId is set at login)
        Object guestIdAttr = request.getSession().getAttribute("guestId");
        if (guestIdAttr == null) {
            return "redirect:/login";
        }
        Long guestId = (guestIdAttr instanceof Integer) ? ((Integer) guestIdAttr).longValue() : (Long) guestIdAttr;

        // Fetch all invitation logs for this guest
        List<InvitationLog> logs = invitationLogRepository.findByGuestId(guestId);

        // Group by event
        Map<Long, List<InvitationLog>> byEvent = logs.stream()
                .filter(log -> log.getInvitation() != null && log.getInvitation().getEvent() != null)
                .collect(Collectors.groupingBy(log -> log.getInvitation().getEvent().getId()));

        if (byEvent.isEmpty()) {
            model.addAttribute("emptyState", true);
            return "guest_dashboard";
        }

        if (byEvent.size() == 1) {
            // Only one event: redirect to event view with buttons
            Long eventId = byEvent.keySet().iterator().next();
            model.addAttribute("singleEvent", true);
            model.addAttribute("event", byEvent.values().iterator().next().get(0).getInvitation().getEvent());
            model.addAttribute("eventInvitations", byEvent.values().iterator().next());
            return "guest_dashboard";
        }

        // Multiple events: show list
        List<EventInvitationsVM> events = byEvent.values().stream()
                .map(list -> {
                    Invitation inv = list.get(0).getInvitation();
                    return new EventInvitationsVM(inv.getEvent(), list);
                })
                .collect(Collectors.toList());

        model.addAttribute("events", events);
        return "guest_dashboard";
    }

    public static class EventInvitationsVM {
        private final com.wedknots.model.WeddingEvent event;
        private final List<InvitationLog> invitations;

        public EventInvitationsVM(com.wedknots.model.WeddingEvent event, List<InvitationLog> invitations) {
            this.event = event;
            this.invitations = invitations;
        }

        public com.wedknots.model.WeddingEvent getEvent() { return event; }
        public List<InvitationLog> getInvitations() { return invitations; }
    }
}
