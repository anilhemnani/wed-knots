package com.wedknots.repository;

import com.wedknots.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    @Query("SELECT i FROM Invitation i WHERE i.event.id = :eventId")
    List<Invitation> findByEventId(@Param("eventId") Long eventId);

    @Query("SELECT i FROM Invitation i WHERE i.event.id = :eventId AND i.status = :status")
    List<Invitation> findByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") String status);

    @Query("SELECT i FROM Invitation i WHERE i.event.id = :eventId ORDER BY i.createdAt DESC")
    List<Invitation> findByEventIdOrderByCreatedAtDesc(@Param("eventId") Long eventId);
}

