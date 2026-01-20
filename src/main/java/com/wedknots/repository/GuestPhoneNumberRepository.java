package com.wedknots.repository;

import com.wedknots.model.Guest;
import com.wedknots.model.GuestPhoneNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuestPhoneNumberRepository extends JpaRepository<GuestPhoneNumber, Long> {

    /**
     * Find all phone numbers for a specific guest
     */
    List<GuestPhoneNumber> findByGuest(Guest guest);

    /**
     * Find all phone numbers for a guest, sorted by primary first
     */
    @Query("SELECT gpn FROM GuestPhoneNumber gpn WHERE gpn.guest.id = :guestId ORDER BY gpn.isPrimary DESC, gpn.createdAt ASC")
    List<GuestPhoneNumber> findByGuestIdOrderedByPrimary(@Param("guestId") Long guestId);

    /**
     * Find the primary phone number for a guest
     */
    @Query("SELECT gpn FROM GuestPhoneNumber gpn WHERE gpn.guest.id = :guestId AND gpn.isPrimary = true")
    Optional<GuestPhoneNumber> findPrimaryPhoneNumberByGuestId(@Param("guestId") Long guestId);

    /**
     * Find phone numbers by type for a guest
     */
    @Query("SELECT gpn FROM GuestPhoneNumber gpn WHERE gpn.guest.id = :guestId AND gpn.phoneType = :phoneType")
    List<GuestPhoneNumber> findByGuestIdAndPhoneType(@Param("guestId") Long guestId, @Param("phoneType") GuestPhoneNumber.PhoneType phoneType);

    /**
     * Check if a phone number already exists for a guest
     */
    @Query("SELECT CASE WHEN COUNT(gpn) > 0 THEN true ELSE false END FROM GuestPhoneNumber gpn WHERE gpn.guest.id = :guestId AND gpn.phoneNumber = :phoneNumber")
    Boolean existsByGuestIdAndPhoneNumber(@Param("guestId") Long guestId, @Param("phoneNumber") String phoneNumber);

    /**
     * Check if a phone number already exists for any guest in the same event (excluding current guest)
     * Used to prevent duplicate phone numbers across all guests in a wedding
     */
    @Query("SELECT CASE WHEN COUNT(gpn) > 0 THEN true ELSE false END FROM GuestPhoneNumber gpn " +
           "WHERE gpn.phoneNumber = :phoneNumber AND gpn.guest.event.id = :eventId AND gpn.guest.id != :guestId")
    Boolean existsPhoneNumberInEvent(@Param("phoneNumber") String phoneNumber, @Param("eventId") Long eventId, @Param("guestId") Long guestId);

    /**
     * Get all phone numbers for a specific event (used for reporting/validation)
     */
    @Query("SELECT gpn FROM GuestPhoneNumber gpn WHERE gpn.guest.event.id = :eventId ORDER BY gpn.guest.id, gpn.isPrimary DESC")
    List<GuestPhoneNumber> findAllByEventId(@Param("eventId") Long eventId);

    /**
     * Find which guest already has this phone number in the event (for error messages)
     */
    @Query("SELECT gpn FROM GuestPhoneNumber gpn WHERE gpn.phoneNumber = :phoneNumber AND gpn.guest.event.id = :eventId")
    Optional<GuestPhoneNumber> findPhoneNumberInEvent(@Param("phoneNumber") String phoneNumber, @Param("eventId") Long eventId);
}
