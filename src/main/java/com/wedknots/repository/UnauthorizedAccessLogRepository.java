package com.wedknots.repository;

import com.wedknots.model.UnauthorizedAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnauthorizedAccessLogRepository extends JpaRepository<UnauthorizedAccessLog, Long> {
    List<UnauthorizedAccessLog> findTop100ByOrderByCreatedAtDesc();
}
