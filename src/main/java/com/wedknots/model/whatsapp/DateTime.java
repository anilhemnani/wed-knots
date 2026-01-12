package com.wedknots.model.whatsapp;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class DateTime {
    @JsonAlias("fallback_value")
    private String fallbackValue;
}
