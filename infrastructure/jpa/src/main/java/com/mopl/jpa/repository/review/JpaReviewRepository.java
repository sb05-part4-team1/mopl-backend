package com.mopl.jpa.repository.review;

import com.mopl.jpa.entity.review.ReviewEntity;
import com.mopl.jpa.repository.review.projection.ReviewStatsProjection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaReviewRepository extends JpaRepository<ReviewEntity, UUID> {

    @EntityGraph(attributePaths = {"content", "author"})
    Optional<ReviewEntity> findWithContentAndAuthorById(UUID id);

    boolean existsByContentIdAndAuthorId(UUID contentId, UUID authorId);

    // denormalized sync batch 전용
    @Query(
        value = """
            SELECT DISTINCT BIN_TO_UUID(r.content_id)
            FROM reviews r
            WHERE r.content_id > :lastContentId
            ORDER BY r.content_id
            LIMIT :limit
            """,
        nativeQuery = true
    )
    List<UUID> findContentIdsAfter(UUID lastContentId, int limit);

    @Query("""
        SELECT r.content.id AS contentId, COUNT(r) AS reviewCount, AVG(r.rating) AS averageRating
        FROM ReviewEntity r
        JOIN r.author
        WHERE r.content.id = :contentId
        GROUP BY r.content.id
        """)
    ReviewStatsProjection findReviewStatsByContentId(UUID contentId);
}
