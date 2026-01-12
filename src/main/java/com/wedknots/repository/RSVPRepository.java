package com.wedknots.repository;

import com.wedknots.model.Guest;
import com.wedknots.model.RSVP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RSVPRepository extends JpaRepository<RSVP, Long> {
    Optional<RSVP> findByGuest(Guest guest);
    Optional<RSVP> findByGuestId(Long guestId);
    List<RSVP> findByEventId(Long eventId);
}

