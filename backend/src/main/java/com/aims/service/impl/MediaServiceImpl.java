package com.aims.service.impl;

import com.aims.entity.HistoryLog;
import com.aims.entity.Media;
import com.aims.enums.MediaStatus;
import com.aims.exception.ResourceNotFoundException;
import com.aims.repository.HistoryLogRepository;
import com.aims.repository.MediaRepository;
import com.aims.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MediaServiceImpl implements MediaService {

    private final MediaRepository      mediaRepository;
    private final HistoryLogRepository historyLogRepository;

    private static final int MAX_BATCH_DELETE = 10;
    private static final int MAX_DAILY_DELETE = 20;

    @Override
    @Transactional(readOnly = true)
    public List<Media> getRandomMedia(int limit) {
        log.debug("Fetching {} random active media items", limit);
        // Two-step: native RANDOM() picks IDs, findAllById loads full JOINED entities
        List<Long> randomIds = mediaRepository.findRandomActiveIds(limit);
        return mediaRepository.findAllById(randomIds);
    }

    @Override
    @Transactional(readOnly = true)
    public Media getMediaById(Long id) {
        log.debug("Fetching media by ID: {}", id);
        return mediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Media> searchMedia(String query, int minPrice, int maxPrice, int limit) {
        log.debug("Searching media: query={}, min={}, max={}, limit={}", query, minPrice, maxPrice, limit);
        if (limit >= Integer.MAX_VALUE) {
            return mediaRepository.searchByTitleOrCategory(query, minPrice, maxPrice);
        }
        return mediaRepository.searchByTitleOrCategoryLimited(
            query, minPrice, maxPrice, PageRequest.of(0, limit, Sort.by("title").ascending())
        );
    }

    @Override
    public Media addMedia(Media media, String performedBy) {
        log.info("Adding new media: '{}', performed by: {}", media.getTitle(), performedBy);
        media.updatePrice(media.getCurrentPrice());
        if (media.getStatus() == null) media.setStatus(MediaStatus.ACTIVE);

        Media saved = mediaRepository.save(media);
        saveHistoryLog("ADD", saved.getBarcode(), performedBy,
                "Added new media: " + saved.getTitle() + " (ID: " + saved.getId() + ")");
        log.info("Successfully added media ID: {}", saved.getId());
        return saved;
    }

    @Override
    public Media updateMedia(Long id, Media updated, String performedBy) {
        log.info("Updating media ID: {}, performed by: {}", id, performedBy);
        Media existing = getMediaById(id);

        String oldTitle = existing.getTitle();
        int    oldPrice = existing.getCurrentPrice();

        existing.updateDetails(updated);
        Media saved = mediaRepository.save(existing);

        saveHistoryLog("UPDATE", saved.getBarcode(), performedBy,
                String.format("Updated media '%s' → '%s'. Price: %d → %d",
                        oldTitle, saved.getTitle(), oldPrice, saved.getCurrentPrice()));
        log.info("Successfully updated media ID: {}", id);
        return saved;
    }

    @Override
    public void deleteMedia(List<Long> ids, String performedBy) {
        log.info("Delete/deactivate request for {} items, performed by: {}", ids.size(), performedBy);

        if (ids.size() > MAX_BATCH_DELETE) {
            throw new com.aims.exception.BusinessException(
                    "Cannot delete more than " + MAX_BATCH_DELETE + " items at once. Got: " + ids.size());
        }

        int currentDailyCount = getDailyDeleteCount();
        if (currentDailyCount + ids.size() > MAX_DAILY_DELETE) {
            throw new com.aims.exception.BusinessException(
                    String.format("Daily deletion limit exceeded. Already deleted %d today, limit is %d.",
                            currentDailyCount, MAX_DAILY_DELETE));
        }

        for (Long id : ids) {
            Media media = getMediaById(id);
            if (media.canBeDeleted()) {
                mediaRepository.delete(media);
                saveHistoryLog("DELETE", media.getBarcode(), performedBy,
                        "Permanently deleted media: " + media.getTitle() + " (zero stock)");
                log.info("Permanently deleted media ID: {}", id);
            } else {
                media.deactivate();
                mediaRepository.save(media);
                saveHistoryLog("DEACTIVATE", media.getBarcode(), performedBy,
                        "Deactivated media: " + media.getTitle() +
                        " (stock: " + media.getQuantityInStock() + ")");
                log.info("Deactivated media ID: {} (stock: {})", id, media.getQuantityInStock());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int getDailyDeleteCount() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        long count = historyLogRepository.countByActionTypeInAndCreatedAtAfter(
                List.of("DELETE", "DEACTIVATE"), startOfDay);
        return (int) count;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoryLog> getHistoryLogs() {
        return historyLogRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getCatalogStats() {
        List<Object[]> rows = mediaRepository.countActiveByCategory();
        Map<String, Long> categoryCounts = new LinkedHashMap<>();
        for (Object[] row : rows) categoryCounts.put((String) row[0], (Long) row[1]);

        Integer maxPrice = mediaRepository.findMaxCurrentPrice();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("categoryCounts", categoryCounts);
        stats.put("maxPrice", maxPrice != null ? maxPrice : 10_000_000);
        return stats;
    }

    private void saveHistoryLog(String actionType, String barcode, String performedBy, String details) {
        HistoryLog entry = new HistoryLog();
        entry.setProductBarcode(barcode);
        entry.setActionType(actionType);
        entry.setDetails(details);
        entry.setPerformedBy(performedBy != null ? performedBy : "System");
        historyLogRepository.save(entry);
    }
}
