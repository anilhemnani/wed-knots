package com.wedknots.controller.dto.whatsapp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RSVP Flow Data (Data API v3.0)
 * Initial data passed to the RSVP flow
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RsvpFlowData {

    @JsonProperty("guest_id")
    private String guestId;

    @JsonProperty("guest_name")
    private String guestName;

    @JsonProperty("guest_family_name")
    private String guestFamilyName;

    @JsonProperty("event_id")
    private String eventId;

    @JsonProperty("bride_name")
    private String brideName;

    @JsonProperty("groom_name")
    private String groomName;

    @JsonProperty("wedding_date")
    private String weddingDate;

    @JsonProperty("wedding_location")
    private String weddingLocation;

    @JsonProperty("preferred_airport")
    private String preferredAirport;

    @JsonProperty("preferred_station")
    private String preferredStation;
}

