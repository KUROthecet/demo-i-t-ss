/*
Coupling level: Control Coupling
Reason why: Math signs (>, <) or hardcoded Strings ("ADD", "DELETE") are used as control flags to branch logic inside the repository.
*/

package com.aims.repository;

import com.aims.entity.StockHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockHistoryRepository extends JpaRepository<StockHistory, Long> {
    List<StockHistory> findByMedia_IdOrderByCreatedAtDesc(Long mediaId);
    List<StockHistory> findAllByOrderByCreatedAtDesc();
}
