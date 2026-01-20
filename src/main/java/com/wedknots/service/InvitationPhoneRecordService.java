package com.wedknots.service;

import com.wedknots.model.*;
import com.wedknots.repository.GuestPhoneNumberRepository;
import com.wedknots.repository.InvitationLogRepository;
import com.wedknots.repository.InvitationPhoneRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for recording which phone numbers of guests received invitations.
 * Tracks invitation delivery to specific phone numbers.
 */
@Service
public class InvitationPhoneRecordService {
    private static final Logger logger = LoggerFactory.getLogger(InvitationPhoneRecordService.class);

    @Autowired
    private InvitationPhoneRecordRepository invitationPhoneRecordRepository;

    @Autowired
    private InvitationLogRepository invitationLogRepository;

    @Autowired
    private GuestPhoneNumberRepository guestPhoneNumberRepository;

    /**
     * Record invitation sent to a specific guest's phone number
     */
    @Transactional
    public InvitationPhoneRecord recordInvitationForPhone(Long invitationLogId, Long phoneNumberId,
                                                          String contactMethod, String deliveryStatus) {
        Optional<InvitationLog> logOpt = invitationLogRepository.findById(invitationLogId);
        if (logOpt.isEmpty()) {
            throw new RuntimeException("Invitation log not found with id: " + invitationLogId);
        }

        Optional<GuestPhoneNumber> phoneOpt = guestPhoneNumberRepository.findById(phoneNumberId);
        if (phoneOpt.isEmpty()) {
            throw new RuntimeException("Guest phone number not found with id: " + phoneNumberId);
        }

        InvitationLog log = logOpt.get();
        GuestPhoneNumber phone = phoneOpt.get();

        // Create phone record
        InvitationPhoneRecord record = InvitationPhoneRecord.builder()
                .invitationLog(log)
                .guestPhoneNumber(phone)
                .phoneNumber(phone.getPhoneNumber())
                .isPrimary(phone.getIsPrimary())
                .phoneType(phone.getPhoneType().name())
                .contactMethod(contactMethod)
                .deliveryStatus(deliveryStatus)
                .recordedAt(LocalDateTime.now())
                .build();

        return invitationPhoneRecordRepository.save(record);
    }

    /**
     * Record invitation sent to all phone numbers of a guest
     */
    @Transactional
    public List<InvitationPhoneRecord> recordInvitationForAllGuestPhones(Long invitationLogId,
                                                                         String contactMethod,
                                                                         String deliveryStatus) {
        Optional<InvitationLog> logOpt = invitationLogRepository.findById(invitationLogId);
        if (logOpt.isEmpty()) {
            throw new RuntimeException("Invitation log not found with id: " + invitationLogId);
        }

        InvitationLog log = logOpt.get();
        Guest guest = log.getGuest();
        List<InvitationPhoneRecord> records = new ArrayList<>();

        // Record for each phone number of the guest
        if (guest.getPhoneNumbers() != null && !guest.getPhoneNumbers().isEmpty()) {
            for (GuestPhoneNumber phone : guest.getPhoneNumbers()) {
                InvitationPhoneRecord record = InvitationPhoneRecord.builder()
                        .invitationLog(log)
                        .guestPhoneNumber(phone)
                        .phoneNumber(phone.getPhoneNumber())
                        .isPrimary(phone.getIsPrimary())
                        .phoneType(phone.getPhoneType().name())
                        .contactMethod(contactMethod)
                        .deliveryStatus(deliveryStatus)
                        .recordedAt(LocalDateTime.now())
                        .build();
                records.add(invitationPhoneRecordRepository.save(record));
            }
        } else {
            logger.warn("Guest {} has no phone numbers", guest.getId());
        }

        return records;
    }

    /**
     * Record invitation sent to selected phone numbers of a guest
     */
    @Transactional
    public List<InvitationPhoneRecord> recordInvitationForSelectedPhones(Long invitationLogId,
                                                                         List<Long> phoneNumberIds,
                                                                         String contactMethod,
                                                                         String deliveryStatus) {
        Optional<InvitationLog> logOpt = invitationLogRepository.findById(invitationLogId);
        if (logOpt.isEmpty()) {
            throw new RuntimeException("Invitation log not found with id: " + invitationLogId);
        }

        InvitationLog log = logOpt.get();
        List<InvitationPhoneRecord> records = new ArrayList<>();

        // Record for each selected phone number
        for (Long phoneNumberId : phoneNumberIds) {
            Optional<GuestPhoneNumber> phoneOpt = guestPhoneNumberRepository.findById(phoneNumberId);
            if (phoneOpt.isPresent()) {
                GuestPhoneNumber phone = phoneOpt.get();
                InvitationPhoneRecord record = InvitationPhoneRecord.builder()
                        .invitationLog(log)
                        .guestPhoneNumber(phone)
                        .phoneNumber(phone.getPhoneNumber())
                        .isPrimary(phone.getIsPrimary())
                        .phoneType(phone.getPhoneType().name())
                        .contactMethod(contactMethod)
                        .deliveryStatus(deliveryStatus)
                        .recordedAt(LocalDateTime.now())
                        .build();
                records.add(invitationPhoneRecordRepository.save(record));
            } else {
                logger.warn("Phone number not found with id: {}", phoneNumberId);
            }
        }

        return records;
    }

    /**
     * Update delivery status of a phone record
     */
    @Transactional
    public InvitationPhoneRecord updateDeliveryStatus(Long phoneRecordId, String newStatus,
                                                      String errorMessage) {
        Optional<InvitationPhoneRecord> recordOpt = invitationPhoneRecordRepository.findById(phoneRecordId);
        if (recordOpt.isEmpty()) {
            throw new RuntimeException("Invitation phone record not found with id: " + phoneRecordId);
        }

        InvitationPhoneRecord record = recordOpt.get();
        record.setDeliveryStatus(newStatus);
        record.setErrorMessage(errorMessage);
        record.setDeliveryTimestamp(LocalDateTime.now());

        return invitationPhoneRecordRepository.save(record);
    }

    /**
     * Get all phone records for an invitation log
     */
    public List<InvitationPhoneRecord> getPhoneRecordsForLog(Long invitationLogId) {
        return invitationPhoneRecordRepository.findByInvitationLogId(invitationLogId);
    }

    /**
     * Get all phone records for an invitation (across all logs)
     */
    public List<InvitationPhoneRecord> getPhoneRecordsForInvitation(Long invitationId) {
        return invitationPhoneRecordRepository.findByInvitationId(invitationId);
    }

    /**
     * Get phone records for a guest's invitation
     */
    public List<InvitationPhoneRecord> getPhoneRecordsForGuestInvitation(Long guestId, Long invitationId) {
        return invitationPhoneRecordRepository.findByGuestAndInvitation(guestId, invitationId);
    }

    /**
     * Check if a phone number was recorded for an invitation log
     */
    public boolean wasPhoneNumberContacted(Long invitationLogId, Long phoneNumberId) {
        return Boolean.TRUE.equals(invitationPhoneRecordRepository.phoneNumberRecordedForLog(invitationLogId, phoneNumberId));
    }

    /**
     * Get statistics for an invitation
     */
    public Map<String, Object> getInvitationStatistics(Long invitationId) {
        List<InvitationPhoneRecord> records = getPhoneRecordsForInvitation(invitationId);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalPhoneRecords", records.size());
        stats.put("uniquePhoneNumbers", records.stream()
                .map(InvitationPhoneRecord::getPhoneNumber)
                .distinct()
                .count());

        // Count by delivery status
        Map<String, Long> statusCounts = records.stream()
                .collect(Collectors.groupingBy(InvitationPhoneRecord::getDeliveryStatus, Collectors.counting()));
        stats.put("deliveryStatusCounts", statusCounts);

        // Count by contact method
        Map<String, Long> methodCounts = records.stream()
                .collect(Collectors.groupingBy(InvitationPhoneRecord::getContactMethod, Collectors.counting()));
        stats.put("contactMethodCounts", methodCounts);

        // Count by phone type
        Map<String, Long> typeCounts = records.stream()
                .collect(Collectors.groupingBy(InvitationPhoneRecord::getPhoneType, Collectors.counting()));
        stats.put("phoneTypeCounts", typeCounts);

        // Primary vs secondary
        long primaryCount = records.stream()
                .filter(InvitationPhoneRecord::getIsPrimary)
                .count();
        stats.put("primaryPhoneRecords", primaryCount);
        stats.put("secondaryPhoneRecords", records.size() - primaryCount);

        return stats;
    }

    /**
     * Get phone records by delivery status for reporting
     */
    public List<InvitationPhoneRecord> getPhoneRecordsByStatus(Long invitationId, String status) {
        return invitationPhoneRecordRepository.findByInvitationAndDeliveryStatus(invitationId, status);
    }

    /**
     * Get phone records by contact method for reporting
     */
    public List<InvitationPhoneRecord> getPhoneRecordsByContactMethod(Long invitationId, String contactMethod) {
        return invitationPhoneRecordRepository.findByInvitationAndContactMethod(invitationId, contactMethod);
    }
}

