package com.wedknots.api;

import com.wedknots.dto.GuestMessageDTO;
import com.wedknots.model.GuestMessage;
import com.wedknots.model.WeddingEvent;
import com.wedknots.service.MessageService;
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
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin API for message management
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMessageApiController {
    private static final Logger logger = LoggerFactory.getLogger(AdminMessageApiController.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private WeddingEventRepository weddingEventRepository;

    /**
     * Get all events
     */
    @GetMapping("/events")
    public ResponseEntity<?> getAllEvents() {
        try {
            List<WeddingEvent> events = weddingEventRepository.findAll();
            List<Map<String, Object>> response = events.stream()
                .map(event -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", event.getId());
                    map.put("name", event.getName());
                    map.put("date", event.getDate());
                    return map;
                })
                .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error loading events", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to load events"));
        }
    }

    /**
     * Get all messages with filters
     */
    @GetMapping("/messages")
    public ResponseEntity<?> getAllMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String search) {
        try {
            Pageable pageable = PageRequest.of(page, size);

            // TODO: Implement filtering in MessageService
            // For now, return all messages for the event if specified
            if (eventId != null) {
                Page<GuestMessage> messages = messageService.getEventMessages(eventId, pageable);
                return ResponseEntity.ok(messages);
            }

            // Return empty page if no event specified
            return ResponseEntity.ok(Page.empty(pageable));

        } catch (Exception e) {
            logger.error("Error loading messages", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to load messages"));
        }
    }

    /**
     * Get message details
     */
    @GetMapping("/messages/{messageId}")
    public ResponseEntity<?> getMessageDetails(@PathVariable Long messageId) {
        try {
            GuestMessage message = messageService.getMessage(messageId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            logger.error("Error loading message details", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to load message details"));
        }
    }

    /**
     * Get message statistics
     */
    @GetMapping("/messages/statistics")
    public ResponseEntity<?> getMessageStatistics(@RequestParam(required = false) Long eventId) {
        try {
            Map<String, Object> stats = new HashMap<>();

            if (eventId != null) {
                stats = messageService.getEventMessageStats(eventId);
            } else {
                // TODO: Implement global statistics
                stats.put("total", 0);
                stats.put("sent", 0);
                stats.put("pending", 0);
                stats.put("failed", 0);
            }

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error loading statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to load statistics"));
        }
    }

    /**
     * Retry failed message
     */
    @PostMapping("/messages/{messageId}/retry")
    public ResponseEntity<?> retryMessage(@PathVariable Long messageId) {
        try {
            // TODO: Implement retry logic in service
            logger.info("Retry message requested for ID: {}", messageId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Retry initiated"));
        } catch (Exception e) {
            logger.error("Error retrying message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to retry message"));
        }
    }

    /**
     * Delete message
     */
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long messageId) {
        try {
            messageService.deleteMessage(messageId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Message deleted"));
        } catch (Exception e) {
            logger.error("Error deleting message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to delete message"));
        }
    }

    /**
     * Get all conversations for an event
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
                        conv.put("guestName", guest != null ? 
                            ((guest.getContactFirstName() != null ? guest.getContactFirstName() : "") + " " + 
                             (guest.getContactLastName() != null ? guest.getContactLastName() : "")).trim() : "Unknown");
                        conv.put("guestPhone", guest != null ? guest.getContactPhone() : "");

                        GuestMessage last = messages.get(0); // Already sorted DESC
                        conv.put("lastMessage", last.getMessageContent());
                        conv.put("lastMessageTime", last.getCreatedAt());

                        // Convert messages to DTOs to avoid circular references
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
}

