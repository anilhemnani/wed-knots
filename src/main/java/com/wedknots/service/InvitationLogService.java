package com.wedknots.service;

import com.wedknots.delivery.DeliveryMode;
import com.wedknots.delivery.DeliveryRequest;
import com.wedknots.delivery.MessageDeliveryService;
import com.wedknots.model.*;
import com.wedknots.repository.GuestRepository;
import com.wedknots.repository.InvitationLogRepository;
import com.wedknots.repository.InvitationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing invitation logs
 * Tracks which guests have been sent invitations and via what method
 */
@Service
public class InvitationLogService {
    private static final Logger logger = LoggerFactory.getLogger(InvitationLogService.class);

    @Autowired
    private InvitationLogRepository invitationLogRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private MessageService messageService;

    @Autowired
    private InvitationPhoneRecordService invitationPhoneRecordService;

    @Autowired
    private MessageDeliveryService messageDeliveryService;

    /**
     * Record invitations as sent to multiple guests
     * Supports both sending notifications and just recording the send
     * @param invitationId ID of the invitation
     * @param guestIds List of guest IDs to send to
     * @param sentBy Username of person sending
     * @param method Invitation method (EMAIL, SMS, IN_PERSON, etc.)
     * @return List of created invitation logs
     */
    @Transactional
    public List<InvitationLog> sendInvitationToGuests(Long invitationId, List<Long> guestIds, String sentBy, String method) {
        Optional<Invitation> invitationOpt = invitationRepository.findById(invitationId);
        if (invitationOpt.isEmpty()) {
            throw new RuntimeException("Invitation not found with id: " + invitationId);
        }

        Invitation invitation = invitationOpt.get();
        List<InvitationLog> logs = new ArrayList<>();

        for (Long guestId : guestIds) {
            Optional<Guest> guestOpt = guestRepository.findById(guestId);
            if (guestOpt.isEmpty()) {
                logger.warn("Guest not found with id: {}", guestId);
                continue;
            }

            Guest guest = guestOpt.get();

            // Check if invitation already sent to this guest
            Optional<InvitationLog> existingLog = invitationLogRepository.findByInvitationIdAndGuestId(invitationId, guestId);
            if (existingLog.isPresent()) {
                logger.info("Invitation already sent to guest: {}", guestId);
                continue;
            }

            // Create invitation log - mark as sent
            InvitationLog log = InvitationLog.builder()
                    .invitation(invitation)
                    .guest(guest)
                    .sentBy(sentBy)
                    .sentAt(LocalDateTime.now())
                    .deliveryStatus("SENT")
                    .deliveryTimestamp(LocalDateTime.now())
                    .invitationMethod(method != null ? method : "EMAIL")
                    .build();

            InvitationLog savedLog = invitationLogRepository.save(log);
            logs.add(savedLog);

            logger.info("✓ Recorded invitation sent to guest {} via {}", guestId, method);

            // Create GuestMessage record for tracking
            try {
                String messageText = (invitation.getTitle() != null ? invitation.getTitle() + "\n\n" : "") +
                        (invitation.getMessage() != null ? invitation.getMessage() : "");

                GuestMessage guestMessage = messageService.createOutboundMessage(
                    invitation.getEvent(),
                    guest,
                    messageText
                );
                guestMessage.setStatus(GuestMessage.MessageStatus.SENT);
                guestMessage.setMessageType(GuestMessage.MessageType.TEXT);
                messageService.updateMessage(guestMessage);

                logger.info("Created GuestMessage record for invitation to guest {}", guestId);
            } catch (Exception msgEx) {
                logger.error("Failed to create GuestMessage for invitation to guest {}: {}", guestId, msgEx.getMessage());
                // Don't fail the overall operation
            }

            // Record invitation sent to each of the guest's phone numbers if they have any
            try {
                if (guest.getPhoneNumbers() != null && !guest.getPhoneNumbers().isEmpty()) {
                    logger.info("Recording invitation phone records for guest {} with {} phone numbers",
                            guestId, guest.getPhoneNumbers().size());
                    invitationPhoneRecordService.recordInvitationForAllGuestPhones(
                            savedLog.getId(),
                            method != null ? method : "EMAIL",
                            "SENT"
                    );
                }
            } catch (Exception phoneEx) {
                logger.error("Failed to record invitation phone records for guest {}: {}", guestId, phoneEx.getMessage());
                // Don't fail if phone records can't be created
            }
        }

        return logs;
    }

    @Transactional(readOnly = true)
    public List<InvitationLog> getLogsByInvitation(Long invitationId) {
        return invitationLogRepository.findByInvitationId(invitationId);
    }

    @Transactional(readOnly = true)
    public List<InvitationLog> getLogsByGuest(Long guestId) {
        return invitationLogRepository.findByGuestId(guestId);
    }

    @Transactional(readOnly = true)
    public List<InvitationLog> getLogsByEvent(Long eventId) {
        return invitationLogRepository.findByEventId(eventId);
    }

    @Transactional(readOnly = true)
    public Long getInvitationSentCount(Long invitationId) {
        return invitationLogRepository.countByInvitationId(invitationId);
    }

    @Transactional(readOnly = true)
    public Long getDeliveredCount(Long invitationId) {
        return invitationLogRepository.countByInvitationIdAndStatus(invitationId, "SENT");
    }

    @Transactional(readOnly = true)
    public Long getFailedCount(Long invitationId) {
        return invitationLogRepository.countByInvitationIdAndStatus(invitationId, "FAILED");
    }

    /**
     * Mark an invitation as sent externally (email, phone call, in-person, SMS, etc.)
     * Creates an invitation log without actually sending via any service
     * @param invitationId ID of the invitation
     * @param guestId ID of the guest
     * @param externalMethod Description of how it was sent (e.g., "Email", "Phone Call", "SMS")
     * @param sentBy Username of person who sent it
     * @return Created invitation log
     */
    @Transactional
    public InvitationLog markInvitationSentExternally(Long invitationId, Long guestId,
                                                       String externalMethod, String sentBy) {
        Optional<Invitation> invitationOpt = invitationRepository.findById(invitationId);
        Optional<Guest> guestOpt = guestRepository.findById(guestId);

        if (invitationOpt.isEmpty()) {
            throw new RuntimeException("Invitation not found with id: " + invitationId);
        }
        if (guestOpt.isEmpty()) {
            throw new RuntimeException("Guest not found with id: " + guestId);
        }

        Invitation invitation = invitationOpt.get();
        Guest guest = guestOpt.get();

        // Check if invitation already sent to this guest
        Optional<InvitationLog> existingLog = invitationLogRepository.findByInvitationIdAndGuestId(invitationId, guestId);
        if (existingLog.isPresent()) {
            logger.info("Invitation already sent to guest: {}", guestId);
            throw new RuntimeException("Invitation already sent to this guest");
        }

        // Create invitation log for external invitation
        InvitationLog log = InvitationLog.builder()
                .invitation(invitation)
                .guest(guest)
                .sentBy(sentBy)
                .sentAt(LocalDateTime.now())
                .deliveryStatus("SENT")
                .deliveryTimestamp(LocalDateTime.now())
                .invitationMethod("EMAIL")
                .externalMethodDescription(externalMethod)
                .build();

        InvitationLog savedLog = invitationLogRepository.save(log);
        logger.info("Marked invitation {} as sent externally to guest {} via {}", invitationId, guestId, externalMethod);

        // Create GuestMessage record for external invitation
        try {
            GuestMessage guestMessage = messageService.createOutboundMessage(
                invitation.getEvent(),
                guest,
                "[" + externalMethod + "] " + invitation.getTitle() + "\n\n" + invitation.getMessage()
            );
            guestMessage.setStatus(GuestMessage.MessageStatus.DELIVERED);
            messageService.updateMessage(guestMessage);
            logger.info("Created GuestMessage record for external invitation to guest {}", guestId);
        } catch (Exception msgEx) {
            logger.error("Failed to create GuestMessage for external invitation: {}", msgEx.getMessage());
        }

        // Record invitation for guest's phone numbers
        try {
            if (guest.getPhoneNumbers() != null && !guest.getPhoneNumbers().isEmpty()) {
                invitationPhoneRecordService.recordInvitationForAllGuestPhones(
                    savedLog.getId(),
                    externalMethod,
                    "SENT"
                );
            }
        } catch (Exception phoneEx) {
            logger.error("Failed to record invitation phone records: {}", phoneEx.getMessage());
        }

        return savedLog;
    }

    /**
     * Mark multiple invitations as sent externally
     * @param invitationId ID of the invitation
     * @param guestIds List of guest IDs
     * @param externalMethod How it was sent (Email, SMS, etc.)
     * @param sentBy Username of person who sent it
     * @return List of created invitation logs
     */
    @Transactional
    public List<InvitationLog> markMultipleInvitationsSentExternally(Long invitationId, List<Long> guestIds,
                                                                      String externalMethod, String sentBy) {
        List<InvitationLog> logs = new ArrayList<>();
        for (Long guestId : guestIds) {
            try {
                InvitationLog log = markInvitationSentExternally(invitationId, guestId, externalMethod, sentBy);
                logs.add(log);
            } catch (Exception e) {
                logger.warn("Failed to mark invitation as sent externally for guest {}: {}", guestId, e.getMessage());
            }
        }
        return logs;
    }

    /**
     * Get phone contact history for an invitation
     * @param invitationId ID of the invitation
     * @return List of phone records showing which phones received the invitation
     */
    public List<InvitationPhoneRecord> getPhoneContactHistory(Long invitationId) {
        return invitationPhoneRecordService.getPhoneRecordsForInvitation(invitationId);
    }

    /**
     * Queue invitations for delivery via selected mode (email, sms, whatsapp_personal, whatsapp_adb)
     * Creates InvitationLog entries with QUEUED status and enqueues DeliveryRequests
     */
    @Transactional
    public List<InvitationLog> queueInvitationsForDelivery(Long invitationId, List<Long> guestIds, String queuedBy, String mode) {
        Optional<Invitation> invitationOpt = invitationRepository.findById(invitationId);
        if (invitationOpt.isEmpty()) {
            throw new RuntimeException("Invitation not found with id: " + invitationId);
        }
        Invitation invitation = invitationOpt.get();

        // Use invitation's delivery method if mode not specified
        String deliveryMethod = (mode != null && !mode.isEmpty()) ? mode : invitation.getDeliveryMethod();
        if (deliveryMethod == null || deliveryMethod.isEmpty()) {
            throw new RuntimeException("No delivery method specified");
        }

        DeliveryMode deliveryMode = mapDeliveryMethodToMode(deliveryMethod);
        List<InvitationLog> logs = new ArrayList<>();

        for (Long guestId : guestIds) {
            Optional<Guest> guestOpt = guestRepository.findById(guestId);
            if (guestOpt.isEmpty()) {
                logger.warn("Guest not found with id: {}", guestId);
                continue;
            }
            Guest guest = guestOpt.get();

            // Build delivery request based on invitation's delivery method
            String title = getContentTitle(invitation, deliveryMethod);
            String content = getContentBody(invitation, deliveryMethod);

            DeliveryRequest request = DeliveryRequest.builder()
                    .messageType("INVITATION")
                    .title(title)
                    .content(content)
                    .recipient(guest)
                    .event(invitation.getEvent())
                    .preferredMode(deliveryMode)
                    .build();

            String messageId = messageDeliveryService.queueMessage(request);

            // Create log with QUEUED status
            InvitationLog log = InvitationLog.builder()
                    .invitation(invitation)
                    .guest(guest)
                    .sentBy(queuedBy)
                    .sentAt(LocalDateTime.now())
                    .deliveryStatus("QUEUED")
                    .invitationMethod(deliveryMethod)
                    .build();
            InvitationLog saved = invitationLogRepository.save(log);
            logs.add(saved);

            logger.info("Queued invitation {} for guest {} via {} (messageId={})", invitationId, guestId, deliveryMethod, messageId);
        }

        return logs;
    }

    /**
     * Get content title based on delivery method
     */
    private String getContentTitle(Invitation invitation, String deliveryMethod) {
        switch (deliveryMethod.toLowerCase()) {
            case "email":
                return invitation.getEmailSubject() != null ? invitation.getEmailSubject() : invitation.getTitle();
            case "sms":
            case "whatsapp-personal":
            case "whatsapp-adb":
            case "whatsapp-business":
            case "external":
            default:
                return invitation.getTitle();
        }
    }

    /**
     * Get content body based on delivery method
     */
    private String getContentBody(Invitation invitation, String deliveryMethod) {
        switch (deliveryMethod.toLowerCase()) {
            case "email":
                return invitation.getEmailBody() != null ? invitation.getEmailBody() : invitation.getMessage();
            case "sms":
                return invitation.getSmsText() != null ? invitation.getSmsText() : invitation.getMessage();
            case "whatsapp-personal":
            case "whatsapp-adb":
            case "whatsapp-business":
                return invitation.getWhatsappText() != null ? invitation.getWhatsappText() : invitation.getMessage();
            case "external":
            default:
                return invitation.getMessage();
        }
    }

    /**
     * Map delivery method string to DeliveryMode enum
     */
    private DeliveryMode mapDeliveryMethodToMode(String deliveryMethod) {
        if (deliveryMethod == null) return DeliveryMode.EMAIL;
        switch (deliveryMethod.toLowerCase()) {
            case "email":
                return DeliveryMode.EMAIL;
            case "sms":
                return DeliveryMode.SMS;
            case "whatsapp-personal":
                return DeliveryMode.WHATSAPP_PERSONAL;
            case "whatsapp-adb":
                return DeliveryMode.WHATSAPP_ADB;
            case "whatsapp-business":
                return DeliveryMode.WHATSAPP_PERSONAL; // Map to personal for now
            case "external":
                return DeliveryMode.EXTERNAL;
            default:
                logger.warn("Unknown delivery method '{}', defaulting to EMAIL", deliveryMethod);
                return DeliveryMode.EMAIL;
        }
    }

    private DeliveryMode mapMode(String mode) {
        return mapDeliveryMethodToMode(mode);
    }
}
