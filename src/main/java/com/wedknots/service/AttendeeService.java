package com.wedknots.service;

import com.wedknots.model.Attendee;
import com.wedknots.model.RSVP;
import com.wedknots.repository.AttendeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AttendeeService {

    @Autowired
    private AttendeeRepository attendeeRepository;

    @Autowired
    private RSVPService rsvpService;

    public List<Attendee> getAllAttendees() {
        return attendeeRepository.findAll();
    }

    public Optional<Attendee> getAttendeeById(Long id) {
        return attendeeRepository.findById(id);
    }

    public List<Attendee> getAttendeesByRsvpId(Long rsvpId) {
        return attendeeRepository.findByRsvpId(rsvpId);
    }

    @Transactional
    public Attendee createAttendee(Long rsvpId, Attendee attendee) {
        Optional<RSVP> rsvpOpt = rsvpService.getRSVPById(rsvpId);
        if (rsvpOpt.isPresent()) {
            attendee.setRsvp(rsvpOpt.get());
            return attendeeRepository.save(attendee);
        }
        throw new RuntimeException("RSVP not found with id: " + rsvpId);
    }

    @Transactional
    public Attendee updateAttendee(Long id, Attendee attendeeDetails) {
        Optional<Attendee> attendeeOpt = attendeeRepository.findById(id);
        if (attendeeOpt.isPresent()) {
            Attendee attendee = attendeeOpt.get();
            attendee.setName(attendeeDetails.getName());
            attendee.setMobileNumber(attendeeDetails.getMobileNumber());
            attendee.setAgeGroup(attendeeDetails.getAgeGroup());
            return attendeeRepository.save(attendee);
        }
        throw new RuntimeException("Attendee not found with id: " + id);
    }

    @Transactional
    public void deleteAttendee(Long id) {
        attendeeRepository.deleteById(id);
    }

    @Transactional
    public void deleteAttendeesByRsvpId(Long rsvpId) {
        List<Attendee> attendees = attendeeRepository.findByRsvpId(rsvpId);
        attendeeRepository.deleteAll(attendees);
    }
}

