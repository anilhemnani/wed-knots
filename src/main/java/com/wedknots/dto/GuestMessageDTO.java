package com.wedknots.dto;

import com.wedknots.model.GuestMessage;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for GuestMessage to avoid circular references and deep nesting
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestMessageDTO {
    private Long id;

    @JsonProperty("messageContent")
    private String messageContent;

    @JsonProperty("direction")
    private String direction;

    @JsonProperty("messageType")
    private String messageType;

    @JsonProperty("isRead")
    private boolean isRead;

    @JsonProperty("status")
    private String status;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("mediaUrl")
    private String mediaUrl;

    @JsonProperty("whatsappMessageId")
    private String whatsappMessageId;

    /**
     * Convert GuestMessage entity to DTO
     */
    public static GuestMessageDTO fromEntity(GuestMessage message) {
        if (message == null) {
            return null;
        }
        return GuestMessageDTO.builder()
            .id(message.getId())
            .messageContent(message.getMessageContent())
            .direction(message.getDirection().name())
            .messageType(message.getMessageType().name())
            .isRead(message.isRead())
            .status(message.getStatus().name())
            .createdAt(message.getCreatedAt())
            .mediaUrl(message.getMediaUrl())
            .build();
    }
}

