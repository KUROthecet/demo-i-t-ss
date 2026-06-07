package com.aims.service;

import com.aims.entity.HistoryLog;
import com.aims.repository.HistoryLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HistoryLogService {

    private final HistoryLogRepository historyLogRepository;

    public void log(String actionType, String reference, String performedBy, String details) {
        HistoryLog entry = new HistoryLog();
        entry.setActionType(actionType);
        entry.setProductBarcode(reference);
        entry.setPerformedBy(performedBy != null ? performedBy : "System");
        entry.setDetails(details);
        historyLogRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public List<HistoryLog> getLogs() {
        return historyLogRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public int countDailyDeletions() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        long count = historyLogRepository.countByActionTypeInAndCreatedAtAfter(
                List.of("DELETE", "DEACTIVATE"), startOfDay);
        return (int) count;
    }
}
