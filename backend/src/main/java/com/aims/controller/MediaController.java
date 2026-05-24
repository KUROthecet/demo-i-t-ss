package com.aims.controller;

import com.aims.entity.HistoryLog;
import com.aims.entity.Media;
import com.aims.service.MediaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class MediaController {

    private final MediaService mediaService;

    @GetMapping("/api/products")
    public ResponseEntity<List<Media>> getProducts(@RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(mediaService.getRandomMedia(limit));
    }

    @GetMapping("/api/products/{id}")
    public ResponseEntity<Media> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(mediaService.getMediaById(id));
    }

    @GetMapping("/api/products/search")
    public ResponseEntity<List<Media>> searchProducts(
            @RequestParam(defaultValue = "")           String query,
            @RequestParam(defaultValue = "0")          int    minPrice,
            @RequestParam(defaultValue = "2147483647") int    maxPrice,
            @RequestParam(defaultValue = "200")        int    limit) {
        int effectiveLimit = (limit <= 0) ? Integer.MAX_VALUE : limit;
        return ResponseEntity.ok(mediaService.searchMedia(query, minPrice, maxPrice, effectiveLimit));
    }

    @GetMapping("/api/products/catalog-stats")
    public ResponseEntity<Map<String, Object>> getCatalogStats() {
        return ResponseEntity.ok(mediaService.getCatalogStats());
    }

    @PostMapping("/api/products")
    public ResponseEntity<Media> addMedia(
            @Valid @RequestBody Media media,
            @RequestHeader(value = "X-Performed-By", defaultValue = "System") String performedBy) {
        return ResponseEntity.ok(mediaService.addMedia(media, performedBy));
    }

    @PutMapping("/api/products/{id}")
    public ResponseEntity<Media> updateMedia(
            @PathVariable Long id,
            @Valid @RequestBody Media media,
            @RequestHeader(value = "X-Performed-By", defaultValue = "System") String performedBy) {
        return ResponseEntity.ok(mediaService.updateMedia(id, media, performedBy));
    }

    @DeleteMapping("/api/products")
    public ResponseEntity<Map<String, String>> deleteMedia(
            @RequestBody List<Long> ids,
            @RequestHeader(value = "X-Performed-By", defaultValue = "System") String performedBy) {
        mediaService.deleteMedia(ids, performedBy);
        return ResponseEntity.ok(Map.of("message", "Products processed successfully"));
    }

    @GetMapping("/api/media/daily-delete-count")
    public ResponseEntity<Map<String, Integer>> getDailyDeleteCount() {
        int count = mediaService.getDailyDeleteCount();
        return ResponseEntity.ok(Map.of("count", count, "remaining", 20 - count));
    }

    @GetMapping("/api/manager/history")
    public ResponseEntity<List<HistoryLog>> getHistoryLogs() {
        return ResponseEntity.ok(mediaService.getHistoryLogs());
    }
}
