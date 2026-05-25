/*
Coupling level: Content coupling
Reason why: Service builds the entity piece-by-piece using setters instead of letting the entity manage its state.
*/

/*
Coupling level: Control Coupling
Reason why: Math signs (>, <) or hardcoded Strings ("ADD", "DELETE") are used as control flags to branch logic inside the repository.
*/

package com.aims.service.impl;

import com.aims.dto.request.StockAdjustmentDto;
import com.aims.entity.Media;
import com.aims.entity.StockHistory;
import com.aims.exception.ResourceNotFoundException;
import com.aims.repository.MediaRepository;
import com.aims.repository.StockHistoryRepository;
import com.aims.service.StockHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StockHistoryServiceImpl implements StockHistoryService {

    private final StockHistoryRepository stockHistoryRepository;
    private final MediaRepository        mediaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StockHistory> getAllHistory() {
        return stockHistoryRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockHistory> getHistoryByMedia(Long mediaId) {
        return stockHistoryRepository.findByMedia_IdOrderByCreatedAtDesc(mediaId);
    }

    @Override
    public StockHistory recordAdjustment(StockAdjustmentDto dto) {
        Media media = mediaRepository.findById(dto.getMediaId())
                .orElseThrow(() -> new ResourceNotFoundException("Media", dto.getMediaId()));

        if (dto.getQuantityDelta() > 0) {
            media.restoreStock(dto.getQuantityDelta());
        } else if (dto.getQuantityDelta() < 0) {
            media.reduceStock(Math.abs(dto.getQuantityDelta()));
        }
        mediaRepository.save(media);

        StockHistory record = new StockHistory();
        record.setMedia(media);
        record.setQuantityDelta(dto.getQuantityDelta());
        record.setReason(dto.getReason());
        record.setPerformedBy(dto.getPerformedBy() != null ? dto.getPerformedBy() : "System");
        record.setActionType(dto.getQuantityDelta() >= 0 ? "MANUAL_ADD" : "MANUAL_REDUCE");

        return stockHistoryRepository.save(record);
    }
}
