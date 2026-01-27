package com.mopl.jpa.repository.review;

import com.mopl.jpa.entity.review.ReviewEntity;
import com.mopl.jpa.repository.review.projection.ReviewStatsProjection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface JpaReviewRepository extends JpaRepository<ReviewEntity, UUID> {

    @EntityGraph(attributePaths = {"content", "author"})
    Optional<ReviewEntity> findWithContentAndAuthorById(UUID id);

    boolean existsByContentIdAndAuthorId(UUID contentId, UUID authorId);

    // denormalized sync batch 전용
    @Query("SELECT DISTINCT r.content.id FROM ReviewEntity r")
    Set<UUID> findAllContentIds();

    @Query("""
        SELECT r.content.id AS contentId, COUNT(r) AS reviewCount, AVG(r.rating) AS averageRating
        FROM ReviewEntity r
        JOIN r.author
        WHERE r.content.id = :contentId
        GROUP BY r.content.id
        """)
    ReviewStatsProjection findReviewStatsByContentId(UUID contentId);
}
