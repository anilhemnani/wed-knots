package com.wedknots.dto;

import lombok.Data;

@Data
public class TravelDetailsRequest {
    private String arrivalMode;
    private String arrivalDateTime;
    private String arrivalFlightNumber;
    private String arrivalAirport;
    private String arrivalTrainNumber;
    private String arrivalStation;
    private String departureMode;
    private String departureDateTime;
    private String departureFlightNumber;
    private String departureAirport;
    private String departureTrainNumber;
    private String departureStation;
}
