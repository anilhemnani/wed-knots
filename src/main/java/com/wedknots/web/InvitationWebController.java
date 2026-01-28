package com.wedknots.web;

import com.wedknots.model.Guest;
import com.wedknots.model.Invitation;
import com.wedknots.model.InvitationLog;
import com.wedknots.model.WeddingEvent;
import com.wedknots.repository.GuestRepository;
import com.wedknots.repository.WeddingEventRepository;
import com.wedknots.service.InvitationLogService;
import com.wedknots.service.InvitationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/events/{eventId}/invitations")
public class InvitationWebController {

    private static final Logger logger = LoggerFactory.getLogger(InvitationWebController.class);

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private InvitationLogService invitationLogService;

    @Autowired
    private com.wedknots.service.DeliveryOptionsService deliveryOptionsService;

    @Autowired
    private WeddingEventRepository weddingEventRepository;

    @Autowired
    private GuestRepository guestRepository;

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @GetMapping
    public String listInvitations(@PathVariable Long eventId, Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return "redirect:/events";
        }

        WeddingEvent event = eventOpt.get();
        List<Invitation> invitations = invitationService.getInvitationsByEvent(eventId);

        model.addAttribute("event", event);
        model.addAttribute("invitations", invitations);
        return "invitation_list";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @GetMapping("/new")
    public String newInvitation(@PathVariable Long eventId, Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return "redirect:/events";
        }

        WeddingEvent event = eventOpt.get();
        Invitation newInvitation = new Invitation();
        newInvitation.setMessageType("PLAIN_TEXT");
        newInvitation.setTemplateLanguage("en_US");

        model.addAttribute("event", event);
        model.addAttribute("invitation", newInvitation);
        model.addAttribute("deliveryOptions", deliveryOptionsService.getEnabledDeliveryOptions());

        return "invitation_form";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PostMapping("/new")
    public String createInvitation(@PathVariable Long eventId,
                                   @ModelAttribute Invitation invitation,
                                   @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                   RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        try {
            // If a file is uploaded, override imageUrl with data URI
            if (imageFile != null && !imageFile.isEmpty()) {
                byte[] bytes = imageFile.getBytes();
                String contentType = imageFile.getContentType() != null ? imageFile.getContentType() : "image/png";
                String dataUri = "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(bytes);
                invitation.setImageUrl(dataUri);
            }

            invitationService.createInvitation(eventId, invitation, username);
            redirectAttributes.addFlashAttribute("successMessage", "Invitation created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create invitation: " + e.getMessage());
        }

        return "redirect:/events/" + eventId + "/invitations";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @GetMapping("/{invitationId}/edit")
    public String editInvitation(@PathVariable Long eventId,
                                  @PathVariable Long invitationId,
                                  Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        Optional<Invitation> invitationOpt = invitationService.getInvitationById(invitationId);

        if (eventOpt.isEmpty() || invitationOpt.isEmpty()) {
            return "redirect:/events/" + eventId + "/invitations";
        }

        WeddingEvent event = eventOpt.get();
        model.addAttribute("event", event);
        model.addAttribute("invitation", invitationOpt.get());
        model.addAttribute("deliveryOptions", deliveryOptionsService.getEnabledDeliveryOptions());

        return "invitation_form";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PostMapping("/{invitationId}/edit")
    public String updateInvitation(@PathVariable Long eventId,
                                    @PathVariable Long invitationId,
                                    @ModelAttribute Invitation invitation,
                                    @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                    RedirectAttributes redirectAttributes) {
        try {
            // If a new file is uploaded, override imageUrl; otherwise keep existing imageUrl
            if (imageFile != null && !imageFile.isEmpty()) {
                byte[] bytes = imageFile.getBytes();
                String contentType = imageFile.getContentType() != null ? imageFile.getContentType() : "image/png";
                String dataUri = "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(bytes);
                invitation.setImageUrl(dataUri);
            }

            invitationService.updateInvitation(invitationId, invitation);
            redirectAttributes.addFlashAttribute("successMessage", "Invitation updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update invitation: " + e.getMessage());
        }

        return "redirect:/events/" + eventId + "/invitations";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PostMapping("/{invitationId}/delete")
    public String deleteInvitation(@PathVariable Long eventId,
                                    @PathVariable Long invitationId,
                                    RedirectAttributes redirectAttributes) {
        try {
            invitationService.deleteInvitation(invitationId);
            redirectAttributes.addFlashAttribute("successMessage", "Invitation deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete invitation: " + e.getMessage());
        }

        return "redirect:/events/" + eventId + "/invitations";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @GetMapping("/{invitationId}/send")
    public String showSendInvitation(@PathVariable Long eventId,
                                      @PathVariable Long invitationId,
                                      @RequestParam(required = false) String side,
                                      @RequestParam(required = false, defaultValue = "email") String method,
                                      Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        Optional<Invitation> invitationOpt = invitationService.getInvitationById(invitationId);

        if (eventOpt.isEmpty() || invitationOpt.isEmpty()) {
            return "redirect:/events/" + eventId + "/invitations";
        }

        WeddingEvent event = eventOpt.get();
        Invitation invitation = invitationOpt.get();

        // Get guests for the event via repository (avoids filtering all guests)
        List<Guest> guests;
        if (side != null && !side.isEmpty() && !side.equalsIgnoreCase("ALL")) {
            guests = guestRepository.findByEventIdAndSideIgnoreCase(eventId, side);
        } else {
            guests = guestRepository.findByEventId(eventId);
        }

        // Get invitation logs to mark already sent
        List<InvitationLog> logs = invitationLogService.getLogsByInvitation(invitationId);
        List<Long> sentGuestIds = logs.stream()
                .map(log -> log.getGuest().getId())
                .collect(Collectors.toList());

        model.addAttribute("event", event);
        model.addAttribute("invitation", invitation);
        model.addAttribute("guests", guests);
        model.addAttribute("sentGuestIds", sentGuestIds);
        model.addAttribute("selectedSide", side != null ? side : "ALL");
        model.addAttribute("method", method);
        model.addAttribute("externalMethods", new String[]{"Email", "Phone Call", "SMS", "In-person", "Other"});
        return "invitation_send";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PostMapping("/{invitationId}/send")
    public String sendInvitation(@PathVariable Long eventId,
                                  @PathVariable Long invitationId,
                                  @RequestParam List<Long> guestIds,
                                  @RequestParam(required = false, defaultValue = "email") String method,
                                  @RequestParam(required = false) String externalMethod,
                                  RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        try {
            List<InvitationLog> logs;

            if ("external".equalsIgnoreCase(method)) {
                // Mark invitations as sent externally (no message actually queued)
                if (externalMethod == null || externalMethod.isBlank()) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "External invitation method is required");
                    return "redirect:/events/" + eventId + "/invitations/" + invitationId + "/send";
                }

                logs = invitationLogService.markMultipleInvitationsSentExternally(
                        invitationId, guestIds, externalMethod, username);

                redirectAttributes.addFlashAttribute("successMessage",
                        String.format("Marked %d invitation(s) as sent via %s", logs.size(), externalMethod));
            } else {
                // Queue for async delivery via selected method (email, sms, etc.)
                logs = invitationLogService.queueInvitationsForDelivery(invitationId, guestIds, username, method);
                redirectAttributes.addFlashAttribute("successMessage",
                        String.format("Queued %d invitation(s) for delivery via %s", logs.size(), method));
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to send invitations: " + e.getMessage());
        }

        return "redirect:/events/" + eventId + "/invitations/" + invitationId + "/logs";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PostMapping("/{invitationId}/send-external")
    public String sendExternalInvitation(@PathVariable Long eventId,
                                         @PathVariable Long invitationId,
                                         @RequestParam Long guestId,
                                         @RequestParam String externalMethod,
                                         RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        try {
            if (externalMethod == null || externalMethod.isBlank()) {
                throw new RuntimeException("External invitation method is required");
            }

            invitationLogService.markInvitationSentExternally(invitationId, guestId, externalMethod, username);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Invitation marked as sent via " + externalMethod);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to mark invitation: " + e.getMessage());
        }

        return "redirect:/events/" + eventId + "/invitations/" + invitationId + "/logs";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @GetMapping("/{invitationId}/logs")
    public String viewInvitationLogs(@PathVariable Long eventId,
                                      @PathVariable Long invitationId,
                                      Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        Optional<Invitation> invitationOpt = invitationService.getInvitationById(invitationId);

        if (eventOpt.isEmpty() || invitationOpt.isEmpty()) {
            return "redirect:/events/" + eventId + "/invitations";
        }

        List<InvitationLog> logs = invitationLogService.getLogsByInvitation(invitationId);
        Long sentCount = invitationLogService.getInvitationSentCount(invitationId);
        Long deliveredCount = invitationLogService.getDeliveredCount(invitationId);
        Long failedCount = invitationLogService.getFailedCount(invitationId);

        model.addAttribute("event", eventOpt.get());
        model.addAttribute("invitation", invitationOpt.get());
        model.addAttribute("logs", logs);
        model.addAttribute("sentCount", sentCount);
        model.addAttribute("deliveredCount", deliveredCount);
        model.addAttribute("failedCount", failedCount);
        return "invitation_logs";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PostMapping("/{invitationId}/activate")
    public String activateInvitation(@PathVariable Long eventId,
                                      @PathVariable Long invitationId,
                                      RedirectAttributes redirectAttributes) {
        try {
            invitationService.activateInvitation(invitationId);
            redirectAttributes.addFlashAttribute("successMessage", "Invitation activated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to activate invitation: " + e.getMessage());
        }

        return "redirect:/events/" + eventId + "/invitations";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PostMapping("/{invitationId}/archive")
    public String archiveInvitation(@PathVariable Long eventId,
                                     @PathVariable Long invitationId,
                                     RedirectAttributes redirectAttributes) {
        try {
            invitationService.archiveInvitation(invitationId);
            redirectAttributes.addFlashAttribute("successMessage", "Invitation archived!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to archive invitation: " + e.getMessage());
        }

        return "redirect:/events/" + eventId + "/invitations";
    }
}

