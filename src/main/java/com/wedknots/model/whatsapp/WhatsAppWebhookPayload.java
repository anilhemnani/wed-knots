package com.wedknots.model.whatsapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * WhatsApp Webhook Response - Wrapper for webhook payloads
 * Represents the complete structure of messages from WhatsApp Cloud API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppWebhookPayload {

    private String object;              // "whatsapp_business_account"
    private List<Entry> entry;          // Array of entries

    /**
     * Entry in webhook payload
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entry {
        private String id;              // Business Account ID
        private List<Change> changes;   // Array of changes
    }

    /**
     * Change (field update)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Change {
        private String field;           // Field name (messages, message_status, etc.)
        private Value value;            // Field value
    }

    /**
     * Value contains the actual data
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Value {
        @JsonProperty("messaging_product")
        private String messagingProduct;// "whatsapp"

        private String from;            // Phone number (for status updates)
        private String id;              // Webhook ID
        private String timestamp;       // Timestamp

        private List<WhatsAppMessage> messages;      // Incoming messages
        private List<StatusUpdate> statuses;          // Message status updates
        private List<TemplateStatusUpdate> template_status_update;  // Template status

        @JsonProperty("phone_number_id")
        private String phoneNumberId;   // Phone number ID

        @JsonProperty("display_phone_number")
        private String displayPhoneNumber; // Display phone number
    }

    /**
     * Message Status Update
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StatusUpdate {
        private String id;              // Message ID
        private String status;          // Status: sent, delivered, read, failed
        private String timestamp;       // Status timestamp
        private RecipientInfo recipient_id;  // Recipient
        private List<Error> errors;     // Errors if any

        /**
         * Recipient info for status
         */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class RecipientInfo {
            private String id;          // Recipient phone number
        }
    }

    /**
     * Error object
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Error {
        private Integer code;
        private String title;
        private String message;
    }

    /**
     * Template Status Update
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TemplateStatusUpdate {
        private String id;              // Template ID
        private String event;           // Event type (APPROVED, PENDING, REJECTED)
        private String language;        // Template language
    }

    /**
     * Helper: Get first message if any
     */
    public WhatsAppMessage getFirstMessage() {
        if (entry == null || entry.isEmpty()) return null;
        for (Entry e : entry) {
            if (e.changes != null) {
                for (Change c : e.changes) {
                    if (c.value != null && c.value.messages != null && !c.value.messages.isEmpty()) {
                        return c.value.messages.get(0);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Helper: Get first status update if any
     */
    public StatusUpdate getFirstStatusUpdate() {
        if (entry == null || entry.isEmpty()) return null;
        for (Entry e : entry) {
            if (e.changes != null) {
                for (Change c : e.changes) {
                    if (c.value != null && c.value.statuses != null && !c.value.statuses.isEmpty()) {
                        return c.value.statuses.get(0);
                    }
                }
            }
        }
        return null;
    }
}

