// Communication Cohesion
// This repository is dedicated to Media type.
package com.aims.repository;

import com.aims.entity.Media;
import com.aims.enums.MediaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MediaRepository — Spring Data JPA repository for Media entities.
 *
 * <p>Custom queries for AIMS-specific operations:
 * <ul>
 *   <li>Random product browsing (homepage)</li>
 *   <li>Text + price search (UC008)</li>
 *   <li>Status-based filtering using the type-safe {@link MediaStatus} enum</li>
 * </ul>
 */
@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    /**
     * UC008: Searches media by title (case-insensitive) or category,
     * optionally filtered by price range. Only returns ACTIVE products.
     *
     * @param query    search term (matches title or category)
     * @param minPrice minimum current price (inclusive)
     * @param maxPrice maximum current price (inclusive)
     * @return matching active media items
     */
    @Query("SELECT m FROM Media m WHERE m.status = com.aims.enums.MediaStatus.ACTIVE AND " +
           "(LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(m.category) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:categories IS NULL OR m.category IN :categories) AND " +
           "m.currentPrice BETWEEN :minPrice AND :maxPrice " +
           "ORDER BY m.title ASC")
    org.springframework.data.domain.Page<Media> searchByTitleOrCategory(
            @Param("query") String query,
            @Param("categories") List<String> categories,
            @Param("minPrice") int minPrice,
            @Param("maxPrice") int maxPrice,
            org.springframework.data.domain.Pageable pageable
    );

    @Query("SELECT m.category, COUNT(m) FROM Media m WHERE m.status = com.aims.enums.MediaStatus.ACTIVE GROUP BY m.category")
    List<Object[]> countByCategory();

    /**
     * Finds all media filtered by status using the type-safe {@link MediaStatus} enum.
     *
     * @param status {@link MediaStatus#ACTIVE} or {@link MediaStatus#DEACTIVATED}
     * @return list of media with the given status
     */
    List<Media> findByStatus(MediaStatus status);

    /**
     * Finds media by exact barcode.
     *
     * @param barcode product barcode
     * @return matching media if found
     */
    Optional<Media> findByBarcode(String barcode);
}
