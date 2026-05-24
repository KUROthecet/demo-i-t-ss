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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Implementation of {@link MediaService} providing complete product catalog management.
 *
 * <p>Enforces all AIMS business rules:
 * <ul>
 *   <li>Price must be 30%–150% of original price (delegated to {@link Media#updatePrice(int)})</li>
 *   <li>Batch deletion limited to 10 items per operation</li>
 *   <li>Daily deletion limit of 20 items (DELETE + DEACTIVATE)</li>
 *   <li>Items with stock > 0 are deactivated rather than deleted</li>
 * </ul>
 *
 * <p>SOLID Design:
 * <ul>
 *   <li>SRP: price validation is delegated to the {@code Media} entity via {@code updatePrice()},
 *       following the "Tell Don't Ask" principle — no duplicate validation logic here.</li>
 *   <li>OCP: history logging is handled by {@code saveHistoryLog()} — adding new action types
 *       requires no modification to the save/update/delete methods.</li>
 *   <li>DIP: depends on {@code MediaRepository} interface, not its JPA implementation.</li>
 * </ul>
 *
 * @author AIMS Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MediaServiceImpl implements MediaService {

    private final MediaRepository        mediaRepository;
    private final HistoryLogRepository   historyLogRepository;

    private static final int MAX_BATCH_DELETE = 10;
    private static final int MAX_DAILY_DELETE = 20;

    /**
     * {@inheritDoc}
     *
     * <p>Shuffles all active media in-memory and returns a random subset.</p>
     */
    @Override
    @Transactional(readOnly = true)
    public List<Media> getRandomMedia(int limit) {
        log.debug("Fetching {} random active media items", limit);
        List<Media> allActive = mediaRepository.findByStatus(MediaStatus.ACTIVE);
        java.util.Collections.shuffle(allActive);
        return allActive.stream().limit(limit).collect(java.util.stream.Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Media getMediaById(Long id) {
        log.debug("Fetching media by ID: {}", id);
        return mediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with ID: " + id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Media> searchMedia(String query, int minPrice, int maxPrice, Pageable pageable) {
        log.debug("Searching media: query={}, minPrice={}, maxPrice={}, pageable={}", query, minPrice, maxPrice, pageable);
        return mediaRepository.searchByTitleOrCategory(query, minPrice, maxPrice, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getCatalogStats() {
        Map<String, Long> stats = new HashMap<>();
        List<Object[]> results = mediaRepository.countByCategory();
        for (Object[] row : results) {
            String category = (String) row[0];
            Long count = (Long) row[1];
            stats.put(category, count);
        }
        return stats;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Price validation is delegated to {@link Media#updatePrice(int)}, which
     * enforces the 30%–150% rule and throws {@code BusinessException} if violated.
     * This follows the "Tell Don't Ask" principle — the entity owns its own invariants.</p>
     */
    @Override
    public Media addMedia(Media media, String performedBy) {
        log.info("Adding new media: '{}', performed by: {}", media.getTitle(), performedBy);

        // Delegate price range enforcement to the entity's own method (Tell Don't Ask)
        media.updatePrice(media.getCurrentPrice());

        if (media.getStatus() == null) {
            media.setStatus(MediaStatus.ACTIVE);
        }

        Media saved = mediaRepository.save(media);
        saveHistoryLog("ADD", saved.getBarcode(), performedBy,
                "Added new media: " + saved.getTitle() + " (ID: " + saved.getId() + ")");
        log.info("Successfully added media ID: {}", saved.getId());
        return saved;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates price validation to the entity's {@code updateDetails} method,
     * which internally calls {@code updatePrice} with the 30%–150% check.</p>
     */
    @Override
    public Media updateMedia(Long id, Media updated, String performedBy) {
        log.info("Updating media ID: {}, performed by: {}", id, performedBy);
        Media existing = getMediaById(id);

        String oldTitle = existing.getTitle();
        int oldPrice    = existing.getCurrentPrice();

        existing.updateDetails(updated);
        Media saved = mediaRepository.save(existing);

        saveHistoryLog("UPDATE", saved.getBarcode(), performedBy,
                String.format("Updated media '%s' → '%s'. Price: %d → %d",
                        oldTitle, saved.getTitle(), oldPrice, saved.getCurrentPrice()));
        log.info("Successfully updated media ID: {}", id);
        return saved;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Applies the following logic per item:
     * <ul>
     *   <li>Items with zero stock → permanently deleted</li>
     *   <li>Items with positive stock → deactivated (soft delete)</li>
     * </ul>
     */
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

    /**
     * {@inheritDoc}
     *
     * <p>Counts DELETE and DEACTIVATE entries in the history log created since midnight today.</p>
     */
    @Override
    @Transactional(readOnly = true)
    public int getDailyDeleteCount() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        long count = historyLogRepository.countByActionTypeInAndCreatedAtAfter(
                List.of("DELETE", "DEACTIVATE"), startOfDay);
        return (int) count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<HistoryLog> getHistoryLogs() {
        return historyLogRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Saves a history log entry for any product management action.
     */
    private void saveHistoryLog(String actionType, String barcode, String performedBy, String details) {
        HistoryLog entry = new HistoryLog();
        entry.setProductBarcode(barcode);
        entry.setActionType(actionType);
        entry.setDetails(details);
        entry.setPerformedBy(performedBy != null ? performedBy : "System");
        historyLogRepository.save(entry);
    }
}
