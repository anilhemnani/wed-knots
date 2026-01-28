package com.wedknots.delivery.provider;

import com.wedknots.delivery.DeliveryMode;
import com.wedknots.delivery.DeliveryRequest;
import com.wedknots.delivery.DeliveryResult;
import com.wedknots.delivery.DeliveryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * SMS delivery provider
 * Handles delivery of messages via SMS/text message
 * Supports multiple providers including Localphone SIP Server
 */
@Component
public class SMSDeliveryProvider implements MessageDeliveryProvider {
    private static final Logger logger = LoggerFactory.getLogger(SMSDeliveryProvider.class);

    @Autowired
    private DeliveryConfig deliveryConfig;

    @Autowired(required = false)
    private RestTemplate restTemplate;

    @Override
    public boolean canDeliver(DeliveryRequest request) {
        String phone = request.getOverridePhoneNumber();
        if (phone != null && !phone.isEmpty()) {
            return true;
        }
        return request.getRecipient() != null &&
               request.getRecipient().getPrimaryPhoneNumber() != null &&
               !request.getRecipient().getPrimaryPhoneNumber().isEmpty();
    }

    @Override
    public DeliveryResult deliver(DeliveryRequest request) {
        try {
            if (!isConfigured()) {
                logger.info("SMS delivery not configured, recording message for manual delivery");
                return new DeliveryResult(
                    true,
                    DeliveryMode.SMS,
                    "SMS_RECORDED",
                    "Message recorded for manual SMS delivery"
                );
            }

            String phoneNumber = request.getOverridePhoneNumber();
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                phoneNumber = request.getRecipient().getPrimaryPhoneNumber();
            }
            String content = buildSMSContent(request);
            String provider = deliveryConfig.getSmsConfig().getProvider();

            if ("d7".equalsIgnoreCase(provider) || "d7networks".equalsIgnoreCase(provider)) {
                return sendViaD7(phoneNumber, content, request.getMessageId());
            } else if ("twilio".equalsIgnoreCase(provider)) {
                return sendViaTwilio(phoneNumber, content, request.getMessageId());
            } else {
                logger.warn("Unknown SMS provider: {}, recording for manual delivery", provider);
                return new DeliveryResult(
                    true,
                    DeliveryMode.SMS,
                    "SMS_RECORDED",
                    "Message recorded for manual SMS delivery (unknown provider)"
                );
            }

        } catch (Exception e) {
            logger.error("Failed to send SMS to {}", request.getRecipient().getPrimaryPhoneNumber(), e);
            return new DeliveryResult(false, DeliveryMode.SMS, e.getMessage());
        }
    }

    /**
     * Send SMS via D7 Networks
     */
    private DeliveryResult sendViaD7(String phoneNumber, String content, String messageId) {
        try {
            if (restTemplate == null) {
                restTemplate = new RestTemplate();
            }

            String apiUrl = deliveryConfig.getSmsConfig().getApiUrl();
            String apiKey = deliveryConfig.getSmsConfig().getApiKey();
            String apiSecret = deliveryConfig.getSmsConfig().getApiSecret();
            String senderId = deliveryConfig.getSmsConfig().getSenderId();

            if (apiUrl == null || apiUrl.isEmpty()) {
                throw new IllegalStateException("D7 API URL not configured");
            }

            // Build payload
            Map<String, Object> message = new HashMap<>();
            message.put("channel", "sms");
            message.put("recipients", new String[]{phoneNumber});
            message.put("content", content);
            message.put("sender_id", senderId != null ? senderId : "WedKnots");

            Map<String, Object> payload = new HashMap<>();
            payload.put("messages", new Object[]{message});

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (apiKey != null && apiSecret != null) {
                String basic = Base64.getEncoder().encodeToString((apiKey + ":" + apiSecret).getBytes(StandardCharsets.UTF_8));
                headers.set("Authorization", "Basic " + basic);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            logger.info("Sending SMS via D7 Networks to {} - Message ID: {}", phoneNumber, messageId);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("SMS sent successfully via D7 to {} - Response: {}", phoneNumber, response.getBody());
                DeliveryResult result = new DeliveryResult();
                result.setSuccess(true);
                result.setDeliveryMode(DeliveryMode.SMS);
                result.setStatus("DELIVERED");
                result.setDeliveryId(messageId);
                return result;
            }

            logger.error("Failed to send SMS via D7 - Status: {}, Response: {}", response.getStatusCode(), response.getBody());
            return new DeliveryResult(false, DeliveryMode.SMS, "Failed with status: " + response.getStatusCode());

        } catch (Exception e) {
            logger.error("Error sending SMS via D7", e);
            return new DeliveryResult(false, DeliveryMode.SMS, e.getMessage());
        }
    }

    /**
     * Send SMS via Twilio (placeholder for future implementation)
     */
    private DeliveryResult sendViaTwilio(String phoneNumber, String content, String messageId) {
        logger.info("Twilio SMS sending not yet implemented, recording for manual delivery");
        return new DeliveryResult(
            true,
            DeliveryMode.SMS,
            "SMS_RECORDED",
            "Message recorded for manual SMS delivery (Twilio not yet implemented)"
        );
    }

    @Override
    public String getProviderName() {
        return "SMS";
    }

    @Override
    public boolean isConfigured() {
        return deliveryConfig != null &&
               deliveryConfig.getSmsConfig() != null &&
               deliveryConfig.getSmsConfig().isEnabled();
    }

    private String buildSMSContent(DeliveryRequest request) {
        // SMS has character limits, truncate as needed
        String title = request.getTitle() != null ? request.getTitle() : "";
        String content = request.getContent() != null ? request.getContent() : "";
        String fullMessage = title + "\n" + content;

        // Limit to 160 characters for single SMS
        if (fullMessage.length() > 160) {
            fullMessage = fullMessage.substring(0, 157) + "...";
        }
        return fullMessage;
    }
}

