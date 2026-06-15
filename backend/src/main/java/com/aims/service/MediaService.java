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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MediaService {

    private static final String RANDOM_CACHE_PREFIX = "media:random:";

    private final MediaRepository       mediaRepository;
    private final HistoryLogService     historyLogService;
    private final StockHistoryService   stockHistoryService;
    private final StringRedisTemplate   redisTemplate;

    private static final int MAX_BATCH_DELETE = 10;
    private static final int MAX_DAILY_DELETE = 20;

    @Transactional(readOnly = true)
    public List<Media> getRandomMedia(int limit) {
        String cacheKey = RANDOM_CACHE_PREFIX + limit;
        String cached   = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            List<Long> ids = Arrays.stream(cached.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            List<Media> result = new ArrayList<>(mediaRepository.findAllById(ids));
            if (!result.isEmpty()) return result;
        }

        List<Media> available = mediaRepository.findByStatus(MediaStatus.ACTIVE)
                .stream()
                .filter(Media::isAvailable)
                .collect(Collectors.toList());
        Collections.shuffle(available);
        List<Media> result = available.subList(0, Math.min(limit, available.size()));

        String idsCsv = result.stream()
                .map(m -> m.getId().toString())
                .collect(Collectors.joining(","));
        redisTemplate.opsForValue().set(cacheKey, idsCsv, 60, TimeUnit.SECONDS);

        return new ArrayList<>(result);
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
    public Page<Media> getManagerProducts(String query, List<String> categories, int minPrice, int maxPrice, Pageable pageable) {
        List<String> validCategories = (categories == null || categories.isEmpty()) ? null : categories;
        return mediaRepository.searchAllByTitleOrCategory(query, validCategories, minPrice, maxPrice, pageable);
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
        Media existing  = getMediaById(id);
        String oldTitle = existing.getTitle();
        int oldPrice    = existing.getCurrentPrice();
        int oldStock    = existing.getQuantityInStock();
        existing.updateDetails(updated);
        Media saved = mediaRepository.save(existing);
        historyLogService.log("UPDATE", saved.getBarcode(), performedBy,
                String.format("Updated media '%s' → '%s'. Price: %d → %d",
                        oldTitle, saved.getTitle(), oldPrice, saved.getCurrentPrice()));
        int delta = saved.getQuantityInStock() - oldStock;
        if (delta != 0) {
            stockHistoryService.recordHistory(saved, delta, "Stock updated via Edit Product",
                    performedBy != null ? performedBy : "System");
        }
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

    @Transactional(readOnly = true)
    public Map<Long, Integer> getStockBatch(List<Long> ids) {
        Map<Long, Integer> result = new HashMap<>();
        for (Long id : ids) {
            mediaRepository.findById(id).ifPresent(m -> result.put(m.getId(), m.getQuantityInStock()));
        }
        return result;
    }

    public Media validateAndDeductStock(Long mediaId, int quantity) {
        Media media = getMediaById(mediaId);
        if (!media.isAvailable()) {
            throw new BusinessException("Media '" + media.getTitle() + "' is not available for purchase.");
        }
        media.reduceStock(quantity);
        Media saved = mediaRepository.save(media);
        stockHistoryService.recordHistory(saved, -quantity,
                "Deducted by order placement", "SYSTEM", "ORDER_DEDUCTION");
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Media> getSimilarMedia(Long mediaId) {
        Media current = getMediaById(mediaId);
        return mediaRepository.findSimilarByCategory(
                current.getCategory(), mediaId, current.getCurrentPrice(), PageRequest.of(0, 6));
    }

    public Media reactivateMedia(Long id, String performedBy) {
        Media media = getMediaById(id);
        media.reactivate();
        Media saved = mediaRepository.save(media);
        historyLogService.log("ACTIVATE", saved.getBarcode(), performedBy,
                "Re-activated media: " + saved.getTitle() + " (ID: " + saved.getId() + ")");
        return saved;
    }

    public void restoreStock(Long mediaId, int quantity) {
        Media media = mediaRepository.findById(mediaId).orElse(null);
        if (media == null) return;
        media.restoreStock(quantity);
        Media saved = mediaRepository.save(media);
        stockHistoryService.recordHistory(saved, quantity,
                "Restored by order cancellation/rejection", "SYSTEM", "ORDER_RESTORE");
    }
}
