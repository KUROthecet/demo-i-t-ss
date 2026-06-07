package com.aims.service;

import com.aims.entity.Media;
import com.aims.enums.MediaStatus;
import com.aims.exception.BusinessException;
import com.aims.exception.ResourceNotFoundException;
import com.aims.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MediaService {

    private final MediaRepository   mediaRepository;
    private final HistoryLogService historyLogService;

    private static final int MAX_BATCH_DELETE = 10;
    private static final int MAX_DAILY_DELETE = 20;

    @Transactional(readOnly = true)
    public List<Media> getRandomMedia(int limit) {
        List<Media> allActive = mediaRepository.findByStatus(MediaStatus.ACTIVE);
        Collections.shuffle(allActive);
        int end = Math.min(limit, allActive.size());
        return new ArrayList<>(allActive.subList(0, end));
    }

    @Transactional(readOnly = true)
    public Media getMediaById(Long id) {
        Optional<Media> found = mediaRepository.findById(id);
        if (found.isEmpty()) {
            throw new ResourceNotFoundException("Media not found with ID: " + id);
        }
        return found.get();
    }

    @Transactional(readOnly = true)
    public Page<Media> searchMedia(String query, List<String> categories, int minPrice, int maxPrice, Pageable pageable) {
        List<String> validCategories = (categories == null || categories.isEmpty()) ? null : categories;
        return mediaRepository.searchByTitleOrCategory(query, validCategories, minPrice, maxPrice, pageable);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getCatalogStats() {
        Map<String, Long> stats = new HashMap<>();
        List<Object[]> results = mediaRepository.countByCategory();
        for (Object[] row : results) {
            stats.put((String) row[0], (Long) row[1]);
        }
        return stats;
    }

    public Media addMedia(Media media, String performedBy) {
        media.updatePrice(media.getCurrentPrice());
        if (media.getStatus() == null) {
            media.setStatus(MediaStatus.ACTIVE);
        }
        Media saved = mediaRepository.save(media);
        historyLogService.log("ADD", saved.getBarcode(), performedBy,
                "Added new media: " + saved.getTitle() + " (ID: " + saved.getId() + ")");
        return saved;
    }

    public Media updateMedia(Long id, Media updated, String performedBy) {
        Media existing = getMediaById(id);
        String oldTitle = existing.getTitle();
        int oldPrice    = existing.getCurrentPrice();
        existing.updateDetails(updated);
        Media saved = mediaRepository.save(existing);
        historyLogService.log("UPDATE", saved.getBarcode(), performedBy,
                String.format("Updated media '%s' → '%s'. Price: %d → %d",
                        oldTitle, saved.getTitle(), oldPrice, saved.getCurrentPrice()));
        return saved;
    }

    public void deleteMedia(List<Long> ids, String performedBy) {
        if (ids.size() > MAX_BATCH_DELETE) {
            throw new BusinessException(
                    "Cannot delete more than " + MAX_BATCH_DELETE + " items at once. Got: " + ids.size());
        }
        int currentDailyCount = getDailyDeleteCount();
        if (currentDailyCount + ids.size() > MAX_DAILY_DELETE) {
            throw new BusinessException(
                    String.format("Daily deletion limit exceeded. Already deleted %d today, limit is %d.",
                            currentDailyCount, MAX_DAILY_DELETE));
        }
        for (Long id : ids) {
            Media media = getMediaById(id);
            if (media.canBeDeleted()) {
                mediaRepository.delete(media);
                historyLogService.log("DELETE", media.getBarcode(), performedBy,
                        "Permanently deleted media: " + media.getTitle() + " (zero stock)");
            } else {
                media.deactivate();
                mediaRepository.save(media);
                historyLogService.log("DEACTIVATE", media.getBarcode(), performedBy,
                        "Deactivated media: " + media.getTitle() +
                        " (stock: " + media.getQuantityInStock() + ")");
            }
        }
    }

    @Transactional(readOnly = true)
    public int getDailyDeleteCount() {
        return historyLogService.countDailyDeletions();
    }

    public Media validateAndDeductStock(Long mediaId, int quantity) {
        Media media = getMediaById(mediaId);
        if (!media.isAvailable()) {
            throw new BusinessException("Media '" + media.getTitle() + "' is not available for purchase.");
        }
        media.reduceStock(quantity);
        return mediaRepository.save(media);
    }

    @Transactional(readOnly = true)
    public List<Media> getSimilarMedia(Long mediaId) {
        Media current = getMediaById(mediaId);
        return mediaRepository.findSimilarByCategory(
                current.getCategory(), mediaId, current.getCurrentPrice(), PageRequest.of(0, 6));
    }

    public void restoreStock(Long mediaId, int quantity) {
        Media media = mediaRepository.findById(mediaId).orElse(null);
        if (media != null) {
            media.restoreStock(quantity);
            mediaRepository.save(media);
        }
    }
}
