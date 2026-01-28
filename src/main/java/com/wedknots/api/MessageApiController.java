package com.wedknots.api;

import com.wedknots.model.Guest;
import com.wedknots.model.GuestMessage;
import com.wedknots.dto.GuestMessageDTO;
import com.wedknots.dto.PagedMessageResponse;
import com.wedknots.model.WeddingEvent;
import com.wedknots.service.MessageService;
import com.wedknots.repository.GuestRepository;
import com.wedknots.repository.WeddingEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * REST API controller for message operations
 * Handles sending messages between guests and hosts
 */
@RestController
@RequestMapping("/api/messages")
public class MessageApiController {
    private static final Logger logger = LoggerFactory.getLogger(MessageApiController.class);

    @Autowired
    private MessageService messageService;


    @Autowired
    private WeddingEventRepository weddingEventRepository;

    @Autowired
    private GuestRepository guestRepository;

    /**
     * Host sends a message to a guest
     * POST /api/messages/send-to-guest
     */
    @PostMapping("/send-to-guest")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<?> sendMessageToGuest(@RequestBody SendMessageRequest request) {
        try {
            // Validate request
            if (request.getEventId() == null || request.getGuestId() == null ||
                request.getMessageContent() == null || request.getMessageContent().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Missing required fields: eventId, guestId, messageContent")
                );
            }

            // Get event
            WeddingEvent event = weddingEventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

            // Get guest
            Guest guest = guestRepository.findById(request.getGuestId())
                .orElseThrow(() -> new RuntimeException("Guest not found"));

            // Store the outbound message in database
            GuestMessage message = messageService.createOutboundMessage(
                event,
                guest,
                request.getMessageContent()
            );

            logger.info("Outbound message created for guest {} in event {}. Message ID: {}",
                guest.getId(), event.getId(), message.getId());

            // WhatsApp support removed - messages are stored but not sent
            message.setStatus(GuestMessage.MessageStatus.DELIVERED);
                    message.setStatus(GuestMessage.MessageStatus.SENT);
                    messageService.updateMessage(message);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "messageId", message.getId(),
                "message", "Message sent successfully"
            ));

        } catch (Exception e) {
            logger.error("Error sending message to guest", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", "Failed to send message: " + e.getMessage())
            );
        }
    }

    /**
     * Guest sends a message to hosts for a specific event
     * POST /api/messages/send-to-host
     */
    @PostMapping("/send-to-host")
    @PreAuthorize("hasRole('GUEST')")
    public ResponseEntity<?> sendMessageToHost(@RequestBody SendMessageRequest request) {
        try {
            // Validate request
            if (request.getEventId() == null || request.getMessageContent() == null ||
                request.getMessageContent().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Missing required fields: eventId, messageContent")
                );
            }

            // Get event
            WeddingEvent event = weddingEventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

            // Get authenticated guest using same resolution order
            Guest guest = null;

            // 1) Prefer explicit guestId when provided in request
            if (request.getGuestId() != null) {
                guest = guestRepository.findById(request.getGuestId()).orElse(null);
            }

            // 2) Fallback to authenticated principal: try phone first, then email
            if (guest == null) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String principal = auth != null ? auth.getName() : null;
                if (principal != null) {
                    guest = guestRepository.findByContactPhone(principal);
                    if (guest == null) {
                        guest = guestRepository.findByContactEmail(principal);
                    }
                }
            }

            if (guest == null) {
                logger.warn("Guest not found when trying to send message to host");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "Guest not found")
                );
            }

            // Verify guest is invited to this event
            if (guest.getEventId() == null || !guest.getEventId().equals(event.getId())) {
                logger.warn("Guest {} attempted to send message for event {} they are not invited to",
                    guest.getId(), event.getId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Guest is not invited to this event")
                );
            }

            // Store the inbound message from guest
            GuestMessage message = GuestMessage.builder()
                .event(event)
                .guest(guest)
                .guestPhoneNumber(guest.getContactPhone())
                .messageContent(request.getMessageContent())
                .direction(GuestMessage.MessageDirection.INBOUND)
                .messageType(GuestMessage.MessageType.TEXT)
                .status(GuestMessage.MessageStatus.DELIVERED)
                .isRead(false)
                .build();

            GuestMessage savedMessage = messageService.saveMessage(message);

            logger.info("Guest message received from {} in event {}. Message ID: {}",
                guest.getId(), event.getId(), savedMessage.getId());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "messageId", savedMessage.getId(),
                "message", "Message sent to hosts successfully"
            ));

        } catch (Exception e) {
            logger.error("Error sending message from guest to host", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", "Failed to send message: " + e.getMessage())
            );
        }
    }

    /**
     * Get messages for guest in specific event
     * GET /api/messages/event/{eventId}/guest
     */
    @GetMapping("/event/{eventId}/guest")
    @PreAuthorize("hasRole('GUEST')")
    public ResponseEntity<?> getGuestMessages(
            @PathVariable Long eventId,
            @RequestParam(required = false) Long guestId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            Guest guest = null;

            // 1) Prefer explicit guestId when provided
            if (guestId != null) {
                guest = guestRepository.findById(guestId).orElse(null);
            }

            // 2) Fallback to authenticated principal: try phone first, then email
            if (guest == null) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String principal = auth != null ? auth.getName() : null;
                if (principal != null) {
                    guest = guestRepository.findByContactPhone(principal);
                    if (guest == null) {
                        guest = guestRepository.findByContactEmail(principal);
                    }
                }
            }

            if (guest == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Guest not found")
                );
            }

            // Verify guest is invited to this event
            if (guest.getEventId() == null || !guest.getEventId().equals(eventId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Access denied")
                );
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<GuestMessage> messages = messageService.getGuestMessages(eventId, guest.getId(), pageable);

            // Mark messages as read when guest views them
            messageService.markGuestMessagesAsRead(eventId, guest.getId());

            // Convert to DTOs to avoid circular reference nesting depth error
            Page<GuestMessageDTO> messageDTOs = messages.map(GuestMessageDTO::fromEntity);

            // Return custom paged response to avoid Spring Data Page serialization issues
            PagedMessageResponse response = PagedMessageResponse.fromPage(messageDTOs);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching guest messages", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", "Failed to fetch messages")
            );
        }
    }

    /**
     * Get unread messages for guest in specific event
     * GET /api/messages/event/{eventId}/unread
     */
    @GetMapping("/event/{eventId}/unread")
    @PreAuthorize("hasRole('GUEST')")
    public ResponseEntity<?> getUnreadCount(@PathVariable Long eventId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String guestIdentifier = auth.getName();

            Guest guest = guestRepository.findByContactEmail(guestIdentifier);
            if (guest == null) {
                guest = guestRepository.findByContactPhone(guestIdentifier);
            }

            if (guest == null || !guest.getEventId().equals(eventId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Access denied")
                );
            }

            long unreadCount = messageService.getGuestUnreadCount(eventId, guest.getId());
            return ResponseEntity.ok(Map.of("unreadCount", unreadCount));

        } catch (Exception e) {
            logger.error("Error fetching unread count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", "Failed to fetch unread count")
            );
        }
    }

    /**
     * DTO for send message request
     */
    public static class SendMessageRequest {
        private Long eventId;
        private Long guestId;
        private String messageContent;

        // Getters and setters
        public Long getEventId() {
            return eventId;
        }

        public void setEventId(Long eventId) {
            this.eventId = eventId;
        }

        public Long getGuestId() {
            return guestId;
        }

        public void setGuestId(Long guestId) {
            this.guestId = guestId;
        }

        public String getMessageContent() {
            return messageContent;
        }

        public void setMessageContent(String messageContent) {
            this.messageContent = messageContent;
        }
    }
}

