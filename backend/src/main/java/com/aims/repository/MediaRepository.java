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

    List<Media> findByStatus(MediaStatus status);

    Optional<Media> findByBarcode(String barcode);

    @Query("SELECT m FROM Media m WHERE m.category = :category AND m.id <> :excludeId AND m.status = com.aims.enums.MediaStatus.ACTIVE ORDER BY ABS(m.currentPrice - :price) ASC")
    List<Media> findSimilarByCategory(
            @Param("category") String category,
            @Param("excludeId") Long excludeId,
            @Param("price") int price,
            Pageable pageable
    );
}
