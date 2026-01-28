package com.wedknots.delivery.provider;

import com.wedknots.delivery.DeliveryMode;
import com.wedknots.delivery.DeliveryRequest;
import com.wedknots.delivery.DeliveryResult;
import com.wedknots.delivery.DeliveryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Email delivery provider
 * Handles delivery of messages via email
 */
@Component
public class EmailDeliveryProvider implements MessageDeliveryProvider {
    private static final Logger logger = LoggerFactory.getLogger(EmailDeliveryProvider.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private DeliveryConfig deliveryConfig;

    @Override
    public boolean canDeliver(DeliveryRequest request) {
        return request.getRecipient() != null &&
               request.getRecipient().getContactEmail() != null &&
               !request.getRecipient().getContactEmail().isEmpty();
    }

    @Override
    public DeliveryResult deliver(DeliveryRequest request) {
        try {
            if (!isConfigured()) {
                logger.info("Email delivery not configured, recording message for manual delivery");
                return new DeliveryResult(true, DeliveryMode.EMAIL, "EMAIL_RECORDED", "Message recorded for manual email delivery");
            }

            String recipientEmail = request.getRecipient() != null ? request.getRecipient().getContactEmail() : null;
            String displayName = request.getOverrideContactName();
            if (displayName == null || displayName.isEmpty()) {
                if (request.getRecipient() != null) {
                    displayName = (request.getRecipient().getContactFirstName() != null ? request.getRecipient().getContactFirstName() : "") +
                                 " " + (request.getRecipient().getContactLastName() != null ? request.getRecipient().getContactLastName() : "");
                    displayName = displayName.trim();
                } else {
                    displayName = "";
                }
            }

            String subject = request.getTitle() != null ? request.getTitle() : "Wedding Invitation";
            String content = request.getContent();

            // Build email message
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientEmail);
            message.setFrom(deliveryConfig.getEmailConfig().getSenderEmail());
            message.setSubject(subject);
            message.setText(content);

            // Send email
            if (mailSender != null) {
                mailSender.send(message);
                logger.info("Email sent successfully to {} for message/invitation: {}",
                    recipientEmail, request.getMessageId());

                DeliveryResult result = new DeliveryResult();
                result.setSuccess(true);
                result.setDeliveryMode(DeliveryMode.EMAIL);
                result.setStatus("DELIVERED");
                result.setDeliveryId(request.getMessageId());
                return result;
            } else {
                logger.warn("JavaMailSender not available, recording for manual delivery");
                return new DeliveryResult(
                    true,
                    DeliveryMode.EMAIL,
                    "EMAIL_RECORDED",
                    "Message recorded for manual email delivery"
                );
            }

        } catch (Exception e) {
            logger.error("Failed to send email to {}", request.getRecipient().getContactEmail(), e);
            return new DeliveryResult(false, DeliveryMode.EMAIL, e.getMessage());
        }
    }

    @Override
    public String getProviderName() {
        return "Email";
    }

    @Override
    public boolean isConfigured() {
        return deliveryConfig != null &&
               deliveryConfig.getEmailConfig() != null &&
               deliveryConfig.getEmailConfig().isEnabled();
    }
}

