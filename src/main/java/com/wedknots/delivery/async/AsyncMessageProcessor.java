package com.wedknots.delivery.async;

import com.wedknots.delivery.*;
import com.wedknots.delivery.provider.*;
import com.wedknots.model.MessageDeliveryQueue;
import com.wedknots.repository.MessageDeliveryQueueRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Asynchronous message delivery processor
 * Processes messages from the queue and handles retries
 */
@Service
@Slf4j
public class AsyncMessageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(AsyncMessageProcessor.class);
    private static final int BATCH_SIZE = 10;
    private static final int STUCK_MESSAGE_TIMEOUT_MINUTES = 30;

    @Autowired
    private MessageDeliveryQueueRepository queueRepository;

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

    /**
     * Process pending messages from the queue
     * Runs every 10 seconds
     */
    @Scheduled(fixedDelay = 10000, initialDelay = 5000)
    @Transactional
    public void processPendingMessages() {
        try {
            List<MessageDeliveryQueue> pendingMessages = queueRepository.findPendingMessages(LocalDateTime.now());

            if (!pendingMessages.isEmpty()) {
                logger.info("Found {} pending messages to process", pendingMessages.size());

                int processed = 0;
                for (MessageDeliveryQueue queuedMessage : pendingMessages) {
                    if (processed >= BATCH_SIZE) {
                        logger.info("Batch limit reached ({}), will process remaining in next cycle", BATCH_SIZE);
                        break;
                    }

                    processMessageAsync(queuedMessage.getId());
                    processed++;
                }
            }
        } catch (Exception e) {
            logger.error("Error processing pending messages", e);
        }
    }

    /**
     * Process messages that need retry
     * Runs every 30 seconds
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 15000)
    @Transactional
    public void processRetryMessages() {
        try {
            List<MessageDeliveryQueue> retryMessages = queueRepository.findMessagesForRetry(LocalDateTime.now());

            if (!retryMessages.isEmpty()) {
                logger.info("Found {} messages for retry", retryMessages.size());

                for (MessageDeliveryQueue queuedMessage : retryMessages) {
                    processMessageAsync(queuedMessage.getId());
                }
            }
        } catch (Exception e) {
            logger.error("Error processing retry messages", e);
        }
    }

    /**
     * Recover stuck messages
     * Runs every 5 minutes
     */
    @Scheduled(fixedDelay = 300000, initialDelay = 60000)
    @Transactional
    public void recoverStuckMessages() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(STUCK_MESSAGE_TIMEOUT_MINUTES);
            List<MessageDeliveryQueue> stuckMessages = queueRepository.findStuckMessages(cutoffTime);

            if (!stuckMessages.isEmpty()) {
                logger.warn("Found {} stuck messages, resetting to PENDING", stuckMessages.size());

                for (MessageDeliveryQueue message : stuckMessages) {
                    message.setStatus("PENDING");
                    message.setProcessingStartedAt(null);
                    queueRepository.save(message);
                }
            }
        } catch (Exception e) {
            logger.error("Error recovering stuck messages", e);
        }
    }

    /**
     * Process a single message asynchronously
     */
    @Async("messageDeliveryExecutor")
    @Transactional
    public void processMessageAsync(Long queueId) {
        MessageDeliveryQueue queuedMessage = queueRepository.findById(queueId).orElse(null);

        if (queuedMessage == null) {
            logger.warn("Queue message {} not found", queueId);
            return;
        }

        try {
            logger.info("Processing message {} (ID: {}, Attempt: {}/{})",
                    queuedMessage.getMessageId(), queuedMessage.getId(),
                    queuedMessage.getRetryCount() + 1, queuedMessage.getMaxRetries());

            // Mark as processing
            queuedMessage.setStatus("PROCESSING");
            queuedMessage.setProcessingStartedAt(LocalDateTime.now());
            queueRepository.save(queuedMessage);

            // Build delivery request
            DeliveryRequest request = buildDeliveryRequest(queuedMessage);

            // Determine delivery mode
            DeliveryMode deliveryMode = determineDeliveryMode(request);
            queuedMessage.setDeliveryMode(deliveryMode.getCode());

            // Attempt delivery
            DeliveryResult result = deliverThroughProvider(request, deliveryMode);

            // Update queue status based on result
            if (result.isSuccess()) {
                queuedMessage.setStatus("DELIVERED");
                queuedMessage.setDeliveryStatus(result.getStatus());
                queuedMessage.setProcessedAt(LocalDateTime.now());
                logger.info("✅ Message {} delivered successfully via {}",
                        queuedMessage.getMessageId(), deliveryMode.getCode());
            } else {
                handleDeliveryFailure(queuedMessage, result);
            }

            queueRepository.save(queuedMessage);

        } catch (Exception e) {
            logger.error("Error processing message {}", queuedMessage.getMessageId(), e);
            handleProcessingException(queuedMessage, e);
        }
    }

    /**
     * Build DeliveryRequest from queued message
     */
    private DeliveryRequest buildDeliveryRequest(MessageDeliveryQueue queuedMessage) {
        DeliveryRequest.Builder builder = DeliveryRequest.builder()
                .messageId(queuedMessage.getMessageId())
                .messageType(queuedMessage.getMessageType())
                .title(queuedMessage.getTitle())
                .content(queuedMessage.getContent())
                .recipient(queuedMessage.getGuest())
                .event(queuedMessage.getEvent());

        if (queuedMessage.getPreferredMode() != null) {
            try {
                builder.preferredMode(DeliveryMode.fromCode(queuedMessage.getPreferredMode()));
            } catch (Exception e) {
                logger.warn("Invalid preferred mode: {}", queuedMessage.getPreferredMode());
            }
        }

        return builder.build();
    }

    /**
     * Determine delivery mode for the request
     */
    private DeliveryMode determineDeliveryMode(DeliveryRequest request) {
        DeliveryMode deliveryMode = DeliveryMode.EXTERNAL;
        if (request.getPreferredMode() != null && canDeliverVia(request, request.getPreferredMode())) {
            log.info("Using preferred delivery mode {} for message {}",
                    request.getPreferredMode().getCode(), request.getMessageId());
            deliveryMode = request.getPreferredMode();
        }
        if (whatsAppAdbProvider.canDeliver(request)) {
            log.info("Using whatasppAdb delivery mode {} for message {}",
                    request.getPreferredMode().getCode(), request.getMessageId());

            deliveryMode = DeliveryMode.WHATSAPP_ADB;
        } else if (smsProvider.canDeliver(request)) {
            log.info("Using sms mode {} for message {}",
                    request.getPreferredMode().getCode(), request.getMessageId());

            deliveryMode = DeliveryMode.SMS;
        } else if (emailProvider.canDeliver(request)) {
            log.info("Using email mode {} for message {}",
                    request.getPreferredMode().getCode(), request.getMessageId());

            deliveryMode = DeliveryMode.EMAIL;
        } else if (internalMessageProvider.canDeliver(request)) {
            log.info("Using internal mode {} for message {}",
                    request.getPreferredMode().getCode(), request.getMessageId());
            deliveryMode = DeliveryMode.INTERNAL_MESSAGE;
        }

        log.info("Determined delivery mode for message {} to be {}", request.getMessageId(), deliveryMode);
        return deliveryMode;
    }

    /**
     * Check if delivery via specific mode is possible
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
            default:
                return false;
        }
    }

    /**
     * Deliver message through provider
     */
    private DeliveryResult deliverThroughProvider(DeliveryRequest request, DeliveryMode mode) {
        try {
            switch (mode) {
                case EMAIL:
                    return emailProvider.deliver(request);
                case SMS:
                    return smsProvider.deliver(request);
                case WHATSAPP_ADB:
                    if (whatsAppAdbProvider != null) {
                        return whatsAppAdbProvider.deliver(request);
                    }
                    return new DeliveryResult(false, mode, "WhatsApp ADB provider not available");
                case INTERNAL_MESSAGE:
                    return internalMessageProvider.deliver(request);
                default:
                    return new DeliveryResult(false, mode, "No provider for mode: " + mode.getCode());
            }
        } catch (Exception e) {
            logger.error("Error delivering via {}", mode.getCode(), e);
            return new DeliveryResult(false, mode, e.getMessage());
        }
    }

    /**
     * Handle delivery failure with retry logic
     */
    private void handleDeliveryFailure(MessageDeliveryQueue queuedMessage, DeliveryResult result) {
        queuedMessage.setErrorMessage(result.getErrorMessage());
        queuedMessage.setRetryCount(queuedMessage.getRetryCount() + 1);

        if (queuedMessage.getRetryCount() >= queuedMessage.getMaxRetries()) {
            queuedMessage.setStatus("FAILED");
            queuedMessage.setProcessedAt(LocalDateTime.now());
            logger.error("❌ Message {} failed after {} attempts: {}",
                    queuedMessage.getMessageId(), queuedMessage.getRetryCount(), result.getErrorMessage());
        } else {
            queuedMessage.setStatus("RETRY");
            // Exponential backoff: 1min, 5min, 15min
            int delayMinutes = (int) Math.pow(5, queuedMessage.getRetryCount());
            queuedMessage.setNextRetryAt(LocalDateTime.now().plusMinutes(delayMinutes));
            logger.warn("⚠️ Message {} failed, will retry in {} minutes (attempt {}/{})",
                    queuedMessage.getMessageId(), delayMinutes,
                    queuedMessage.getRetryCount() + 1, queuedMessage.getMaxRetries());
        }
    }

    /**
     * Handle processing exception
     */
    private void handleProcessingException(MessageDeliveryQueue queuedMessage, Exception e) {
        queuedMessage.setErrorMessage(e.getMessage());
        queuedMessage.setRetryCount(queuedMessage.getRetryCount() + 1);

        if (queuedMessage.getRetryCount() >= queuedMessage.getMaxRetries()) {
            queuedMessage.setStatus("FAILED");
            queuedMessage.setProcessedAt(LocalDateTime.now());
        } else {
            queuedMessage.setStatus("RETRY");
            int delayMinutes = (int) Math.pow(5, queuedMessage.getRetryCount());
            queuedMessage.setNextRetryAt(LocalDateTime.now().plusMinutes(delayMinutes));
        }

        queueRepository.save(queuedMessage);
    }
}

