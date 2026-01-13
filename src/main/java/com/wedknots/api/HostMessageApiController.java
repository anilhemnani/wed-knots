package com.wedknots.api;

import com.wedknots.dto.GuestMessageDTO;
import com.wedknots.model.GuestMessage;
import com.wedknots.model.WeddingEvent;
import com.wedknots.service.MessageService;
import com.wedknots.repository.WeddingEventRepository;
import com.wedknots.repository.GuestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Host API for WhatsApp message management
 */
@RestController
@RequestMapping("/api/host")
@PreAuthorize("hasRole('HOST')")
public class HostMessageApiController {
    private static final Logger logger = LoggerFactory.getLogger(HostMessageApiController.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private WeddingEventRepository weddingEventRepository;

    @Autowired
    private GuestRepository guestRepository;

    /**
     * Get host's events
     */
    @GetMapping("/events")
    public ResponseEntity<?> getHostEvents() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String hostEmail = auth.getName();

            List<WeddingEvent> events = weddingEventRepository.findByHostEmail(hostEmail);
            List<Map<String, Object>> response = events.stream()
                .map(event -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", event.getId());
                    map.put("name", event.getName());
                    map.put("date", event.getDate());
                    map.put("status", event.getStatus());
                    return map;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error loading host events", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to load events"));
        }
    }

    /**
     * Get conversations for an event
     */
    @GetMapping("/messages/event/{eventId}/conversations")
    public ResponseEntity<?> getConversations(@PathVariable Long eventId) {
        try {
            Map<Long, List<GuestMessage>> grouped =
                messageService.getMessagesGroupedByGuest(eventId);

            List<Map<String, Object>> conversations = grouped.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> conv = new HashMap<>();
                    Long guestId = entry.getKey();
                    List<GuestMessage> messages = entry.getValue();

                    // Get guest info from first message
                    if (!messages.isEmpty()) {
                        GuestMessage firstMsg = messages.get(0);
                        com.wedknots.model.Guest guest = firstMsg.getGuest();

                        conv.put("guestId", guestId);
                        conv.put("guestName", guest != null ? guest.getContactName() : "Unknown");
                        conv.put("guestPhone", guest != null ? guest.getContactPhone() : "");

                        GuestMessage last = messages.get(0); // Already sorted DESC
                        conv.put("lastMessage", last.getMessageContent());
                        conv.put("lastMessageTime", last.getCreatedAt());

                        // Convert messages to DTOs to avoid circular references and deep nesting
                        List<GuestMessageDTO> messageDTOs = messages.stream()
                            .map(GuestMessageDTO::fromEntity)
                            .collect(Collectors.toList());
                        conv.put("messages", messageDTOs);
                    }

                    long unread = messages.stream()
                        .filter(m -> m.getDirection() == GuestMessage.MessageDirection.INBOUND && !m.isRead())
                        .count();
                    conv.put("unreadCount", unread);

                    return conv;
                })
                .sorted((a, b) -> {
                    java.time.LocalDateTime timeA = (java.time.LocalDateTime) a.get("lastMessageTime");
                    java.time.LocalDateTime timeB = (java.time.LocalDateTime) b.get("lastMessageTime");
                    if (timeA == null) return 1;
                    if (timeB == null) return -1;
                    return timeB.compareTo(timeA);
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            logger.error("Error loading conversations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to load conversations"));
        }
    }

    /**
     * Get message thread for a guest
     */
    @GetMapping("/messages/event/{eventId}/guest/{guestId}")
    public ResponseEntity<?> getMessageThread(
            @PathVariable Long eventId,
            @PathVariable Long guestId) {
        try {
            List<GuestMessage> messages = messageService.getConversation(eventId, guestId);

            // Reverse to show oldest first
            Collections.reverse(messages);

            // Mark messages as read
            messageService.markGuestMessagesAsRead(eventId, guestId);

            // Get guest details
            com.wedknots.model.Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new RuntimeException("Guest not found"));

            // Convert messages to DTOs to avoid undefined fields
            List<GuestMessageDTO> messageDTOs = messages.stream()
                .map(GuestMessageDTO::fromEntity)
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("guestId", guestId);
            response.put("guestName", guest.getContactName());
            response.put("guestPhone", guest.getContactPhone());
            response.put("messages", messageDTOs);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error loading message thread", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to load message thread"));
        }
    }
}
