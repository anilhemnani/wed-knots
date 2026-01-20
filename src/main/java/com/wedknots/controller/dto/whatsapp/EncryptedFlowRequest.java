package com.wedknots.controller.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Encrypted Flow Request Payload (Data API v3.0)
 * Represents the encrypted payload received from WhatsApp
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EncryptedFlowRequest {

    @JsonProperty("encrypted_flow_data")
    private String encryptedFlowData;

    @JsonProperty("encrypted_aes_key")
    private String encryptedAesKey;

    @JsonProperty("initial_vector")
    private String initialVector;
}

