package com.wedknots.service;

import com.wedknots.model.Guest;
import com.wedknots.model.GuestMessage;
import com.wedknots.model.Invitation;
import com.wedknots.model.InvitationLog;
import com.wedknots.model.InvitationPhoneRecord;
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
import java.util.Map;
import java.util.Optional;

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
    private WhatsAppService whatsAppService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private InvitationPhoneRecordService invitationPhoneRecordService;

    @Transactional
    public List<InvitationLog> sendInvitationToGuests(Long invitationId, List<Long> guestIds, String sentBy) {
        Optional<Invitation> invitationOpt = invitationRepository.findById(invitationId);
        if (invitationOpt.isEmpty()) {
            throw new RuntimeException("Invitation not found with id: " + invitationId);
        }

        Invitation invitation = invitationOpt.get();
        List<InvitationLog> logs = new ArrayList<>();

        // Validate template requirements based on invitation's message type
        if ("TEMPLATE".equals(invitation.getMessageType())) {
            if (invitation.getTemplateName() == null || invitation.getTemplateName().isBlank()) {
                throw new RuntimeException("WhatsApp template name is required when message type is TEMPLATE.");
            }
        }

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

            // Create invitation log
            InvitationLog log = InvitationLog.builder()
                    .invitation(invitation)
                    .guest(guest)
                    .sentBy(sentBy)
                    .sentAt(LocalDateTime.now())
                    .whatsappNumber(guest.getContactPhone())
                    .deliveryStatus("PENDING")
                    .build();

            // Send WhatsApp message using invitation's message type and template settings
            String whatsappMessageId = null;
            String composedMessage = null;
            try {
                // Build the message body we send (plain or template preview)
                if (invitation.getMessageType() != null && invitation.getMessageType().equals("TEMPLATE")) {
                    // For template messages, use the stored message field which should contain the template preview
                    // If message is empty, build a basic preview from title
                    if (invitation.getMessage() != null && !invitation.getMessage().trim().isEmpty()) {
                        composedMessage = invitation.getMessage();
                    } else {
                        composedMessage = invitation.getTitle() + "\n\n" +
                                "(WhatsApp Template: " + invitation.getTemplateName() + ")\n" +
                                "Note: The actual template content was sent via WhatsApp but not stored for display.";
                    }
                } else {
                    // For plain text messages, combine title and message
                    composedMessage = (invitation.getTitle() != null ? invitation.getTitle() + "\n\n" : "") +
                            (invitation.getMessage() != null ? invitation.getMessage() : "");
                }

                boolean sent = whatsAppService.sendMessage(
                        invitation.getEvent(), // Pass the event for Cloud API configuration
                        guest.getContactPhone(),
                        invitation.getTitle(),
                        invitation.getMessage(),
                        invitation.getImageUrl(),
                        invitation.getMessageType(),
                        invitation.getTemplateName(),
                        invitation.getTemplateLanguage()
                );

                if (sent) {
                    log.setDeliveryStatus("SENT");
                    log.setDeliveryTimestamp(LocalDateTime.now());
                    log.setWhatsappMessageText(composedMessage);

                    // Create GuestMessage record so invitation appears in messages
                    try {
                        String messageText = composedMessage != null ? composedMessage : (invitation.getTitle() + "\n\n" + invitation.getMessage());
                        logger.info("Creating GuestMessage for guest {} with text: {}", guestId, messageText.substring(0, Math.min(50, messageText.length())) + "...");

                        GuestMessage guestMessage = messageService.createOutboundMessage(
                            invitation.getEvent(),
                            guest,
                            messageText
                        );
                        guestMessage.setStatus(GuestMessage.MessageStatus.SENT);
                        guestMessage.setMessageType(GuestMessage.MessageType.TEXT);
                        messageService.updateMessage(guestMessage);

                        logger.info("✓ Successfully created GuestMessage record ID {} for invitation to guest {} in event {}",
                            guestMessage.getId(), guestId, invitation.getEvent().getId());
                    } catch (Exception msgEx) {
                        logger.error("✗ Failed to create GuestMessage for invitation to guest {}: {}", guestId, msgEx.getMessage(), msgEx);
                        // Don't fail the invitation send if message record creation fails
                    }
                } else {
                    log.setDeliveryStatus("FAILED");
                    log.setErrorMessage("Failed to send WhatsApp message");
                }
            } catch (Exception e) {
                logger.error("Error sending WhatsApp to guest {}: {}", guestId, e.getMessage());
                log.setDeliveryStatus("FAILED");
                log.setErrorMessage(e.getMessage());
            }

            InvitationLog savedLog = invitationLogRepository.save(log);
            logs.add(savedLog);

            // Record invitation sent to each of the guest's phone numbers
            try {
                if (guest.getPhoneNumbers() != null && !guest.getPhoneNumbers().isEmpty()) {
                    logger.info("Recording invitation phone records for guest {} with {} phone numbers",
                            guestId, guest.getPhoneNumbers().size());
                    invitationPhoneRecordService.recordInvitationForAllGuestPhones(
                            savedLog.getId(),
                            "WHATSAPP",
                            savedLog.getDeliveryStatus()
                    );
                } else {
                    logger.warn("Guest {} has no phone numbers to record", guestId);
                }
            } catch (Exception phoneEx) {
                logger.error("Failed to record invitation phone records for guest {}: {}", guestId, phoneEx.getMessage());
                // Don't fail the overall operation if phone record creation fails
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

    @Transactional
    public void retryFailedDelivery(Long logId) {
        Optional<InvitationLog> logOpt = invitationLogRepository.findById(logId);
        if (logOpt.isEmpty() || !logOpt.get().getDeliveryStatus().equals("FAILED")) {
            return;
        }

        InvitationLog log = logOpt.get();
        try {
            boolean sent = whatsAppService.sendMessage(
                    log.getWhatsappNumber(),
                    log.getInvitation().getTitle(),
                    log.getInvitation().getMessage(),
                    log.getInvitation().getImageUrl()
            );

            if (sent) {
                log.setDeliveryStatus("SENT");
                log.setDeliveryTimestamp(LocalDateTime.now());
                log.setErrorMessage(null);

                // Create GuestMessage record for retry success
                try {
                    GuestMessage guestMessage = messageService.createOutboundMessage(
                        log.getInvitation().getEvent(),
                        log.getGuest(),
                        log.getInvitation().getTitle() + "\n\n" + log.getInvitation().getMessage()
                    );
                    guestMessage.setStatus(GuestMessage.MessageStatus.SENT);
                    messageService.updateMessage(guestMessage);
                    logger.info("Created GuestMessage record for retried invitation to guest {}", log.getGuest().getId());
                } catch (Exception msgEx) {
                    logger.error("Failed to create GuestMessage for retried invitation: {}", msgEx.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Retry failed for log {}: {}", logId, e.getMessage());
            log.setErrorMessage("Retry failed: " + e.getMessage());
        }

        invitationLogRepository.save(log);
    }

    /**
     * Mark an invitation as sent externally (email, phone call, in-person, etc.)
     * Creates an invitation log without actually sending via WhatsApp
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
                .invitationMethod("EXTERNAL")
                .externalMethodDescription(externalMethod)
                .build();

        InvitationLog savedLog = invitationLogRepository.save(log);
        logger.info("Marked invitation {} as sent externally to guest {} via {}",
                   invitationId, guestId, externalMethod);

        // Create GuestMessage record for external invitation
        try {
            GuestMessage guestMessage = messageService.createOutboundMessage(
                invitation.getEvent(),
                guest,
                "[External: " + externalMethod + "] " + invitation.getTitle() + "\n\n" + invitation.getMessage()
            );
            guestMessage.setStatus(GuestMessage.MessageStatus.DELIVERED);
            messageService.updateMessage(guestMessage);
            logger.info("Created GuestMessage record for external invitation to guest {}", guestId);
        } catch (Exception msgEx) {
            logger.error("Failed to create GuestMessage for external invitation: {}", msgEx.getMessage());
        }

        return savedLog;
    }

    /**
     * Mark multiple invitations as sent externally
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
     * Send invitation to guest recording it against all their phone numbers
     */
    @Transactional
    public InvitationLog sendInvitationWithAllPhones(Long invitationId, Long guestId, String sentBy, String contactMethod) {
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

        // Create invitation log
        InvitationLog log = InvitationLog.builder()
                .invitation(invitation)
                .guest(guest)
                .sentBy(sentBy)
                .sentAt(LocalDateTime.now())
                .whatsappNumber(guest.getPrimaryPhoneNumberString())
                .deliveryStatus("SENT")
                .build();

        InvitationLog savedLog = invitationLogRepository.save(log);

        // Record the invitation for ALL phone numbers of the guest
        invitationPhoneRecordService.recordInvitationForAllGuestPhones(
            savedLog.getId(),
            contactMethod != null ? contactMethod : "WHATSAPP",
            "SENT"
        );

        logger.info("Recorded invitation {} to guest {} with all phone numbers", invitationId, guestId);
        return savedLog;
    }

    /**
     * Send invitation to guest recording it against selected phone numbers
     */
    @Transactional
    public InvitationLog sendInvitationWithSelectedPhones(Long invitationId, Long guestId,
                                                          List<Long> selectedPhoneNumberIds, String sentBy,
                                                          String contactMethod) {
        Optional<Invitation> invitationOpt = invitationRepository.findById(invitationId);
        Optional<Guest> guestOpt = guestRepository.findById(guestId);

        if (invitationOpt.isEmpty()) {
            throw new RuntimeException("Invitation not found with id: " + invitationId);
        }
        if (guestOpt.isEmpty()) {
            throw new RuntimeException("Guest not found with id: " + guestId);
        }
        if (selectedPhoneNumberIds == null || selectedPhoneNumberIds.isEmpty()) {
            throw new RuntimeException("At least one phone number must be selected");
        }

        Invitation invitation = invitationOpt.get();
        Guest guest = guestOpt.get();

        // Check if invitation already sent to this guest
        Optional<InvitationLog> existingLog = invitationLogRepository.findByInvitationIdAndGuestId(invitationId, guestId);
        if (existingLog.isPresent()) {
            logger.info("Invitation already sent to guest: {}", guestId);
            throw new RuntimeException("Invitation already sent to this guest");
        }

        // Create invitation log
        InvitationLog log = InvitationLog.builder()
                .invitation(invitation)
                .guest(guest)
                .sentBy(sentBy)
                .sentAt(LocalDateTime.now())
                .whatsappNumber(guest.getPrimaryPhoneNumberString())
                .deliveryStatus("SENT")
                .build();

        InvitationLog savedLog = invitationLogRepository.save(log);

        // Record the invitation for SELECTED phone numbers of the guest
        invitationPhoneRecordService.recordInvitationForSelectedPhones(
            savedLog.getId(),
            selectedPhoneNumberIds,
            contactMethod != null ? contactMethod : "WHATSAPP",
            "SENT"
        );

        logger.info("Recorded invitation {} to guest {} with {} selected phone numbers",
                   invitationId, guestId, selectedPhoneNumberIds.size());
        return savedLog;
    }

    /**
     * Mark invitation as sent externally with phone numbers recorded
     */
    @Transactional
    public InvitationLog markInvitationSentExternallyWithPhones(Long invitationId, Long guestId,
                                                               List<Long> phoneNumberIds,
                                                               String externalMethod, String sentBy) {
        Optional<Invitation> invitationOpt = invitationRepository.findById(invitationId);
        if (invitationOpt.isEmpty()) {
            throw new RuntimeException("Invitation not found with id: " + invitationId);
        }

        Optional<Guest> guestOpt = guestRepository.findById(guestId);
        if (guestOpt.isEmpty()) {
            throw new RuntimeException("Guest not found with id: " + guestId);
        }

        Guest guest = guestOpt.get();

        // Check if invitation already sent to this guest
        Optional<InvitationLog> existingLog = invitationLogRepository.findByInvitationIdAndGuestId(invitationId, guestId);
        if (existingLog.isPresent()) {
            logger.info("Invitation already sent to guest: {}", guestId);
            throw new RuntimeException("Invitation already sent to this guest");
        }

        // Create invitation log for external invitation
        InvitationLog log = InvitationLog.builder()
                .invitation(invitationOpt.get())
                .guest(guest)
                .sentBy(sentBy)
                .sentAt(LocalDateTime.now())
                .deliveryStatus("SENT")
                .deliveryTimestamp(LocalDateTime.now())
                .invitationMethod("EXTERNAL")
                .externalMethodDescription(externalMethod)
                .build();

        InvitationLog savedLog = invitationLogRepository.save(log);

        // Record for specific phones if provided, otherwise all phones
        if (phoneNumberIds != null && !phoneNumberIds.isEmpty()) {
            invitationPhoneRecordService.recordInvitationForSelectedPhones(
                savedLog.getId(),
                phoneNumberIds,
                externalMethod,
                "SENT"
            );
        } else {
            invitationPhoneRecordService.recordInvitationForAllGuestPhones(
                savedLog.getId(),
                externalMethod,
                "SENT"
            );
        }

        logger.info("Marked invitation {} as sent externally to guest {} via {} on phone numbers",
                   invitationId, guestId, externalMethod);

        return savedLog;
    }

    /**
     * Get phone contact history for an invitation (how many phones contacted, delivery status, etc.)
     */
    public List<InvitationPhoneRecord> getPhoneContactHistory(Long invitationId) {
        return invitationPhoneRecordService.getPhoneRecordsForInvitation(invitationId);
    }

    /**
     * Get statistics about phone numbers contacted for an invitation
     */
    public Map<String, Object> getPhoneContactStatistics(Long invitationId) {
        return invitationPhoneRecordService.getInvitationStatistics(invitationId);
    }
}
