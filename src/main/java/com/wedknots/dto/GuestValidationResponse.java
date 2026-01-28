package com.wedknots.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GuestValidationResponse {
    private boolean valid;
    private String message;
    private Long guestId;
    private String guestName;
    private String familyName;
    private Long eventId;
    private Integer maxAttendees;

    public static GuestValidationResponse success(Long guestId, String guestName, String familyName, Long eventId, Integer maxAttendees) {
        return new GuestValidationResponse(true, null, guestId, guestName, familyName, eventId, maxAttendees);
    }

    public static GuestValidationResponse error(String message) {
        return new GuestValidationResponse(false, message, null, null, null, null, null);
    }
}
