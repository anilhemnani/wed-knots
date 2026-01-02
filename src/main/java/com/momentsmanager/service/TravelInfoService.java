package com.momentsmanager.service;

import com.momentsmanager.model.Attendee;
import com.momentsmanager.model.TravelInfo;
import com.momentsmanager.repository.AttendeeRepository;
import com.momentsmanager.repository.TravelInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TravelInfoService {

    @Autowired
    private TravelInfoRepository travelInfoRepository;

    @Autowired
    private AttendeeRepository attendeeRepository;

    @Transactional
    public TravelInfo createTravelInfo(Long attendeeId, TravelInfo travelInfo) {
        Optional<Attendee> attendeeOpt = attendeeRepository.findById(attendeeId);
        if (attendeeOpt.isEmpty()) {
            throw new RuntimeException("Attendee not found with id: " + attendeeId);
        }

        Attendee attendee = attendeeOpt.get();
        travelInfo.setAttendee(attendee);
        return travelInfoRepository.save(travelInfo);
    }

    @Transactional
    public TravelInfo updateTravelInfo(Long travelInfoId, TravelInfo updatedInfo) {
        Optional<TravelInfo> existingOpt = travelInfoRepository.findById(travelInfoId);
        if (existingOpt.isEmpty()) {
            throw new RuntimeException("TravelInfo not found with id: " + travelInfoId);
        }

        TravelInfo existing = existingOpt.get();
        existing.setArrivalMode(updatedInfo.getArrivalMode());
        existing.setArrivalDateTime(updatedInfo.getArrivalDateTime());
        existing.setArrivalFlightNumber(updatedInfo.getArrivalFlightNumber());
        existing.setArrivalTrainNumber(updatedInfo.getArrivalTrainNumber());
        existing.setArrivalAirport(updatedInfo.getArrivalAirport());
        existing.setArrivalStation(updatedInfo.getArrivalStation());
        existing.setDepartureMode(updatedInfo.getDepartureMode());
        existing.setDepartureDateTime(updatedInfo.getDepartureDateTime());
        existing.setDepartureFlightNumber(updatedInfo.getDepartureFlightNumber());
        existing.setDepartureTrainNumber(updatedInfo.getDepartureTrainNumber());
        existing.setDepartureAirport(updatedInfo.getDepartureAirport());
        existing.setDepartureStation(updatedInfo.getDepartureStation());
        existing.setNeedsPickup(updatedInfo.getNeedsPickup());
        existing.setNeedsDrop(updatedInfo.getNeedsDrop());
        existing.setSpecialRequirements(updatedInfo.getSpecialRequirements());
        existing.setNotes(updatedInfo.getNotes());

        return travelInfoRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public Optional<TravelInfo> getTravelInfoByAttendeeId(Long attendeeId) {
        return travelInfoRepository.findByAttendeeId(attendeeId);
    }

    @Transactional
    public void deleteTravelInfo(Long travelInfoId) {
        travelInfoRepository.deleteById(travelInfoId);
    }
}

