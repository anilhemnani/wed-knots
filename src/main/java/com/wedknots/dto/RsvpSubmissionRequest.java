package com.wedknots.dto;

import com.wedknots.model.RSVPStatus;
import lombok.Data;

import java.util.List;

@Data
public class RsvpSubmissionRequest {
    private Long guestId;
    private String rsvpStatus;
    private List<AttendeeInfo> attendees;
}
