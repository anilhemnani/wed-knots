package com.momentsmanager.repository;

import com.momentsmanager.model.Host;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HostRepository extends JpaRepository<Host, Long> {
    @Query("SELECT h FROM Host h WHERE h.event.id = :eventId")
    List<Host> findByEventId(@Param("eventId") Long eventId);

    Optional<Host> findByEmail(String email);

}
