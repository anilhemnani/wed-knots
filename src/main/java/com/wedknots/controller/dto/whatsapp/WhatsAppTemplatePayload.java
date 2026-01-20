package com.wedknots.controller.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * WhatsApp Template Message Payload (Data API v3.0)
 * Used for sending template messages with flow buttons
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppTemplatePayload {

    @JsonProperty("messaging_product")
    private String messagingProduct = "whatsapp";

    @JsonProperty("recipient_type")
    private String recipientType = "individual";

    private String to;
    private String type = "template";
    private Template template;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Template {
        private String name;
        private Language language;
        private List<Component> components;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Language {
            private String code;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Component {
        private String type;

        @JsonProperty("sub_type")
        private String subType;

        private Integer index;
        private List<Map<String, Object>> parameters;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Parameter {
        private String type; // "text", "payload", "image", "document", "video"
        private String text;
        private String payload;
    }
}

