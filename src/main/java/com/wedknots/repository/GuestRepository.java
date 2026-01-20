package com.wedknots.repository;

import com.wedknots.model.Guest;
import com.wedknots.model.WeddingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GuestRepository extends JpaRepository<Guest, Long> {
    // ...existing code...
    Optional<Guest> findByFamilyNameIgnoreCaseAndContactPhone(String familyName, String contactPhone);
    Guest findByContactPhone(String contactPhone);
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
}
