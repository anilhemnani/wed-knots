package com.wedknots.controller.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * Base class for WhatsApp Flow Responses (Data API v3.0)
 * Contains common attributes shared across all flow response types
 */
@Data
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseFlowResponse {

    @JsonProperty("version")
    private String version = "3.0";

    @JsonProperty("screen")
    private String screen;

    @JsonProperty("data")
    private Map<String, Object> data;

    @JsonProperty("error_message")
    private String errorMessage;
}

