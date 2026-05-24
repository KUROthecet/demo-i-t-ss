package com.aims.repository;

import com.aims.entity.Media;
import com.aims.enums.MediaStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    @Query("SELECT m FROM Media m WHERE m.status = com.aims.enums.MediaStatus.ACTIVE AND " +
           "(LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(m.category) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "m.currentPrice BETWEEN :minPrice AND :maxPrice " +
           "ORDER BY m.title ASC")
    List<Media> searchByTitleOrCategory(
            @Param("query")    String query,
            @Param("minPrice") int    minPrice,
            @Param("maxPrice") int    maxPrice
    );

    List<Media> findByStatus(MediaStatus status);

    Optional<Media> findByBarcode(String barcode);

    @Query("SELECT m.category, COUNT(m) FROM Media m " +
           "WHERE m.status = com.aims.enums.MediaStatus.ACTIVE " +
           "GROUP BY m.category")
    List<Object[]> countActiveByCategory();

    @Query("SELECT MAX(m.currentPrice) FROM Media m " +
           "WHERE m.status = com.aims.enums.MediaStatus.ACTIVE")
    Integer findMaxCurrentPrice();

    @Query("SELECT m FROM Media m WHERE m.status = com.aims.enums.MediaStatus.ACTIVE AND " +
           "(LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(m.category) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "m.currentPrice BETWEEN :minPrice AND :maxPrice")
    List<Media> searchByTitleOrCategoryLimited(
            @Param("query")    String   query,
            @Param("minPrice") int      minPrice,
            @Param("maxPrice") int      maxPrice,
            Pageable                    pageable
    );

    // Two-step random fetch: native RANDOM() for IDs, then findAllById loads full JOINED entities
    @Query(value = "SELECT id FROM media WHERE status = 'ACTIVE' ORDER BY RANDOM() LIMIT :limit",
           nativeQuery = true)
    List<Long> findRandomActiveIds(@Param("limit") int limit);
}
