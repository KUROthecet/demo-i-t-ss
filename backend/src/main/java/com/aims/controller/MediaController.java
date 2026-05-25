// Stamp Coupling
// The backend API endpoints accept the entire Media entity as the request body. 
// This full object is then passed directly from MediaController to MediaServiceImpl.addMedia() 
// and updateMedia(). However, the service methods only use a subset of fields 
// (title, currentPrice, originalValue, barcode, status, quantityInStock) while ignoring many others.

package com.aims.controller;

import com.aims.entity.HistoryLog;
import com.aims.entity.Media;
import com.aims.service.MediaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * MediaController — REST API for product browsing and management.
 *
 * <p>SRP: this controller is the sole entry point for HTTP requests related to Media.
 * All business logic and data access is delegated to {@link MediaService}.
 * The controller does not inject any repository directly.</p>
 *
 * <ul>
 *   <li>Public endpoints (no auth): GET /api/products, GET /api/products/{id}, GET /api/products/search</li>
 *   <li>Manager endpoints: POST, PUT, DELETE /api/products</li>
 * </ul>
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class MediaController {

    private final MediaService mediaService;

    /** UC001: Get random products for homepage */
    @GetMapping("/api/products")
    public ResponseEntity<List<Media>> getProducts(@RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(mediaService.getRandomMedia(limit));
    }

    /** UC002: Get product detail by ID */
    @GetMapping("/api/products/{id}")
    public ResponseEntity<Media> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(mediaService.getMediaById(id));
    }

    /** UC008: Search products by title/category with optional price filter and pagination */
    @GetMapping("/api/products/search")
    public ResponseEntity<Page<Media>> searchProducts(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(required = false) List<String> category,
            @RequestParam(defaultValue = "0") int minPrice,
            @RequestParam(defaultValue = "2147483647") int maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(mediaService.searchMedia(query, category, minPrice, maxPrice, PageRequest.of(page, size)));
    }

    @GetMapping("/api/products/stats")
    public ResponseEntity<Map<String, Long>> getCatalogStats() {
        return ResponseEntity.ok(mediaService.getCatalogStats());
    }

    /** UC003: Add new product (Product Manager only) */
    @PostMapping("/api/products")
    public ResponseEntity<Media> addMedia(
            @Valid @RequestBody Media media,
            @RequestHeader(value = "X-Performed-By", defaultValue = "System") String performedBy) {
        return ResponseEntity.ok(mediaService.addMedia(media, performedBy));
    }

    /** UC004: Update product (Product Manager only) */
    @PutMapping("/api/products/{id}")
    public ResponseEntity<Media> updateMedia(
            @PathVariable Long id,
            @Valid @RequestBody Media media,
            @RequestHeader(value = "X-Performed-By", defaultValue = "System") String performedBy) {
        return ResponseEntity.ok(mediaService.updateMedia(id, media, performedBy));
    }

    /** UC005: Delete/deactivate products (Product Manager only) */
    @DeleteMapping("/api/products")
    public ResponseEntity<Map<String, String>> deleteMedia(
            @RequestBody List<Long> ids,
            @RequestHeader(value = "X-Performed-By", defaultValue = "System") String performedBy) {
        mediaService.deleteMedia(ids, performedBy);
        return ResponseEntity.ok(Map.of("message", "Products processed successfully"));
    }

    /** Get daily delete count and remaining quota for UI display */
    @GetMapping("/api/media/daily-delete-count")
    public ResponseEntity<Map<String, Integer>> getDailyDeleteCount() {
        int count = mediaService.getDailyDeleteCount();
        return ResponseEntity.ok(Map.of("count", count, "remaining", 20 - count));
    }

    /**
     * Get all operation history logs (Product Manager dashboard).
     *
     * <p>SRP fix: previously this method accessed HistoryLogRepository directly,
     * bypassing the service layer. Now delegates to {@link MediaService#getHistoryLogs()}.</p>
     */
    @GetMapping("/api/manager/history")
    public ResponseEntity<List<HistoryLog>> getHistoryLogs() {
        return ResponseEntity.ok(mediaService.getHistoryLogs());
    }
}
