package com.wedknots.model.whatsapp;

import lombok.Data;

import java.util.List;
@Data
public class Component {
    private String type;        // HEADER, BODY, FOOTER, BUTTONS
    private String format;      // TEXT, IMAGE, VIDEO, DOCUMENT (for HEADER)
    private String text;        // Body text content
    private List<Parameter> parameters;  // For dynamic content
    private List<Button> buttons;        // For ACTION buttons
}
