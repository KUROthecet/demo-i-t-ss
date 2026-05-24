package com.aims.service;

import com.aims.entity.HistoryLog;
import com.aims.entity.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * Service interface for media product management operations.
 *
 * <p>Defines the business operations available for managing the product catalog,
 * including retrieval, search, creation, update, and deletion with business rule enforcement.</p>
 *
 * @author AIMS Team
 * @version 1.0.0
 */
public interface MediaService {

    /**
     * Retrieves a random selection of active media items for the homepage display.
     *
     * @param limit maximum number of random items to return
     * @return a list of randomly selected active media items
     */
    List<Media> getRandomMedia(int limit);

    /**
     * Retrieves a single media item by its database ID.
     *
     * @param id the unique media ID
     * @return the found media entity
     * @throws com.aims.exception.ResourceNotFoundException if no media with the given ID exists
     */
    Media getMediaById(Long id);

    /**
     * UC008: Searches media by title or category with optional price filter and pagination.
     *
     * @param query    search term
     * @param minPrice min price
     * @param maxPrice max price
     * @param pageable pagination parameters
     * @return a page of matching media items
     */
    Page<Media> searchMedia(String query, int minPrice, int maxPrice, Pageable pageable);

    Map<String, Long> getCatalogStats();

    /**
     * Adds a new media item to the catalog and records the action in the history log.
     *
     * @param media       the media entity to persist (must have valid price range)
     * @param performedBy username of the staff member performing the action
     * @return the saved media entity with its generated ID
     * @throws com.aims.exception.BusinessException if the price is outside allowed range
     */
    Media addMedia(Media media, String performedBy);

    /**
     * Updates an existing media item's details and records the change in the history log.
     *
     * @param id          the ID of the media item to update
     * @param updated     a media entity containing the new field values
     * @param performedBy username of the staff member performing the action
     * @return the updated and saved media entity
     * @throws com.aims.exception.ResourceNotFoundException if the media item does not exist
     * @throws com.aims.exception.BusinessException         if the new price is out of range
     */
    Media updateMedia(Long id, Media updated, String performedBy);

    /**
     * Deletes or deactivates a list of media items by their IDs.
     *
     * <p>Items with zero stock are permanently deleted; items with remaining stock
     * are deactivated. Enforces batch size limit (max 10) and daily deletion
     * count limit (max 20).</p>
     *
     * @param ids         list of media IDs to delete/deactivate (max 10 items)
     * @param performedBy username of the staff member performing the action
     * @throws com.aims.exception.BusinessException if the batch exceeds 10 items or daily limit exceeded
     */
    void deleteMedia(List<Long> ids, String performedBy);

    /**
     * Returns the count of DELETE and DEACTIVATE operations performed today.
     *
     * <p>Used to enforce the daily deletion limit of 20 items per day.</p>
     *
     * @return count of media items deleted or deactivated since midnight today
     */
    int getDailyDeleteCount();

    /**
     * Returns all product management history log entries, sorted newest first.
     *
     * <p>SRP: the controller delegates history retrieval to the service layer,
     * avoiding a direct repository dependency in the controller.</p>
     *
     * @return all history log entries ordered by creation time descending
     */
    java.util.List<com.aims.entity.HistoryLog> getHistoryLogs();
}
