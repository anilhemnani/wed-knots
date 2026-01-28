package com.wedknots.service;

import com.wedknots.model.Invitation;
import com.wedknots.model.WeddingEvent;
import com.wedknots.repository.InvitationRepository;
import com.wedknots.repository.WeddingEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InvitationService {

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private WeddingEventRepository weddingEventRepository;

    @Transactional
    public Invitation createInvitation(Long eventId, Invitation invitation, String createdBy) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            throw new RuntimeException("Event not found with id: " + eventId);
        }

        // Validate delivery method and required fields
        validateInvitation(invitation);

        invitation.setEvent(eventOpt.get());
        invitation.setCreatedBy(createdBy);
        invitation.setCreatedAt(LocalDateTime.now());
        invitation.setStatus("DRAFT");

        return invitationRepository.save(invitation);
    }

    @Transactional
    public Invitation updateInvitation(Long invitationId, Invitation updatedInvitation) {
        Optional<Invitation> existingOpt = invitationRepository.findById(invitationId);
        if (existingOpt.isEmpty()) {
            throw new RuntimeException("Invitation not found with id: " + invitationId);
        }

        // Validate delivery method and required fields
        validateInvitation(updatedInvitation);

        Invitation existing = existingOpt.get();
        existing.setTitle(updatedInvitation.getTitle());
        existing.setMessage(updatedInvitation.getMessage());
        existing.setInvitationType(updatedInvitation.getInvitationType());
        existing.setImageUrl(updatedInvitation.getImageUrl());
        existing.setStatus(updatedInvitation.getStatus());
        existing.setMessageType(updatedInvitation.getMessageType());
        existing.setTemplateName(updatedInvitation.getTemplateName());
        existing.setTemplateLanguage(updatedInvitation.getTemplateLanguage());

        // Update delivery method and method-specific fields
        existing.setDeliveryMethod(updatedInvitation.getDeliveryMethod());
        existing.setEmailSubject(updatedInvitation.getEmailSubject());
        existing.setEmailBody(updatedInvitation.getEmailBody());
        existing.setEmailAttachments(updatedInvitation.getEmailAttachments());
        existing.setSmsText(updatedInvitation.getSmsText());
        existing.setWhatsappText(updatedInvitation.getWhatsappText());
        existing.setWhatsappMediaUrl(updatedInvitation.getWhatsappMediaUrl());
        existing.setWhatsappMediaType(updatedInvitation.getWhatsappMediaType());

        return invitationRepository.save(existing);
    }

    /**
     * Validate invitation based on delivery method
     */
    private void validateInvitation(Invitation invitation) {
        String method = invitation.getDeliveryMethod();
        if (method == null || method.trim().isEmpty()) {
            throw new RuntimeException("Delivery method is required");
        }

        switch (method.toLowerCase()) {
            case "email":
                if (isEmpty(invitation.getEmailSubject())) {
                    throw new RuntimeException("Email subject is required");
                }
                if (isEmpty(invitation.getEmailBody())) {
                    throw new RuntimeException("Email body is required");
                }
                break;
            case "sms":
                if (isEmpty(invitation.getSmsText())) {
                    throw new RuntimeException("SMS text is required");
                }
                if (invitation.getSmsText().length() > 160) {
                    throw new RuntimeException("SMS text must be 160 characters or less");
                }
                break;
            case "whatsapp-personal":
            case "whatsapp-adb":
            case "whatsapp-business":
                if (isEmpty(invitation.getWhatsappText())) {
                    throw new RuntimeException("WhatsApp message is required");
                }
                break;
            case "external":
                // No specific validation for external delivery
                break;
            default:
                throw new RuntimeException("Unknown delivery method: " + method);
        }
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    @Transactional(readOnly = true)
    public List<Invitation> getInvitationsByEvent(Long eventId) {
        return invitationRepository.findByEventIdOrderByCreatedAtDesc(eventId);
    }

    @Transactional(readOnly = true)
    public Optional<Invitation> getInvitationById(Long invitationId) {
        return invitationRepository.findById(invitationId);
    }

    @Transactional
    public void deleteInvitation(Long invitationId) {
        invitationRepository.deleteById(invitationId);
    }

    @Transactional
    public void activateInvitation(Long invitationId) {
        Optional<Invitation> invitationOpt = invitationRepository.findById(invitationId);
        if (invitationOpt.isPresent()) {
            Invitation invitation = invitationOpt.get();
            invitation.setStatus("ACTIVE");
            invitationRepository.save(invitation);
        }
    }

    @Transactional
    public void archiveInvitation(Long invitationId) {
        Optional<Invitation> invitationOpt = invitationRepository.findById(invitationId);
        if (invitationOpt.isPresent()) {
            Invitation invitation = invitationOpt.get();
            invitation.setStatus("ARCHIVED");
            invitationRepository.save(invitation);
        }
    }

    @Transactional(readOnly = true)
    public List<Invitation> getActiveInvitations(Long eventId) {
        return invitationRepository.findByEventIdAndStatus(eventId, "ACTIVE");
    }
}

