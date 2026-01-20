package com.wedknots.controller.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * WhatsApp Flow Data API Response (Data API v3.0)
 * Standard response format for data_exchange endpoint
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FlowDataApiResponse extends BaseFlowResponse {
    // Inherits all fields from BaseFlowResponse
    // Can add data API specific fields here if needed
}

