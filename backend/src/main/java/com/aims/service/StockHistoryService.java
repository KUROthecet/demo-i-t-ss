package com.aims.service;

import com.aims.dto.request.StockAdjustmentDto;
import com.aims.entity.Media;
import com.aims.entity.StockHistory;
import com.aims.exception.ResourceNotFoundException;
import com.aims.repository.MediaRepository;
import com.aims.repository.StockHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class StockHistoryService {

    private final StockHistoryRepository stockHistoryRepository;
    private final MediaRepository        mediaRepository;

    @Transactional(readOnly = true)
    public List<StockHistory> getAllHistory() {
        return stockHistoryRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<StockHistory> getHistoryByMedia(Long mediaId) {
        return stockHistoryRepository.findByMedia_IdOrderByCreatedAtDesc(mediaId);
    }

    public StockHistory recordAdjustment(StockAdjustmentDto dto) {
        Optional<Media> foundMedia = mediaRepository.findById(dto.getMediaId());
        if (foundMedia.isEmpty()) {
            throw new ResourceNotFoundException("Media", dto.getMediaId());
        }
        Media media = foundMedia.get();

        if (dto.getQuantityDelta() > 0) {
            media.restoreStock(dto.getQuantityDelta());
        } else if (dto.getQuantityDelta() < 0) {
            media.reduceStock(Math.abs(dto.getQuantityDelta()));
        }
        mediaRepository.save(media);

        return recordHistory(media, dto.getQuantityDelta(), dto.getReason(),
                dto.getPerformedBy() != null ? dto.getPerformedBy() : "System");
    }

    public StockHistory recordHistory(Media media, int delta, String reason, String performedBy) {
        return recordHistory(media, delta, reason, performedBy, delta >= 0 ? "MANUAL_ADD" : "MANUAL_REDUCE");
    }

    public StockHistory recordHistory(Media media, int delta, String reason, String performedBy, String actionType) {
        StockHistory record = new StockHistory();
        record.setMedia(media);
        record.setQuantityDelta(delta);
        record.setReason(reason);
        record.setPerformedBy(performedBy);
        record.setActionType(actionType);
        return stockHistoryRepository.save(record);
    }
}
