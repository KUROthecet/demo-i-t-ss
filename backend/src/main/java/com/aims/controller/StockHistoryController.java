package com.aims.controller;

import com.aims.dto.request.StockAdjustmentDto;
import com.aims.entity.StockHistory;
import com.aims.service.StockHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock-history")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class StockHistoryController {

    private final StockHistoryService stockHistoryService;

    @GetMapping
    public ResponseEntity<List<StockHistory>> getAllHistory() {
        return ResponseEntity.ok(stockHistoryService.getAllHistory());
    }

    @GetMapping("/media/{id}")
    public ResponseEntity<List<StockHistory>> getHistoryByMedia(@PathVariable Long id) {
        return ResponseEntity.ok(stockHistoryService.getHistoryByMedia(id));
    }

    @PostMapping("/adjust")
    public ResponseEntity<StockHistory> adjustStock(@Valid @RequestBody StockAdjustmentDto dto) {
        return ResponseEntity.ok(stockHistoryService.recordAdjustment(dto));
    }
}
