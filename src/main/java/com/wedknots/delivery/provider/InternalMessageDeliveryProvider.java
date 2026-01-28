package com.wedknots.delivery.provider;

import com.wedknots.delivery.DeliveryMode;
import com.wedknots.delivery.DeliveryRequest;
import com.wedknots.delivery.DeliveryResult;
import com.wedknots.delivery.DeliveryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Internal message delivery provider
 * Handles delivery of messages via internal messaging system
 */
@Component
public class InternalMessageDeliveryProvider implements MessageDeliveryProvider {
    private static final Logger logger = LoggerFactory.getLogger(InternalMessageDeliveryProvider.class);

    @Autowired
    private DeliveryConfig deliveryConfig;

    @Override
    public boolean canDeliver(DeliveryRequest request) {
        // Internal messages can always be delivered
        return true;
    }

    @Override
    public DeliveryResult deliver(DeliveryRequest request) {
        try {
            // Internal messages are stored in the database by default
            // This provider confirms they're ready for the messaging UI
            String recipientName = (request.getRecipient().getContactFirstName() != null ? request.getRecipient().getContactFirstName() : "") +
                                  " " + (request.getRecipient().getContactLastName() != null ? request.getRecipient().getContactLastName() : "");
            logger.info("Internal message recorded for delivery: Message ID: {}, Recipient: {}",
                request.getMessageId(), recipientName.trim());

            DeliveryResult result = new DeliveryResult();
            result.setSuccess(true);
            result.setDeliveryMode(DeliveryMode.INTERNAL_MESSAGE);
            result.setStatus("STORED");
            result.setDeliveryId(request.getMessageId());
            return result;

        } catch (Exception e) {
            logger.error("Failed to store internal message", e);
            return new DeliveryResult(false, DeliveryMode.INTERNAL_MESSAGE, e.getMessage());
        }
    }

    @Override
    public String getProviderName() {
        return "Internal Message";
    }

    @Override
    public boolean isConfigured() {
        // Internal messaging is always available
        return true;
    }
}

