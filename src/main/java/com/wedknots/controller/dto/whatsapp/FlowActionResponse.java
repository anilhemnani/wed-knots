package com.wedknots.controller.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * WhatsApp Flow Action Response (Data API v3.0)
 * Response for navigate, update_data, and complete actions
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FlowActionResponse extends BaseFlowResponse {
    // Inherits all fields from BaseFlowResponse
    // Can add action-specific fields here if needed
}

