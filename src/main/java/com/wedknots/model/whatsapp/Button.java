package com.wedknots.model.whatsapp;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class Button {
    private String type;        // PHONE_NUMBER, URL, QUICK_REPLY
    private String text;        // Button label
    @JsonAlias("phone_number")
    private String phoneNumber; // For PHONE_NUMBER button
    private String url;         // For URL button
}
