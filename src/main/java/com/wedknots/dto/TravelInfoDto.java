package com.wedknots.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wedknots.model.Guest;
import com.wedknots.model.ModeOfTravel;
import com.wedknots.model.TravelInfo;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TravelInfoDto {
    private Long guestId;
    private Integer maxAllowedAttendees;
    private List<AttendeeInfo> attendees;
    private Long id;
    private ModeOfTravel arrivalMode; // Flight, Train, Car, Bus, etc.
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime arrivalDateTime;
    private String arrivalFlightNumber;
    private String arrivalTrainNumber;
    private String arrivalAirport;
    private String arrivalStation;
    private ModeOfTravel departureMode; // Flight, Train, Car, Bus, etc.
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime departureDateTime;
    private String departureFlightNumber;
    private String departureTrainNumber;
    private String departureAirport;
    private String departureStation;

}
