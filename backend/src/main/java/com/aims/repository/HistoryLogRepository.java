package com.aims.repository;

import com.aims.entity.HistoryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoryLogRepository extends JpaRepository<HistoryLog, Long> {

    List<HistoryLog> findAllByOrderByCreatedAtDesc();

    List<HistoryLog> findByActionTypeOrderByCreatedAtDesc(String actionType);

    // Enforces the daily delete limit (max 20 per day)
    long countByActionTypeInAndCreatedAtAfter(List<String> actionType, LocalDateTime after);
}
