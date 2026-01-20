package com.wedknots.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for WhatsApp Flow payload (v7.3 compatible)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppFlowPayload {

    @JsonProperty("messaging_product")
    private String messagingProduct = "whatsapp";

    @JsonProperty("recipient_type")
    private String recipientType = "individual";

    @JsonProperty("to")
    private String to;

    @JsonProperty("type")
    private String type = "interactive";

    @JsonProperty("interactive")
    private Interactive interactive;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Interactive {

        @JsonProperty("type")
        private String type = "flow";

        @JsonProperty("action")
        private Action action;

        @JsonProperty("body")
        private Body body;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Action {

            @JsonProperty("type")
            private String type = "navigate";

            @JsonProperty("parameters")
            private Parameters parameters;

            @Data
            @Builder
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Parameters {

                @JsonProperty("flow_id")
                private String flowId;

                @JsonProperty("flow_token")
                private String flowToken;

                @JsonProperty("mode")
                private String mode = "published";

                @JsonProperty("initial_screen")
                private String initialScreen;

                @JsonProperty("data")
                private Map<String, Object> data;
            }
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Body {

            @JsonProperty("text")
            private String text;
        }
    }
}

