package com.aims.service;

import com.aims.dto.request.StockAdjustmentDto;
import com.aims.entity.StockHistory;

import java.util.List;

public interface StockHistoryService {
    List<StockHistory> getAllHistory();
    List<StockHistory> getHistoryByMedia(Long mediaId);
    StockHistory recordAdjustment(StockAdjustmentDto dto);
}
