package com.wedknots.model.whatsapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * WhatsApp Message Template - Simplified model for Jackson parsing
 * Represents WhatsApp templates from Meta Business Manager API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppTemplate {

    private String id;                  // Template ID
    private String name;                // Template name
    private String language;            // Language code (e.g., "en")
    private String status;              // Status (APPROVED, PENDING, REJECTED)
    private String category;            // Category (UTILITY, MARKETING, AUTHENTICATION)

    @JsonProperty("sub_category")
    private String subCategory;         // Sub category

    @JsonProperty("correct_category")
    private String correctCategory;     // Correct category

    @JsonProperty("previous_category")
    private String previousCategory;    // Previous category

    @JsonProperty("parameter_format")
    private String parameterFormat;     // Parameter format (POSITIONAL)

    private List<TemplateComponent> components;  // Template components

    /**
     * Template Component (HEADER, BODY, FOOTER, BUTTONS)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TemplateComponent {
        private String type;            // HEADER, BODY, FOOTER, BUTTONS
        private String format;          // TEXT, IMAGE, VIDEO, DOCUMENT (for HEADER)
        private String text;            // Component text content
        private List<TemplateParameter> parameters;  // Dynamic parameters
        private List<TemplateButton> buttons;        // Action buttons

        /**
         * Helper: Get first N characters of text
         */
        public String getTextPreview(int maxLength) {
            if (text == null) return "";
            return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
        }
    }

    /**
     * Template Parameter (for dynamic values like {{1}}, {{2}})
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TemplateParameter {
        private String type;            // text, currency, date_time
        private String text;            // Parameter text
    }

    /**
     * Template Button
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TemplateButton {
        private String type;            // PHONE_NUMBER, URL, QUICK_REPLY
        private String text;            // Button label

        @JsonProperty("phone_number")
        private String phoneNumber;     // For PHONE_NUMBER button

        private String url;             // For URL button
    }

    /**
     * Helper: Get header component
     */
    public TemplateComponent getHeader() {
        return components != null
            ? components.stream()
                .filter(c -> "HEADER".equals(c.getType()))
                .findFirst()
                .orElse(null)
            : null;
    }

    /**
     * Helper: Get body component
     */
    public TemplateComponent getBody() {
        return components != null
            ? components.stream()
                .filter(c -> "BODY".equals(c.getType()))
                .findFirst()
                .orElse(null)
            : null;
    }

    /**
     * Helper: Get footer component
     */
    public TemplateComponent getFooter() {
        return components != null
            ? components.stream()
                .filter(c -> "FOOTER".equals(c.getType()))
                .findFirst()
                .orElse(null)
            : null;
    }

    /**
     * Helper: Get buttons component
     */
    public TemplateComponent getButtonsComponent() {
        return components != null
            ? components.stream()
                .filter(c -> "BUTTONS".equals(c.getType()))
                .findFirst()
                .orElse(null)
            : null;
    }

    /**
     * Helper: Get all buttons
     */
    public List<TemplateButton> getButtons() {
        TemplateComponent buttonComp = getButtonsComponent();
        return buttonComp != null ? buttonComp.getButtons() : null;
    }

    /**
     * Helper: Check if template is approved
     */
    public boolean isApproved() {
        return "APPROVED".equals(status);
    }

    /**
     * Helper: Check if template has dynamic parameters
     */
    public boolean hasDynamicParameters() {
        return components != null && components.stream()
            .anyMatch(c -> c.getParameters() != null && !c.getParameters().isEmpty());
    }

    /**
     * Helper: Count dynamic parameters in body
     */
    public int getBodyParameterCount() {
        TemplateComponent body = getBody();
        return body != null && body.getParameters() != null ? body.getParameters().size() : 0;
    }
}

