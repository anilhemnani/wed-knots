package com.wedknots.model.whatsapp;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.List;
@Data
public class Template {
    private String name;
    private String language;
    private String status;
    private String category;
    @JsonAlias("sub_category")
    private String subCategory;
    @JsonAlias("correct_category")
    private String correctCategory;
    private String id;
    @JsonAlias("previous_category")
    private String previousCategory;
    @JsonAlias("parameter_format")
    private String parameterFormat;
    private List<Component> components;

    /**
     * Get header text if exists
     */
    public String getHeaderText() {
        if (components != null) {
            return components.stream()
                    .filter(c -> "HEADER".equals(c.getType()))
                    .map(Component::getText)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /**
     * Get body text if exists
     */
    public String getBodyText() {
        if (components != null) {
            return components.stream()
                    .filter(c -> "BODY".equals(c.getType()))
                    .map(Component::getText)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /**
     * Get footer text if exists
     */
    public String getFooterText() {
        if (components != null) {
            return components.stream()
                    .filter(c -> "FOOTER".equals(c.getType()))
                    .map(Component::getText)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    /**
     * Get buttons if exists
     */
    public List<Button> getButtons() {
        if (components != null) {
            return components.stream()
                    .filter(c -> "BUTTONS".equals(c.getType()))
                    .findFirst()
                    .map(Component::getButtons)
                    .orElse(null);
        }
        return null;
    }

    /**
     * Check if template has dynamic parameters
     */
    public boolean hasDynamicParameters() {
        if (components == null) {
            return false;
        }
        return components.stream()
                .anyMatch(c -> c.getParameters() != null && !c.getParameters().isEmpty());
    }

    /**
     * Get number of body parameters
     */
    public int getBodyParameterCount() {
        if (components == null) {
            return 0;
        }
        return components.stream()
                .filter(c -> "BODY".equals(c.getType()))
                .findFirst()
                .map(c -> c.getParameters() != null ? c.getParameters().size() : 0)
                .orElse(0);
    }

}
