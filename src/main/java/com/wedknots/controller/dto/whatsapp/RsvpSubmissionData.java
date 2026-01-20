package com.wedknots.controller.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RSVP Submission Data (Data API v3.0)
 * Data submitted from the RSVP flow
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RsvpSubmissionData {

    @JsonProperty("guest_id")
    private String guestId;

    @JsonProperty("event_id")
    private String eventId;

    @JsonProperty("rsvp_status")
    private String rsvpStatus; // "attending", "not_attending", "maybe"

    @JsonProperty("attendee_count")
    private Integer attendeeCount;

    @JsonProperty("dietary_restrictions")
    private String dietaryRestrictions;

    @JsonProperty("travel_mode")
    private String travelMode; // "flight", "train", "car", "other"

    @JsonProperty("arrival_date")
    private String arrivalDate;

    @JsonProperty("departure_date")
    private String departureDate;

    @JsonProperty("special_requests")
    private String specialRequests;

    @JsonProperty("flow_token")
    private String flowToken;
}

