package com.wedknots.web;

import com.wedknots.model.GuestMessage;
import com.wedknots.model.InvitationLog;
import com.wedknots.repository.InvitationLogRepository;
import com.wedknots.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/guest/messages")
public class GuestMessagesController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private InvitationLogRepository invitationLogRepository;

    @PreAuthorize("hasRole('GUEST')")
    @GetMapping("/event/{eventId}")
    public String guestMessages(@PathVariable Long eventId,
                                @RequestParam(required = false) Long guestId,
                                HttpServletRequest request,
                                Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return "redirect:/login";
        }

        Object guestIdAttr = request.getSession().getAttribute("guestId");
        Long resolvedGuestId = guestIdAttr instanceof Integer ? ((Integer) guestIdAttr).longValue() : (Long) guestIdAttr;
        if (resolvedGuestId == null) {
            return "redirect:/login";
        }
        if (guestId != null && !guestId.equals(resolvedGuestId)) {
            return "redirect:/guest/dashboard";
        }

        // Ensure guest has an invitation for this event
        List<InvitationLog> logs = invitationLogRepository.findByGuestIdAndEventId(resolvedGuestId, eventId);
        if (logs.isEmpty()) {
            model.addAttribute("error", "No invitations for this event");
            return "guest_messages";
        }

        // Fetch messages (ordered newest first from service)
        List<GuestMessage> messages = messageService.getGuestMessages(eventId, resolvedGuestId, org.springframework.data.domain.PageRequest.of(0, 100)).getContent();

        model.addAttribute("messages", messages);
        model.addAttribute("event", logs.get(0).getInvitation().getEvent());
        model.addAttribute("guest", logs.get(0).getGuest());
        return "guest_messages";
    }
}

