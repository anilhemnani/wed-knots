package com.wedknots.repository;

import com.wedknots.model.EventItem;
import com.wedknots.model.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventItemRepository extends JpaRepository<EventItem, Long> {

    @Query("SELECT i FROM EventItem i LEFT JOIN FETCH i.supplier LEFT JOIN FETCH i.neededForActivity WHERE i.event.id = :eventId ORDER BY i.neededByDate, i.name")
    List<EventItem> findByEventId(@Param("eventId") Long eventId);

    @Query("SELECT i FROM EventItem i LEFT JOIN FETCH i.supplier LEFT JOIN FETCH i.neededForActivity WHERE i.event.id = :eventId AND i.supplier.id = :supplierId ORDER BY i.neededByDate, i.name")
    List<EventItem> findByEventIdAndSupplierId(@Param("eventId") Long eventId, @Param("supplierId") Long supplierId);

    @Query("SELECT i FROM EventItem i LEFT JOIN FETCH i.supplier LEFT JOIN FETCH i.neededForActivity WHERE i.event.id = :eventId AND i.status = :status ORDER BY i.neededByDate, i.name")
    List<EventItem> findByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") ItemStatus status);

    @Query("SELECT i FROM EventItem i LEFT JOIN FETCH i.supplier LEFT JOIN FETCH i.neededForActivity WHERE i.event.id = :eventId AND LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY i.neededByDate, i.name")
    List<EventItem> findByEventIdAndNameContaining(@Param("eventId") Long eventId, @Param("name") String name);

    @Query("SELECT i FROM EventItem i LEFT JOIN FETCH i.supplier LEFT JOIN FETCH i.neededForActivity WHERE i.event.id = :eventId AND i.neededByDate <= :date AND i.status NOT IN ('DELIVERED', 'NOT_NEEDED') ORDER BY i.neededByDate, i.name")
    List<EventItem> findOverdueItems(@Param("eventId") Long eventId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(i) FROM EventItem i WHERE i.event.id = :eventId AND i.status = :status")
    Long countByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") ItemStatus status);

    @Query("SELECT i FROM EventItem i LEFT JOIN FETCH i.supplier LEFT JOIN FETCH i.neededForActivity WHERE i.supplier.id = :supplierId ORDER BY i.neededByDate, i.name")
    List<EventItem> findBySupplierId(@Param("supplierId") Long supplierId);

    @Query("SELECT i FROM EventItem i LEFT JOIN FETCH i.supplier LEFT JOIN FETCH i.neededForActivity WHERE i.neededForActivity.id = :activityId ORDER BY i.neededByDate, i.name")
    List<EventItem> findByNeededForActivityId(@Param("activityId") Long activityId);

    @Query("SELECT COALESCE(SUM(i.totalPrice), 0) FROM EventItem i WHERE i.event.id = :eventId")
    java.math.BigDecimal getTotalCostByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COALESCE(SUM(i.totalPrice), 0) FROM EventItem i WHERE i.event.id = :eventId AND i.status = :status")
    java.math.BigDecimal getTotalCostByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") ItemStatus status);
}

