package com.wedknots.model.whatsapp;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class Currency {
    @JsonAlias("currency_code")
    private String currencyCode; // ISO 4217 currency code, e.g., "USD"
    private String amount;       // Amount in minor units (e.g., cents)
}
