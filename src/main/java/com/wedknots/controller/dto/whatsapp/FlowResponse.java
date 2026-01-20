package com.wedknots.controller.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * WhatsApp Flow Response DTO (Data API v3.0)
 * Represents the response to be encrypted and sent back to WhatsApp
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FlowResponse extends BaseFlowResponse {
    // Inherits all fields from BaseFlowResponse
    // Can add specific fields here if needed
}

