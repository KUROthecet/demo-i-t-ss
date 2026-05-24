package com.aims.service;

import com.aims.entity.Media;

import java.util.List;
import java.util.Map;

public interface MediaService {

    List<Media> getRandomMedia(int limit);

    Media getMediaById(Long id);

    List<Media> searchMedia(String query, int minPrice, int maxPrice, int limit);

    Media addMedia(Media media, String performedBy);

    Media updateMedia(Long id, Media updated, String performedBy);

    void deleteMedia(List<Long> ids, String performedBy);

    int getDailyDeleteCount();

    java.util.List<com.aims.entity.HistoryLog> getHistoryLogs();

    Map<String, Object> getCatalogStats();
}
