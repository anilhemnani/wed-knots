package com.wedknots.repository;

import com.wedknots.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    @Query("SELECT s FROM Supplier s WHERE s.event.id = :eventId ORDER BY s.name")
    List<Supplier> findByEventId(@Param("eventId") Long eventId);

    @Query("SELECT s FROM Supplier s LEFT JOIN FETCH s.items WHERE s.event.id = :eventId ORDER BY s.name")
    List<Supplier> findByEventIdWithItems(@Param("eventId") Long eventId);

    @Query("SELECT s FROM Supplier s WHERE s.event.id = :eventId AND LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Supplier> findByEventIdAndNameContaining(@Param("eventId") Long eventId, @Param("name") String name);

    @Query("SELECT s FROM Supplier s WHERE s.event.id = :eventId AND LOWER(s.city) = LOWER(:city)")
    List<Supplier> findByEventIdAndCity(@Param("eventId") Long eventId, @Param("city") String city);

    @Query("SELECT DISTINCT s.city FROM Supplier s WHERE s.event.id = :eventId AND s.city IS NOT NULL ORDER BY s.city")
    List<String> findDistinctCitiesByEventId(@Param("eventId") Long eventId);
}

