package com.wedknots.service;

import com.wedknots.model.RSVP;
import com.wedknots.model.RSVPStatus;
import com.wedknots.repository.RSVPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RSVPService {

    @Autowired
    private RSVPRepository rsvpRepository;

    public List<RSVP> getAllRSVPs() {
        return rsvpRepository.findAll();
    }

    public Optional<RSVP> getRSVPById(Long id) {
        return rsvpRepository.findById(id);
    }

    public Optional<RSVP> getRSVPByGuestId(Long guestId) {
        return rsvpRepository.findByGuestId(guestId);
    }

    @Transactional
    public RSVP createRSVP(RSVP rsvp) {
        // Set default status if not provided
        if (rsvp.getStatus() == null) {
            rsvp.setStatus(RSVPStatus.PENDING);
        }
        return rsvpRepository.save(rsvp);
    }

    @Transactional
    public RSVP updateRSVPStatus(Long rsvpId, RSVPStatus status, int attendeeCount) {
        Optional<RSVP> rsvpOpt = rsvpRepository.findById(rsvpId);
        if (rsvpOpt.isPresent()) {
            RSVP rsvp = rsvpOpt.get();
            rsvp.setStatus(status);
            rsvp.setAttendeeCount(attendeeCount);
            return rsvpRepository.save(rsvp);
        }
        throw new RuntimeException("RSVP not found with id: " + rsvpId);
    }

    @Transactional
    public RSVP acceptRSVP(Long rsvpId, int attendeeCount) {
        return updateRSVPStatus(rsvpId, RSVPStatus.ATTENDING, attendeeCount);
    }

    @Transactional
    public RSVP declineRSVP(Long rsvpId) {
        return updateRSVPStatus(rsvpId, RSVPStatus.NOT_ATTENDING, 0);
    }

    @Transactional
    public void deleteRSVP(Long id) {
        rsvpRepository.deleteById(id);
    }
}
