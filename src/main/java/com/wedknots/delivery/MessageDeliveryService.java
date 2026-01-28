package com.wedknots.delivery;

import com.wedknots.delivery.provider.*;
import com.wedknots.model.Guest;
import com.wedknots.model.GuestMessage;
import com.wedknots.model.MessageDeliveryQueue;
import com.wedknots.repository.MessageDeliveryQueueRepository;
import com.wedknots.template.TemplateVariableProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Main Message Delivery Service ("The Delivery Man")
 * Orchestrates message delivery through various channels
 * Supports both synchronous and asynchronous (queued) delivery
 */
@Service
@Transactional
public class MessageDeliveryService {
    private static final Logger logger = LoggerFactory.getLogger(MessageDeliveryService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Autowired
    private EmailDeliveryProvider emailProvider;

    @Autowired
    private SMSDeliveryProvider smsProvider;

    @Autowired
    private InternalMessageDeliveryProvider internalMessageProvider;

    @Autowired(required = false)
    private WhatsAppAdbProvider whatsAppAdbProvider;

    @Autowired
    private DeliveryConfig deliveryConfig;

    @Autowired
    private MessageDeliveryQueueRepository queueRepository;

    @Autowired
    private TemplateVariableProcessor templateProcessor;

    /**
     * Send a message/invitation synchronously (immediate delivery)
     * If email is not available, automatically falls back to SMS
     * Finally, records in internal messaging system
     */
    public DeliveryResult send(DeliveryRequest request) {
        String recipientName = request.getRecipient().getContactFirstName() != null ? request.getRecipient().getContactFirstName() : "" +
                              " " + (request.getRecipient().getContactLastName() != null ? request.getRecipient().getContactLastName() : "");
        logger.info("Processing delivery request - ID: {}, Type: {}, Mode: {}, Recipient: {}",
            request.getMessageId(), request.getMessageType(),
            request.getPreferredMode() != null ? request.getPreferredMode().getCode() : "AUTO",
            recipientName.trim());

        // Process template variables
        request = processTemplateVariables(request);

        // Determine delivery mode
        DeliveryMode deliveryMode = determineDeliveryMode(request);
        logger.debug("Determined delivery mode: {}", deliveryMode.getCode());

        // Attempt delivery through appropriate provider
        DeliveryResult result = deliverThroughProvider(request, deliveryMode);

        // Always record in internal messaging system for UI
        recordInternalMessage(request, deliveryMode, result);

        return result;
    }

    /**
     * Queue a message for asynchronous delivery
     * Returns immediately with queue ID, delivery happens in background
     */
    public String queueMessage(DeliveryRequest request) {
        return queueMessage(request, null, null);
    }

    /**
     * Queue a message for asynchronous delivery with options
     * @param request The delivery request
     * @param scheduledAt Optional scheduled delivery time
     * @param priority Optional priority (1-10, higher = more important)
     */
    public String queueMessage(DeliveryRequest request, LocalDateTime scheduledAt, Integer priority) {
        try {
            // Process template variables before queueing
            request = processTemplateVariables(request);

            MessageDeliveryQueue queueItem = new MessageDeliveryQueue();

            // Generate message ID if not provided
            String messageId = request.getMessageId();
            if (messageId == null || messageId.isEmpty()) {
                messageId = UUID.randomUUID().toString();
            }

            queueItem.setMessageId(messageId);
            queueItem.setMessageType(request.getMessageType());
            queueItem.setGuest(request.getRecipient());
            queueItem.setEvent(request.getEvent());
            queueItem.setTitle(request.getTitle());  // Already processed
            queueItem.setContent(request.getContent());  // Already processed

            if (request.getPreferredMode() != null) {
                queueItem.setPreferredMode(request.getPreferredMode().getCode());
            }

            if (scheduledAt != null) {
                queueItem.setScheduledAt(scheduledAt);
            }

            if (priority != null) {
                queueItem.setPriority(priority);
            }

            queueRepository.save(queueItem);

            logger.info("✅ Message queued for async delivery - ID: {}, Queue ID: {}",
                messageId, queueItem.getId());

            return messageId;

        } catch (Exception e) {
            logger.error("Failed to queue message", e);
            throw new RuntimeException("Failed to queue message: " + e.getMessage(), e);
        }
    }

    /**
     * Queue multiple messages for asynchronous delivery
     */
    public List<String> queueMessages(List<DeliveryRequest> requests) {
        return queueMessages(requests, null, null);
    }

    /**
     * Queue multiple messages with options
     */
    public List<String> queueMessages(List<DeliveryRequest> requests, LocalDateTime scheduledAt, Integer priority) {
        List<String> messageIds = new ArrayList<>();

        for (DeliveryRequest request : requests) {
            try {
                String messageId = queueMessage(request, scheduledAt, priority);
                messageIds.add(messageId);
            } catch (Exception e) {
                String recipientName = (request.getRecipient().getContactFirstName() != null ? request.getRecipient().getContactFirstName() : "") +
                                      " " + (request.getRecipient().getContactLastName() != null ? request.getRecipient().getContactLastName() : "");
                logger.error("Error queueing message for {}", recipientName.trim(), e);
                messageIds.add(null); // Indicate failure
            }
        }

        logger.info("Queued {}/{} messages successfully",
            messageIds.stream().filter(id -> id != null).count(), requests.size());

        return messageIds;
    }

    /**
     * Get delivery status for a queued message
     */
    public Optional<MessageDeliveryQueue> getDeliveryStatus(String messageId) {
        return queueRepository.findByMessageId(messageId);
    }

    /**
     * Get all queued messages for a guest
     */
    public List<MessageDeliveryQueue> getGuestQueuedMessages(Long guestId) {
        return queueRepository.findByGuestIdOrderByCreatedAtDesc(guestId);
    }

    /**
     * Get all queued messages for an event
     */
    public List<MessageDeliveryQueue> getEventQueuedMessages(Long eventId) {
        return queueRepository.findByEventIdOrderByCreatedAtDesc(eventId);
    }

    /**
     * Get queue statistics
     */
    public QueueStatistics getQueueStatistics() {
        QueueStatistics stats = new QueueStatistics();
        stats.setPendingCount(queueRepository.countByStatus("PENDING"));
        stats.setProcessingCount(queueRepository.countByStatus("PROCESSING"));
        stats.setDeliveredCount(queueRepository.countByStatus("DELIVERED"));
        stats.setFailedCount(queueRepository.countByStatus("FAILED"));
        stats.setRetryCount(queueRepository.countByStatus("RETRY"));
        return stats;
    }

    /**
     * Send batch messages to multiple guests
     */
    public List<DeliveryResult> sendBatch(List<DeliveryRequest> requests) {
        List<DeliveryResult> results = new ArrayList<>();
        for (DeliveryRequest request : requests) {
            try {
                DeliveryResult result = send(request);
                results.add(result);
            } catch (Exception e) {
                String recipientName = (request.getRecipient().getContactFirstName() != null ? request.getRecipient().getContactFirstName() : "") +
                                      " " + (request.getRecipient().getContactLastName() != null ? request.getRecipient().getContactLastName() : "");
                logger.error("Error sending batch message to {}", recipientName.trim(), e);
                DeliveryResult errorResult = new DeliveryResult(false, DeliveryMode.EMAIL, e.getMessage());
                errorResult.setDeliveryId(request.getMessageId());
                results.add(errorResult);
            }
        }
        return results;
    }

    /**
     * Determine the appropriate delivery mode for this request
     * Uses preferred mode if specified, otherwise auto-selects based on guest details
     */
    private DeliveryMode determineDeliveryMode(DeliveryRequest request) {
        // If preferred mode is specified and guest has data for it, use it
        if (request.getPreferredMode() != null) {
            if (canDeliverVia(request, request.getPreferredMode())) {
                return request.getPreferredMode();
            }
            logger.warn("Cannot deliver via preferred mode {}, attempting fallback",
                request.getPreferredMode().getCode());
        }

        // Auto-select: Try email first, fallback to SMS, finally internal
        if (emailProvider.canDeliver(request)) {
            return DeliveryMode.EMAIL;
        }

        if (smsProvider.canDeliver(request)) {
            String recipientName = (request.getRecipient().getContactFirstName() != null ? request.getRecipient().getContactFirstName() : "") +
                                  " " + (request.getRecipient().getContactLastName() != null ? request.getRecipient().getContactLastName() : "");
            logger.info("Email not available for {}, falling back to SMS",
                recipientName.trim());
            return DeliveryMode.SMS;
        }

        // Default to internal messaging
        String recipientName2 = (request.getRecipient().getContactFirstName() != null ? request.getRecipient().getContactFirstName() : "") +
                               " " + (request.getRecipient().getContactLastName() != null ? request.getRecipient().getContactLastName() : "");
        logger.info("No external contact info for {}, using internal messaging",
            recipientName2.trim());
        return DeliveryMode.INTERNAL_MESSAGE;
    }

    /**
     * Check if a delivery mode is possible for this request
     */
    private boolean canDeliverVia(DeliveryRequest request, DeliveryMode mode) {
        switch (mode) {
            case EMAIL:
                return emailProvider.canDeliver(request);
            case SMS:
                return smsProvider.canDeliver(request);
            case WHATSAPP_ADB:
                return whatsAppAdbProvider != null && whatsAppAdbProvider.canDeliver(request);
            case INTERNAL_MESSAGE:
                return true;
            case WHATSAPP_PERSONAL:
            case EXTERNAL:
            default:
                return false;
        }
    }

    /**
     * Deliver message through the appropriate provider
     */
    private DeliveryResult deliverThroughProvider(DeliveryRequest request, DeliveryMode deliveryMode) {
        switch (deliveryMode) {
            case SMS:
                // Fan-out to all phone numbers
                return deliverToAllPhones(request, DeliveryMode.SMS, smsProvider);
            case WHATSAPP_ADB:
                return deliverToAllPhones(request, DeliveryMode.WHATSAPP_ADB, whatsAppAdbProvider);
            default:
                return routeSingle(request, deliveryMode);
        }
    }

    private DeliveryResult deliverToAllPhones(DeliveryRequest request, DeliveryMode mode, MessageDeliveryProvider provider) {
        if (provider == null || !provider.isConfigured()) {
            logger.warn("Provider {} not configured, recording for manual delivery", mode.getCode());
            return new DeliveryResult(true, mode, mode.getCode() + "_RECORDED", "Provider not configured");
        }
        if (request.getRecipient() == null || request.getRecipient().getPhoneNumbers() == null || request.getRecipient().getPhoneNumbers().isEmpty()) {
            logger.warn("No phone numbers for recipient, cannot deliver via {}", mode.getCode());
            return new DeliveryResult(false, mode, "No phone numbers");
        }
        DeliveryResult lastResult = null;
        for (var phone : request.getRecipient().getPhoneNumbers()) {
            DeliveryRequest perPhone = DeliveryRequest.builder()
                    .messageId(request.getMessageId())
                    .messageType(request.getMessageType())
                    .title(request.getTitle())
                    .content(request.getContent())
                    .recipient(request.getRecipient())
                    .event(request.getEvent())
                    .preferredMode(request.getPreferredMode())
                    .overridePhoneNumber(phone.getPhoneNumber())
                    .overrideContactName(buildContactName(phone))
                    .build();
            lastResult = provider.deliver(perPhone);
        }
        return lastResult != null ? lastResult : new DeliveryResult(false, mode, "No phones processed");
    }

    private DeliveryResult routeSingle(DeliveryRequest request, DeliveryMode deliveryMode) {
        switch (deliveryMode) {
            case EMAIL:
                return emailProvider.deliver(request);
            case SMS:
                return smsProvider.deliver(request);
            case WHATSAPP_ADB:
                return whatsAppAdbProvider != null ? whatsAppAdbProvider.deliver(request)
                        : new DeliveryResult(false, deliveryMode, "WhatsApp ADB provider not available");
            default:
                return internalMessageProvider.deliver(request);
        }
    }

    private String buildContactName(com.wedknots.model.GuestPhoneNumber phone) {
        String fn = phone.getContactFirstName();
        String ln = phone.getContactLastName();
        if (fn == null && ln == null) return null;
        if (fn == null) return ln;
        if (ln == null) return fn;
        return fn + " " + ln;
    }

    /**
     * Record delivery in internal messaging system for UI
     * This ensures all messages are visible in the messaging interface
     */
    private void recordInternalMessage(DeliveryRequest request, DeliveryMode deliveryMode, DeliveryResult result) {
        try {
            // TODO: Call MessageService to record in GuestMessage table
            // This allows the message to be displayed in the messaging UI
            logger.debug("Recording message in internal system - Mode: {}, Status: {}",
                deliveryMode.getCode(), result.getStatus());
        } catch (Exception e) {
            logger.error("Error recording internal message", e);
        }
    }

    /**
     * Process template variables in request
     * Replaces {{variable}} placeholders with actual values
     */
    private DeliveryRequest processTemplateVariables(DeliveryRequest request) {
        try {
            String processedTitle = templateProcessor.process(
                request.getTitle(),
                request.getEvent(),
                request.getRecipient()
            );

            String processedContent = templateProcessor.process(
                request.getContent(),
                request.getEvent(),
                request.getRecipient()
            );

            logger.debug("Template variables processed for message {}", request.getMessageId());

            // Create new request with processed content
            return DeliveryRequest.builder()
                .messageId(request.getMessageId())
                .messageType(request.getMessageType())
                .title(processedTitle)
                .content(processedContent)
                .recipient(request.getRecipient())
                .event(request.getEvent())
                .preferredMode(request.getPreferredMode())
                .build();

        } catch (Exception e) {
            logger.warn("Error processing template variables, using original content", e);
            return request;
        }
    }

    /**
     * Get delivery configuration
     */
    public DeliveryConfig getConfig() {
        return deliveryConfig;
    }

    /**
     * Check health of delivery providers
     */
    public DeliveryHealthStatus getHealth() {
        DeliveryHealthStatus status = new DeliveryHealthStatus();
        status.setEmailConfigured(emailProvider.isConfigured());
        status.setSmsConfigured(smsProvider.isConfigured());
        status.setInternalMessageConfigured(internalMessageProvider.isConfigured());
        status.setTimestamp(LocalDateTime.now().format(TIMESTAMP_FORMAT));
        return status;
    }

    /**
     * Health status DTO
     */
    public static class DeliveryHealthStatus {
        private boolean emailConfigured;
        private boolean smsConfigured;
        private boolean internalMessageConfigured;
        private String timestamp;

        // Getters and Setters
        public boolean isEmailConfigured() {
            return emailConfigured;
        }

        public void setEmailConfigured(boolean emailConfigured) {
            this.emailConfigured = emailConfigured;
        }

        public boolean isSmsConfigured() {
            return smsConfigured;
        }

        public void setSmsConfigured(boolean smsConfigured) {
            this.smsConfigured = smsConfigured;
        }

        public boolean isInternalMessageConfigured() {
            return internalMessageConfigured;
        }

        public void setInternalMessageConfigured(boolean internalMessageConfigured) {
            this.internalMessageConfigured = internalMessageConfigured;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }

    /**
     * Queue statistics DTO
     */
    public static class QueueStatistics {
        private long pendingCount;
        private long processingCount;
        private long deliveredCount;
        private long failedCount;
        private long retryCount;

        public long getPendingCount() {
            return pendingCount;
        }

        public void setPendingCount(long pendingCount) {
            this.pendingCount = pendingCount;
        }

        public long getProcessingCount() {
            return processingCount;
        }

        public void setProcessingCount(long processingCount) {
            this.processingCount = processingCount;
        }

        public long getDeliveredCount() {
            return deliveredCount;
        }

        public void setDeliveredCount(long deliveredCount) {
            this.deliveredCount = deliveredCount;
        }

        public long getFailedCount() {
            return failedCount;
        }

        public void setFailedCount(long failedCount) {
            this.failedCount = failedCount;
        }

        public long getRetryCount() {
            return retryCount;
        }

        public void setRetryCount(long retryCount) {
            this.retryCount = retryCount;
        }

        public long getTotalCount() {
            return pendingCount + processingCount + deliveredCount + failedCount + retryCount;
        }
    }
}
