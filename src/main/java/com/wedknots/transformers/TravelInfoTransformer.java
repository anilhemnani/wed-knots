package com.wedknots.transformers;

import com.wedknots.dto.TravelInfoDto;
import com.wedknots.model.TravelInfo;

public class TravelInfoTransformer {

    public static TravelInfoDto toDto(com.wedknots.model.TravelInfo travelInfo, TravelInfoDto dto) {
        if (dto == null) {
            dto = new TravelInfoDto();
        }
        if (travelInfo == null) {
            return dto;
        }
        dto.setId(travelInfo.getId());
        dto.setArrivalMode(travelInfo.getArrivalMode());
        dto.setArrivalDateTime(travelInfo.getArrivalDateTime());
        dto.setArrivalFlightNumber(travelInfo.getArrivalFlightNumber());
        dto.setArrivalAirport(travelInfo.getArrivalAirport());
        dto.setArrivalTrainNumber(travelInfo.getArrivalTrainNumber());
        dto.setArrivalStation(travelInfo.getArrivalStation());
        dto.setDepartureMode(travelInfo.getDepartureMode());
        dto.setDepartureDateTime(travelInfo.getDepartureDateTime());
        dto.setDepartureFlightNumber(travelInfo.getDepartureFlightNumber());
        dto.setDepartureAirport(travelInfo.getDepartureAirport());
        dto.setDepartureTrainNumber(travelInfo.getDepartureTrainNumber());
        dto.setDepartureStation(travelInfo.getDepartureStation());
        return dto;
    }

    public static TravelInfo toEntity(TravelInfoDto dto, com.wedknots.model.TravelInfo travelInfo) {
        if (travelInfo == null) {
            travelInfo = new com.wedknots.model.TravelInfo();
        }
        if (dto == null) {
            return travelInfo;
        }
        travelInfo.setId(travelInfo.getId());
        travelInfo.setArrivalMode(dto.getArrivalMode());
        travelInfo.setArrivalDateTime(dto.getArrivalDateTime());
        travelInfo.setArrivalFlightNumber(dto.getArrivalFlightNumber());
        travelInfo.setArrivalAirport(dto.getArrivalAirport());
        travelInfo.setArrivalTrainNumber(dto.getArrivalTrainNumber());
        travelInfo.setArrivalStation(dto.getArrivalStation());
        travelInfo.setDepartureMode(dto.getDepartureMode());
        travelInfo.setDepartureDateTime(dto.getDepartureDateTime());
        travelInfo.setDepartureFlightNumber(dto.getDepartureFlightNumber());
        travelInfo.setDepartureAirport(dto.getDepartureAirport());
        travelInfo.setDepartureTrainNumber(dto.getDepartureTrainNumber());
        travelInfo.setDepartureStation(dto.getDepartureStation());
        return travelInfo;
    }
}
