package com.wedknots.repository;

import com.wedknots.model.Guest;
import com.wedknots.model.WeddingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GuestRepository extends JpaRepository<Guest, Long> {
    @Query("SELECT g FROM Guest g WHERE g.event.id = :eventId")
    List<Guest> findByEventId(@Param("eventId") Long eventId);

    @Query("SELECT DISTINCT g FROM Guest g LEFT JOIN FETCH g.phoneNumbers WHERE g.event.id = :eventId")
    List<Guest> findByEventIdWithPhones(@Param("eventId") Long eventId);

    @Query("SELECT g FROM Guest g WHERE g.event.id = :eventId AND LOWER(g.side) = LOWER(:side)")
    List<Guest> findByEventIdAndSideIgnoreCase(@Param("eventId") Long eventId, @Param("side") String side);

    /**
     * Find guest by family name and phone number (searches primary phone and additional phones)
     */
    @Query("SELECT DISTINCT g FROM Guest g " +
           "LEFT JOIN g.phoneNumbers gp " +
           "WHERE UPPER(g.familyName) = UPPER(:familyName) " +
           "AND (g.primaryPhoneNumber = :phoneNumber OR gp.phoneNumber = :phoneNumber)")
    Optional<Guest> findByFamilyNameIgnoreCaseAndContactPhone(@Param("familyName") String familyName,
                                                               @Param("phoneNumber") String phoneNumber);

    /**
     * Find guest by phone number (searches primary phone and additional phones)
     */
    @Query("SELECT DISTINCT g FROM Guest g " +
           "LEFT JOIN g.phoneNumbers gp " +
           "WHERE g.primaryPhoneNumber = :phoneNumber OR gp.phoneNumber = :phoneNumber")
    Guest findByContactPhone(@Param("phoneNumber") String phoneNumber);

    Guest findByContactEmail(String contactEmail);

    @Query("select g.event.id as eventId, count(g) as guests from Guest g group by g.event.id")
    List<Object[]> summarizeGuestsPerEvent();

    /**
     * Find guest by family name and ANY of their phone numbers
     * Allows login using any phone number associated with the guest
     */
    @Query("SELECT DISTINCT g FROM Guest g " +
           "JOIN g.phoneNumbers gp " +
           "WHERE LOWER(g.familyName) = LOWER(:familyName) " +
           "AND gp.phoneNumber = :phoneNumber")
    Optional<Guest> findByFamilyNameAndAnyPhoneNumber(@Param("familyName") String familyName,
                                                      @Param("phoneNumber") String phoneNumber);

    /**
     * Find guest by ANY of their phone numbers (across all guests)
     * Used to identify which guest owns a specific phone
     */
    @Query("SELECT DISTINCT g FROM Guest g " +
           "JOIN g.phoneNumbers gp " +
           "WHERE gp.phoneNumber = :phoneNumber")
    Optional<Guest> findByAnyPhoneNumber(@Param("phoneNumber") String phoneNumber);

    /**
     * Find guest by family name and any phone number
     * Primary method for multi-phone guest login
     */
    @Query("SELECT DISTINCT g FROM Guest g " +
           "JOIN g.phoneNumbers gp " +
           "WHERE UPPER(g.familyName) = UPPER(:familyName) " +
           "AND gp.phoneNumber = :phoneNumber")
    Optional<Guest> findGuestByFamilyNameAndPhone(@Param("familyName") String familyName,
                                                  @Param("phoneNumber") String phoneNumber);

    /**
     * Find guest by event and phone number (for mobile RSVP)
     * Supports multiple phone numbers per guest
     */
    @Query("SELECT DISTINCT g FROM Guest g " +
           "JOIN g.phoneNumbers gp " +
           "WHERE g.event = :event " +
           "AND gp.phoneNumber = :phoneNumber")
    Optional<Guest> findByEventAndPhoneNumber(@Param("event") WeddingEvent event,
                                               @Param("phoneNumber") String phoneNumber);

    /**
     * Find guest by phone last name and any phone number (uses exact stored phone string)
     * Searches both:
     * 1. Guest's own contactLastName + primaryPhoneNumber
     * 2. Additional phone's contactLastName + that specific phoneNumber
     */
    @Query("SELECT DISTINCT g FROM Guest g " +
           "LEFT JOIN g.phoneNumbers gp " +
           "WHERE (UPPER(g.contactLastName) = UPPER(:contactLastName) AND g.primaryPhoneNumber = :phoneNumber) " +
           "OR (UPPER(gp.contactLastName) = UPPER(:contactLastName) AND gp.phoneNumber = :phoneNumber)")
    Optional<Guest> findByContactLastNameAndAnyPhone(@Param("contactLastName") String contactLastName,
                                                     @Param("phoneNumber") String phoneNumber);

    /**
     * Check if email already exists for another guest in the same event
     */
    @Query("SELECT COUNT(g) > 0 FROM Guest g WHERE g.contactEmail = :email AND g.event.id = :eventId AND (:guestId IS NULL OR g.id != :guestId)")
    Boolean existsByEmailInEvent(@Param("email") String email, @Param("eventId") Long eventId, @Param("guestId") Long guestId);

    /**
     * Find guest by ID with eagerly loaded phone numbers
     * Used for edit form to show all phone numbers
     */
    @Query("SELECT g FROM Guest g LEFT JOIN FETCH g.phoneNumbers WHERE g.id = :guestId")
    Optional<Guest> findByIdWithPhones(@Param("guestId") Long guestId);

    /**
     * Find guest by ID with RSVP and attendees eagerly loaded
     * Ensures attendee data is available for serialization in the controller
     */
    @Query("SELECT g FROM Guest g " +
           "LEFT JOIN FETCH g.rsvp r " +
           "LEFT JOIN FETCH r.attendees " +
           "WHERE g.id = :guestId")
    Optional<Guest> findByIdWithRsvpAndAttendees(@Param("guestId") Long guestId);

    /**
     * Find guest by primary phone number
     */
    @Query("SELECT g FROM Guest g WHERE g.primaryPhoneNumber = :phoneNumber")
    Optional<Guest> findByPrimaryPhoneNumber(@Param("phoneNumber") String phoneNumber);

    /**
     * Find guest by additional phone number
     */
    @Query("SELECT DISTINCT g FROM Guest g JOIN g.phoneNumbers gp WHERE gp.phoneNumber = :phoneNumber")
    Optional<Guest> findByAdditionalPhoneNumber(@Param("phoneNumber") String phoneNumber);
}
