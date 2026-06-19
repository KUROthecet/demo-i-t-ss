package com.aims.controller;

import com.aims.config.BusinessConstants;
import com.aims.dto.FieldSchema;
import com.aims.dto.response.MediaResponseDto;
import com.aims.media.MediaTypeDefinition;
import com.aims.service.MediaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.aims.entity.Media;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;
    private final List<MediaTypeDefinition> mediaTypeDefinitions;

    @GetMapping("/api/products")
    public ResponseEntity<List<MediaResponseDto>> getProducts(@RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(
            mediaService.getRandomMedia(limit).stream()
                .map(MediaResponseDto::fromEntity)
                .collect(Collectors.toList())
        );
    }

    @GetMapping("/api/products/{id}")
    public ResponseEntity<MediaResponseDto> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(MediaResponseDto.fromEntity(mediaService.getMediaById(id)));
    }

    @GetMapping("/api/products/search")
    public ResponseEntity<Page<MediaResponseDto>> searchProducts(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(required = false) List<String> category,
            @RequestParam(defaultValue = "0") int minPrice,
            @RequestParam(defaultValue = "2147483647") int maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
            mediaService.searchMedia(query, category, minPrice, maxPrice, PageRequest.of(page, size))
                .map(MediaResponseDto::fromEntity)
        );
    }

    @GetMapping("/api/products/price-histogram")
    public ResponseEntity<List<Integer>> getPriceHistogram() {
        return ResponseEntity.ok(mediaService.getAllPricesForHistogram());
    }

    @GetMapping("/api/manager/products")
    public ResponseEntity<Page<MediaResponseDto>> getManagerProducts(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(required = false) List<String> category,
            @RequestParam(defaultValue = "0") int minPrice,
            @RequestParam(defaultValue = "2147483647") int maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(
            mediaService.getManagerProducts(query, category, minPrice, maxPrice, status, PageRequest.of(page, size))
                .map(MediaResponseDto::fromEntity)
        );
    }

    @GetMapping("/api/manager/products/stats")
    public ResponseEntity<Map<String, Long>> getManagerStats() {
        return ResponseEntity.ok(mediaService.getManagerStats());
    }

    @GetMapping("/api/products/stats")
    public ResponseEntity<Map<String, Long>> getCatalogStats() {
        return ResponseEntity.ok(mediaService.getCatalogStats());
    }

    @GetMapping("/api/products/price-range")
    public ResponseEntity<Map<String, Integer>> getPriceRange() {
        return ResponseEntity.ok(mediaService.getPriceRange());
    }

    @GetMapping("/api/products/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(
            mediaTypeDefinitions.stream()
                .map(MediaTypeDefinition::getCategory)
                .sorted()
                .collect(Collectors.toList())
        );
    }

    @PostMapping("/api/products")
    public ResponseEntity<MediaResponseDto> addMedia(
            @Valid @RequestBody Media media,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(MediaResponseDto.fromEntity(mediaService.addMedia(media, principal.getUsername())));
    }

    @PutMapping("/api/products/{id}")
    public ResponseEntity<MediaResponseDto> updateMedia(
            @PathVariable Long id,
            @Valid @RequestBody Media media,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(MediaResponseDto.fromEntity(mediaService.updateMedia(id, media, principal.getUsername())));
    }

    @DeleteMapping("/api/products")
    public ResponseEntity<Map<String, String>> deleteMedia(
            @RequestBody List<Long> ids,
            @AuthenticationPrincipal UserDetails principal) {
        mediaService.deleteMedia(ids, principal.getUsername());
        return ResponseEntity.ok(Map.of("message", "Products processed successfully"));
    }

    @PatchMapping("/api/products/{id}/activate")
    public ResponseEntity<MediaResponseDto> activateMedia(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(MediaResponseDto.fromEntity(mediaService.reactivateMedia(id, principal.getUsername())));
    }

    @GetMapping("/api/products/{id}/similar")
    public ResponseEntity<List<MediaResponseDto>> getSimilarProducts(@PathVariable Long id) {
        return ResponseEntity.ok(
            mediaService.getSimilarMedia(id).stream()
                .map(MediaResponseDto::fromEntity)
                .collect(Collectors.toList())
        );
    }

    @GetMapping("/api/media/daily-delete-count")
    public ResponseEntity<Map<String, Integer>> getDailyDeleteCount() {
        int count = mediaService.getDailyDeleteCount();
        return ResponseEntity.ok(Map.of("count", count, "remaining", BusinessConstants.MAX_DAILY_DELETE - count));
    }

    @PostMapping("/api/products/stock-batch")
    public ResponseEntity<Map<Long, Integer>> getStockBatch(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(mediaService.getStockBatch(ids));
    }

    @GetMapping("/api/media/schema")
    public ResponseEntity<Map<String, List<FieldSchema>>> getFormSchema() {
        Map<String, List<FieldSchema>> schema = mediaTypeDefinitions.stream()
            .collect(Collectors.toMap(MediaTypeDefinition::getCategory, MediaTypeDefinition::getFormSchema));
        return ResponseEntity.ok(schema);
    }
}
