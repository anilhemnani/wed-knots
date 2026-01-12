package com.wedknots.model.whatsapp;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class Parameter {
    private String type;    // text, currency, date_time
    private String text;    // Parameter text
    private Currency currency;
    @JsonAlias("date_time")
    private DateTime dateTime;
}
