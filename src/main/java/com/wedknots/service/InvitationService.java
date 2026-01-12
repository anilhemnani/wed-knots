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

        Invitation existing = existingOpt.get();
        existing.setTitle(updatedInvitation.getTitle());
        existing.setMessage(updatedInvitation.getMessage());
        existing.setInvitationType(updatedInvitation.getInvitationType());
        existing.setImageUrl(updatedInvitation.getImageUrl());
        existing.setStatus(updatedInvitation.getStatus());
        existing.setMessageType(updatedInvitation.getMessageType());
        existing.setTemplateName(updatedInvitation.getTemplateName());
        existing.setTemplateLanguage(updatedInvitation.getTemplateLanguage());

        return invitationRepository.save(existing);
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

