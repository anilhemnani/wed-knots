package com.wedknots.controller.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * WhatsApp Flow Data Exchange DTO (Data API v3.0)
 * Used for INIT action and data exchange between screens
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FlowDataExchange extends BaseFlowRequest {

    @JsonProperty("screen")
    private String screen;

    @JsonProperty("data")
    private Object data;
}

