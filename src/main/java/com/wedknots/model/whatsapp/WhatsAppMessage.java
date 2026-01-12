package com.wedknots.model.whatsapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * WhatsApp Message - Simplified model for Jackson parsing
 * Represents incoming messages from WhatsApp Cloud API webhook
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppMessage {

    private String id;                  // Message ID
    private String from;                // Sender phone number
    private String timestamp;           // Message timestamp
    private String type;                // Message type: text, image, document, audio, video, sticker, location, contacts, interactive

    // Text message
    private TextContent text;

    // Media messages
    private MediaContent image;
    private MediaContent document;
    private MediaContent audio;
    private MediaContent video;
    private MediaContent sticker;

    // Location
    private Location location;

    // Multiple contacts
    private List<Contact> contacts;

    // Interactive message (buttons, lists, etc.)
    private Interactive interactive;

    // Status/Error
    private String status;              // For status update webhooks

    /**
     * Text message content
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TextContent {
        private String body;            // Message text
    }

    /**
     * Media content (image, document, audio, video)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MediaContent {
        private String id;              // Media ID
        @JsonProperty("mime_type")
        private String mimeType;        // MIME type
        private String caption;         // For images/videos only
        private String filename;        // For documents only
    }

    /**
     * Location coordinates
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {
        private Double latitude;
        private Double longitude;
        private String name;            // Location name
        private String address;         // Location address
    }

    /**
     * Contact information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Contact {
        private List<Phone> phones;
        private List<Email> emails;
        private Name name;
        private String org;             // Organization
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Phone {
        private String phone;
        private String type;            // HOME, WORK, etc.
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Email {
        private String email;
        private String type;            // HOME, WORK, etc.
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Name {
        @JsonProperty("first_name")
        private String firstName;
        @JsonProperty("last_name")
        private String lastName;
        @JsonProperty("middle_name")
        private String middleName;
        @JsonProperty("name_prefix")
        private String namePrefix;
        @JsonProperty("name_suffix")
        private String nameSuffix;

        public String getFullName() {
            return String.join(" ",
                namePrefix != null ? namePrefix : "",
                firstName != null ? firstName : "",
                middleName != null ? middleName : "",
                lastName != null ? lastName : "",
                nameSuffix != null ? nameSuffix : "")
                .replaceAll(" +", " ").trim();
        }
    }

    /**
     * Interactive message (buttons, list selections, etc.)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Interactive {
        private String type;            // button_reply, list_reply, nfm_reply

        @JsonProperty("button_reply")
        private ButtonReply buttonReply;

        @JsonProperty("list_reply")
        private ListReply listReply;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ButtonReply {
        private String id;
        private String title;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ListReply {
        private String id;
        private String title;
    }

    /**
     * Helper: Get message text content
     */
    public String getMessageText() {
        if ("text".equals(type) && text != null) {
            return text.body;
        }
        if ("image".equals(type) && image != null && image.caption != null) {
            return image.caption;
        }
        if ("document".equals(type) && document != null && document.filename != null) {
            return "Document: " + document.filename;
        }
        return null;
    }

    /**
     * Helper: Check if message is text
     */
    public boolean isText() {
        return "text".equals(type) && text != null;
    }

    /**
     * Helper: Check if message is media
     */
    public boolean isMedia() {
        return type != null && (type.equals("image") || type.equals("document") ||
               type.equals("audio") || type.equals("video") || type.equals("sticker"));
    }
}

