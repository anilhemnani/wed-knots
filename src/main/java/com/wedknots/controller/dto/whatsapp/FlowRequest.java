package com.wedknots.controller.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * WhatsApp Flow Request DTO (Data API v3.0)
 * Represents the decrypted request payload from WhatsApp
 */
@Data
@SuperBuilder
@Jacksonized
@EqualsAndHashCode(callSuper = true)
public class FlowRequest extends BaseFlowRequest {

    @JsonProperty("screen")
    private String screen;

    @JsonProperty("data")
    private Object data;
}

