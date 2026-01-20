package com.wedknots.controller;

import com.wedknots.controller.dto.WhatsAppWebhookPayload;
import com.wedknots.model.GuestMessage;
import com.wedknots.model.WeddingEvent;
import com.wedknots.repository.WeddingEventRepository;
import com.wedknots.service.MessageService;
import com.wedknots.service.WhatsAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for handling WhatsApp Cloud API webhook callbacks
 * Receives incoming messages, status updates, and other notifications from Meta
 */
@RestController
@RequestMapping("/api/whatsapp/webhook")
public class WhatsAppWebhookController {
    private static final Logger logger = LoggerFactory.getLogger(WhatsAppWebhookController.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private WhatsAppService whatsAppService;

    @Autowired
    private WeddingEventRepository weddingEventRepository;

    @Value("${whatsapp.webhook.verify-token:wed-knots-verify-token}")
    private String webhookVerifyToken;

    @Value("${whatsapp.webhook.app-secret:}")
    private String webhookAppSecret;

    /**
     * GET endpoint for webhook verification from Meta
     * Meta sends a verification request when setting up the webhook
     */
    @GetMapping("")
    public ResponseEntity<?> verifyWebhook(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.challenge", required = false) String challenge,
            @RequestParam(name = "hub.verify_token", required = false) String verifyToken) {

        logger.warn("Webhook verification request received. Mode: {}", mode);

        if (!"subscribe".equals(mode)) {
            logger.error("Invalid webhook verification mode: {}", mode);
            return ResponseEntity.badRequest().build();
        }

        if (!verifyToken.equals(webhookVerifyToken)) {
            logger.error("Webhook verification token mismatch");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        logger.info("Webhook verified successfully");
        return ResponseEntity.ok(challenge);
    }

    /**
     * POST endpoint for receiving messages, status updates, and other events
     * Validates webhook signature before processing
     */
    @PostMapping("")
    public ResponseEntity<?> handleWebhookEvent(
            @RequestBody String payload,
            @RequestHeader(name = "X-Hub-Signature-256", required = false) String signature) {
        logger.info("Received webhook event {}", payload);
        try {
            // Validate webhook signature if app secret is configured
            // Skip validation if app-secret is empty (development mode)
            if (webhookAppSecret != null && !webhookAppSecret.isEmpty()) {
                if (!validateWebhookSignature(payload, signature)) {
                    logger.error("Webhook signature validation failed");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        Map.of("error", "Invalid signature")
                    );
                }
            } else {
                logger.warn("Webhook signature validation SKIPPED - app-secret not configured (development mode)");
            }

            logger.info("Processing webhook payload {}", payload);

            // Parse the webhook payload
            WhatsAppWebhookPayload webhookPayload = parseWebhookPayload(payload);

            if (webhookPayload == null || webhookPayload.getEntry() == null) {
                logger.warn("Invalid webhook payload structure");
                return ResponseEntity.ok(Map.of("success", true));
            }

            // Process each entry in the webhook
            for (WhatsAppWebhookPayload.Entry entry : webhookPayload.getEntry()) {
                if (entry.getChanges() == null) continue;

                for (WhatsAppWebhookPayload.Change change : entry.getChanges()) {
                    if ("messages".equals(change.getField())) {
                        processMessageChanges(change.getValue());
                    } else if ("message_template_status_update".equals(change.getField())) {
                        processTemplateStatusUpdate(change.getValue());
                    } else if ("message_status".equals(change.getField())) {
                        processMessageStatusUpdate(change.getValue());
                    } else {
                        logger.debug("Received webhook for field: {}", change.getField());
                    }
                }
            }

            return ResponseEntity.ok(Map.of("success", true));

        } catch (Exception e) {
            logger.error("Error processing webhook event", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", "Failed to process webhook")
            );
        }
    }

    /**
     * Process incoming messages from the webhook
     * Supports multiple wedding events with the same WhatsApp phone number ID
     * Messages are cloned and associated with all matching events
     */
    private void processMessageChanges(WhatsAppWebhookPayload.Value value) {
        if (value == null || value.getMessages() == null) {
            return;
        }

        // Extract phone number ID from metadata
        String phoneNumberId = value.getMetadata() != null ?
            value.getMetadata().getPhoneNumberId() : null;

        if (phoneNumberId == null) {
            logger.warn("Received message without phone number ID in metadata");
            return;
        }

        // Find ALL events associated with this phone number ID
        List<WeddingEvent> events = weddingEventRepository.findAllByWhatsappPhoneNumberId(phoneNumberId);

        if (events.isEmpty()) {
            logger.warn("Received message for WhatsApp phone number ID: {} - No associated event found. " +
                       "Configure this phone number ID for a wedding event.", phoneNumberId);
            return;
        }

        logger.info("Found {} event(s) associated with WhatsApp phone number ID: {}", events.size(), phoneNumberId);

        // Process messages for each event
        for (WeddingEvent event : events) {
            try {
                logger.info("Processing messages for event '{}' (ID: {}) with phone number ID: {}",
                    event.getName(), event.getId(), phoneNumberId);

                processMessagesForEvent(event, value);

            } catch (Exception e) {
                logger.error("Error processing messages for event ID {}: {}", event.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Process incoming messages for a specific event
     * Clones the message payload and associates it with the event
     */
    private void processMessagesForEvent(WeddingEvent event, WhatsAppWebhookPayload.Value value) {
        if (value == null || value.getMessages() == null) {
            return;
        }

        for (WhatsAppWebhookPayload.Message message : value.getMessages()) {
            try {
                logger.info("Processing incoming message from: {} with ID: {} for event: {}",
                    message.getFrom(), message.getId(), event.getName());

                String phoneNumber = message.getFrom();
                String messageContent = null;
                GuestMessage.MessageType messageType = GuestMessage.MessageType.TEXT;
                String mediaUrl = null;

                // Extract message content based on message type
                if ("text".equals(message.getType()) && message.getText() != null) {
                    messageContent = message.getText().getBody();
                } else if ("image".equals(message.getType()) && message.getImage() != null) {
                    messageType = GuestMessage.MessageType.IMAGE;
                    mediaUrl = message.getImage().getId();
                    messageContent = "[Image received]";
                } else if ("document".equals(message.getType()) && message.getDocument() != null) {
                    messageType = GuestMessage.MessageType.DOCUMENT;
                    mediaUrl = message.getDocument().getId();
                    messageContent = "[Document: " + message.getDocument().getFilename() + "]";
                } else if ("audio".equals(message.getType()) && message.getAudio() != null) {
                    messageType = GuestMessage.MessageType.AUDIO;
                    mediaUrl = message.getAudio().getId();
                    messageContent = "[Audio message]";
                } else if ("video".equals(message.getType()) && message.getVideo() != null) {
                    messageType = GuestMessage.MessageType.VIDEO;
                    mediaUrl = message.getVideo().getId();
                    messageContent = "[Video message]";
                } else if ("location".equals(message.getType()) && message.getLocation() != null) {
                    messageType = GuestMessage.MessageType.LOCATION;
                    messageContent = "Location: " + message.getLocation().getLatitude() + ", " +
                                   message.getLocation().getLongitude();
                    if (message.getLocation().getName() != null) {
                        messageContent += " (" + message.getLocation().getName() + ")";
                    }
                } else if ("interactive".equals(message.getType()) && message.getInteractive() != null) {
                    messageContent = "[Interactive message]";
                } else {
                    messageType = GuestMessage.MessageType.UNKNOWN;
                    messageContent = "[Unknown message type]";
                }

                // Store the message in the database with event association
                GuestMessage storedMessage = messageService.storeIncomingMessage(
                    event.getId(),
                    phoneNumber,
                    messageContent,
                    message.getId(),
                    messageType,
                    mediaUrl
                );

                logger.info("Message from {} stored successfully for event '{}' (ID: {}). Content: {}",
                    phoneNumber, event.getName(), event.getId(), messageContent);

            } catch (Exception e) {
                logger.error("Error processing message from {} for event ID {}: {}",
                    message.getFrom(), event.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Process message status updates (delivery, read, etc.)
     */
    private void processMessageStatusUpdate(WhatsAppWebhookPayload.Value value) {
        if (value == null || value.getStatuses() == null) {
            return;
        }

        for (WhatsAppWebhookPayload.Status status : value.getStatuses()) {
            try {
                logger.debug("Processing message status update - ID: {}, Status: {}", status.getId(), status.getStatus());

                // Update message status in database
                messageService.updateMessageStatus(status.getId(), status.getStatus());

            } catch (Exception e) {
                logger.error("Error processing message status update", e);
            }
        }
    }

    /**
     * Process message template status updates
     */
    private void processTemplateStatusUpdate(WhatsAppWebhookPayload.Value value) {
        logger.debug("Processing template status update");
        // Template status updates are informational
        // You can log or process these as needed for template management
    }

    /**
     * Validate webhook signature using HMAC-SHA256
     * This ensures the webhook request is actually from Meta/Facebook
     */
    private boolean validateWebhookSignature(String payload, String signature) {
        try {
            if (signature == null || signature.isEmpty()) {
                logger.warn("No signature provided in webhook request");
                return false;
            }

            // Signature format is "sha256=<hash>"
            String[] parts = signature.split("=");
            if (parts.length != 2) {
                logger.error("Invalid signature format");
                return false;
            }

            String providedHash = parts[1];

            // Calculate HMAC-SHA256
            String appSecret = webhookAppSecret;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);

            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String calculatedHash = bytesToHex(hash);

            boolean isValid = calculatedHash.equals(providedHash);

            if (!isValid) {
                logger.error("Webhook signature validation failed. Expected: {}, Got: {}", calculatedHash, providedHash);
            } else {
                logger.debug("Webhook signature validation successful");
            }

            return isValid;

        } catch (Exception e) {
            logger.error("Error validating webhook signature", e);
            return false;
        }
    }

    /**
     * Convert bytes to hexadecimal string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Parse JSON webhook payload
     */
    private WhatsAppWebhookPayload parseWebhookPayload(String payload) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(payload, WhatsAppWebhookPayload.class);
        } catch (Exception e) {
            logger.error("Error parsing webhook payload", e);
            return null;
        }
    }
}

