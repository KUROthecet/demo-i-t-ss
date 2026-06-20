package com.aims.repository;

import com.aims.entity.Media;
import com.aims.enums.MediaStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    @Query(value = """
        SELECT m.id FROM media m
        LEFT JOIN book b ON m.id = b.id
        LEFT JOIN cd c ON m.id = c.id
        LEFT JOIN dvd d ON m.id = d.id
        LEFT JOIN newspaper n ON m.id = n.id
        WHERE m.status = 'ACTIVE' AND m.quantity_in_stock > 0
        AND (:catAll = 1 OR m.category IN (:cats))
        AND m.current_price BETWEEN :minPrice AND :maxPrice
        AND (:query = '' OR
             LOWER(m.title)             LIKE LOWER(CONCAT('%',:query,'%'))
             OR LOWER(m.category)       LIKE LOWER(CONCAT('%',:query,'%'))
             OR LOWER(COALESCE(b.author,''))          LIKE LOWER(CONCAT('%',:query,'%'))
             OR LOWER(COALESCE(c.artist,''))          LIKE LOWER(CONCAT('%',:query,'%'))
             OR LOWER(COALESCE(d.director,''))        LIKE LOWER(CONCAT('%',:query,'%'))
             OR LOWER(COALESCE(n.editor_in_chief,'')) LIKE LOWER(CONCAT('%',:query,'%')))
        ORDER BY
             CASE WHEN :sort = 'price_asc'  THEN m.current_price END ASC,
             CASE WHEN :sort = 'price_desc' THEN m.current_price END DESC,
             CASE WHEN :sort = 'title_asc'  THEN m.title         END ASC,
             CASE WHEN :sort = 'title_desc' THEN m.title         END DESC,
             CASE WHEN :sort NOT IN ('price_asc','price_desc','title_asc','title_desc')
                  THEN CASE WHEN LOWER(m.title) = LOWER(:query)                        THEN 0
                            WHEN LOWER(m.title) LIKE LOWER(CONCAT(:query,'%'))          THEN 1
                            WHEN LOWER(m.title) LIKE LOWER(CONCAT('%',:query,'%'))      THEN 2
                            ELSE 3 END
             END ASC,
             m.title ASC
        LIMIT :lim OFFSET :off
        """, nativeQuery = true)
    List<Long> findActiveIdsByCreator(
        @Param("query") String query,
        @Param("catAll") int catAll,
        @Param("cats") List<String> cats,
        @Param("minPrice") int minPrice,
        @Param("maxPrice") int maxPrice,
        @Param("sort") String sort,
        @Param("lim") int lim,
        @Param("off") long off
    );

    @Query(value = """
        SELECT COUNT(*) FROM media m
        LEFT JOIN book b ON m.id = b.id
        LEFT JOIN cd c ON m.id = c.id
        LEFT JOIN dvd d ON m.id = d.id
        LEFT JOIN newspaper n ON m.id = n.id
        WHERE m.status = 'ACTIVE' AND m.quantity_in_stock > 0
        AND (:catAll = 1 OR m.category IN (:cats))
        AND m.current_price BETWEEN :minPrice AND :maxPrice
        AND (:query = '' OR
             LOWER(m.title)             LIKE LOWER(CONCAT('%',:query,'%'))
             OR LOWER(m.category)       LIKE LOWER(CONCAT('%',:query,'%'))
             OR LOWER(COALESCE(b.author,''))        LIKE LOWER(CONCAT('%',:query,'%'))
             OR LOWER(COALESCE(c.artist,''))        LIKE LOWER(CONCAT('%',:query,'%'))
             OR LOWER(COALESCE(d.director,''))      LIKE LOWER(CONCAT('%',:query,'%'))
             OR LOWER(COALESCE(n.editor_in_chief,'')) LIKE LOWER(CONCAT('%',:query,'%')))
        """, nativeQuery = true)
    long countActiveByCreator(
        @Param("query") String query,
        @Param("catAll") int catAll,
        @Param("cats") List<String> cats,
        @Param("minPrice") int minPrice,
        @Param("maxPrice") int maxPrice
    );

    @Query("SELECT m FROM Media m WHERE " +
           "(:status IS NULL OR m.status = :status) AND " +
           "(LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(m.category) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:categories IS NULL OR m.category IN :categories) AND " +
           "m.currentPrice BETWEEN :minPrice AND :maxPrice " +
           "ORDER BY m.status ASC, m.title ASC")
    Page<Media> searchManagerProducts(
        @Param("status") MediaStatus status,
        @Param("query") String query,
        @Param("categories") List<String> categories,
        @Param("minPrice") int minPrice,
        @Param("maxPrice") int maxPrice,
        Pageable pageable
    );

    @Query("SELECT m.category, COUNT(m) FROM Media m WHERE m.status = com.aims.enums.MediaStatus.ACTIVE GROUP BY m.category")
    List<Object[]> countByCategory();

    @Query("SELECT m.status, COUNT(m) FROM Media m GROUP BY m.status")
    List<Object[]> countAllByStatus();

    @Query("SELECT MAX(m.currentPrice) FROM Media m WHERE m.status = com.aims.enums.MediaStatus.ACTIVE AND m.quantityInStock > 0")
    Optional<Integer> findMaxPrice();

    @Query("SELECT m.currentPrice FROM Media m WHERE m.status = com.aims.enums.MediaStatus.ACTIVE AND m.quantityInStock > 0")
    List<Integer> findAllActivePrices();

    @Query(value = "SELECT m.id FROM media m WHERE m.status = 'ACTIVE' AND m.quantity_in_stock > 0", nativeQuery = true)
    List<Long> findAllActiveIds();

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
