package com.wedknots.controller.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base class for WhatsApp Flow Requests (Data API v3.0)
 * Contains common attributes shared across all flow request types
 */
@Data
@SuperBuilder
public abstract class BaseFlowRequest {

    @JsonProperty("version")
    private String version;

    @JsonProperty("action")
    private String action;

    @JsonProperty("flow_token")
    private String flowToken;
}

