package com.wedknots.repository;

import com.wedknots.model.ActivityType;
import com.wedknots.model.EventActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventActivityRepository extends JpaRepository<EventActivity, Long> {

    @Query("SELECT a FROM EventActivity a WHERE a.event.id = :eventId ORDER BY a.startTime, a.sortOrder, a.name")
    List<EventActivity> findByEventId(@Param("eventId") Long eventId);

    @Query("SELECT a FROM EventActivity a WHERE a.event.id = :eventId AND a.visibleToGuests = true ORDER BY a.startTime, a.sortOrder, a.name")
    List<EventActivity> findByEventIdAndVisibleToGuests(@Param("eventId") Long eventId);

    @Query("SELECT a FROM EventActivity a WHERE a.event.id = :eventId AND a.activityType = :type ORDER BY a.startTime, a.sortOrder, a.name")
    List<EventActivity> findByEventIdAndType(@Param("eventId") Long eventId, @Param("type") ActivityType type);

    @Query("SELECT a FROM EventActivity a WHERE a.event.id = :eventId AND a.startTime >= :start AND a.startTime <= :end ORDER BY a.startTime, a.sortOrder")
    List<EventActivity> findByEventIdAndDateRange(@Param("eventId") Long eventId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT a FROM EventActivity a WHERE a.event.id = :eventId AND a.startTime >= :now ORDER BY a.startTime LIMIT 5")
    List<EventActivity> findUpcomingActivities(@Param("eventId") Long eventId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(a) FROM EventActivity a WHERE a.event.id = :eventId")
    Long countByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(a) FROM EventActivity a WHERE a.event.id = :eventId AND a.activityType = :type")
    Long countByEventIdAndType(@Param("eventId") Long eventId, @Param("type") ActivityType type);
}

