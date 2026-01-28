package com.wedknots.repository;

import com.wedknots.model.MessageDeliveryQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageDeliveryQueueRepository extends JpaRepository<MessageDeliveryQueue, Long> {

    /**
     * Find pending messages ready for processing
     */
    @Query("SELECT m FROM MessageDeliveryQueue m WHERE m.status = 'PENDING' " +
           "AND (m.scheduledAt IS NULL OR m.scheduledAt <= :now) " +
           "ORDER BY m.priority DESC, m.createdAt ASC")
    List<MessageDeliveryQueue> findPendingMessages(@Param("now") LocalDateTime now);

    /**
     * Find messages ready for retry
     */
    @Query("SELECT m FROM MessageDeliveryQueue m WHERE m.status = 'RETRY' " +
           "AND m.nextRetryAt <= :now " +
           "AND m.retryCount < m.maxRetries " +
           "ORDER BY m.priority DESC, m.nextRetryAt ASC")
    List<MessageDeliveryQueue> findMessagesForRetry(@Param("now") LocalDateTime now);

    /**
     * Find messages by status
     */
    List<MessageDeliveryQueue> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * Find messages by message ID
     */
    Optional<MessageDeliveryQueue> findByMessageId(String messageId);

    /**
     * Find all messages for a guest
     */
    List<MessageDeliveryQueue> findByGuestIdOrderByCreatedAtDesc(Long guestId);

    /**
     * Find all messages for an event
     */
    List<MessageDeliveryQueue> findByEventIdOrderByCreatedAtDesc(Long eventId);

    /**
     * Count pending messages
     */
    long countByStatus(String status);

    /**
     * Find stuck messages (processing for too long)
     */
    @Query("SELECT m FROM MessageDeliveryQueue m WHERE m.status = 'PROCESSING' " +
           "AND m.processingStartedAt < :cutoffTime")
    List<MessageDeliveryQueue> findStuckMessages(@Param("cutoffTime") LocalDateTime cutoffTime);
}

