package com.wedknots.controller.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * WhatsApp Flow Data API Request (Data API v3.0)
 * For data_exchange action type
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FlowDataApiRequest extends BaseFlowRequest {

    @JsonProperty("screen")
    private String screen;

    @JsonProperty("data")
    private Map<String, Object> data;
}

