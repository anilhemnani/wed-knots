package com.wedknots.repository;

import com.wedknots.model.InvitationPhoneRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationPhoneRecordRepository extends JpaRepository<InvitationPhoneRecord, Long> {

    /**
     * Find all phone records for a specific invitation log
     */
    List<InvitationPhoneRecord> findByInvitationLogId(Long invitationLogId);

    /**
     * Find all phone records for a specific invitation (across all logs)
     */
    @Query("SELECT ipr FROM InvitationPhoneRecord ipr " +
           "WHERE ipr.invitationLog.invitation.id = :invitationId " +
           "ORDER BY ipr.recordedAt DESC")
    List<InvitationPhoneRecord> findByInvitationId(@Param("invitationId") Long invitationId);

    /**
     * Find all phone records for a guest's invitation
     */
    @Query("SELECT ipr FROM InvitationPhoneRecord ipr " +
           "WHERE ipr.invitationLog.guest.id = :guestId " +
           "AND ipr.invitationLog.invitation.id = :invitationId")
    List<InvitationPhoneRecord> findByGuestAndInvitation(@Param("guestId") Long guestId,
                                                         @Param("invitationId") Long invitationId);

    /**
     * Check if a specific phone number was contacted for an invitation
     */
    @Query("SELECT CASE WHEN COUNT(ipr) > 0 THEN true ELSE false END FROM InvitationPhoneRecord ipr " +
           "WHERE ipr.invitationLog.id = :invitationLogId " +
           "AND ipr.guestPhoneNumber.id = :phoneNumberId")
    Boolean phoneNumberRecordedForLog(@Param("invitationLogId") Long invitationLogId,
                                       @Param("phoneNumberId") Long phoneNumberId);

    /**
     * Count how many phone numbers were contacted for an invitation
     */
    @Query("SELECT COUNT(DISTINCT ipr.guestPhoneNumber.id) FROM InvitationPhoneRecord ipr " +
           "WHERE ipr.invitationLog.invitation.id = :invitationId")
    Long countPhoneNumbersContactedForInvitation(@Param("invitationId") Long invitationId);

    /**
     * Get phone records by contact method for reporting
     */
    @Query("SELECT ipr FROM InvitationPhoneRecord ipr " +
           "WHERE ipr.invitationLog.invitation.id = :invitationId " +
           "AND ipr.contactMethod = :contactMethod")
    List<InvitationPhoneRecord> findByInvitationAndContactMethod(@Param("invitationId") Long invitationId,
                                                                  @Param("contactMethod") String contactMethod);

    /**
     * Get phone records by delivery status for reporting
     */
    @Query("SELECT ipr FROM InvitationPhoneRecord ipr " +
           "WHERE ipr.invitationLog.invitation.id = :invitationId " +
           "AND ipr.deliveryStatus = :status")
    List<InvitationPhoneRecord> findByInvitationAndDeliveryStatus(@Param("invitationId") Long invitationId,
                                                                   @Param("status") String status);
}

