package com.aims.controller;

import com.aims.entity.HistoryLog;
import com.aims.service.HistoryLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HistoryLogController {

    private final HistoryLogService historyLogService;

    @GetMapping("/api/manager/history")
    public ResponseEntity<List<HistoryLog>> getHistoryLogs() {
        return ResponseEntity.ok(historyLogService.getLogs());
    }
}
