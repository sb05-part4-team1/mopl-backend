package com.mopl.jpa.repository.review;

import com.mopl.jpa.entity.review.ReviewEntity;
import com.mopl.jpa.repository.review.projection.ReviewStatsProjection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface JpaReviewRepository extends JpaRepository<ReviewEntity, UUID> {

    @EntityGraph(attributePaths = {"content", "author"})
    Optional<ReviewEntity> findWithContentAndAuthorById(UUID id);

    boolean existsByContentIdAndAuthorIdAndDeletedAtIsNull(UUID contentId, UUID authorId);

    // denormalized sync batch 전용
    @Query("SELECT DISTINCT r.content.id FROM ReviewEntity r WHERE r.deletedAt IS NULL")
    Set<UUID> findAllContentIds();

    @Query("""
        SELECT r.content.id AS contentId, COUNT(r) AS reviewCount, AVG(r.rating) AS averageRating
        FROM ReviewEntity r
        WHERE r.content.id = :contentId AND r.deletedAt IS NULL
        GROUP BY r.content.id
        """)
    ReviewStatsProjection findReviewStatsByContentId(UUID contentId);

    // cleanup batch 전용
    @Query(
        value = """
                select BIN_TO_UUID(id)
                from reviews
                where deleted_at is not null
                  and deleted_at < :threshold
                order by deleted_at
                limit :limit
            """,
        nativeQuery = true
    )
    List<UUID> findCleanupTargets(Instant threshold, int limit);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "delete from reviews where id in :reviewIds", nativeQuery = true)
    int deleteByIdIn(List<UUID> reviewIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ReviewEntity r
            set r.deletedAt = :now
            where r.content.id in :contentIds
              and r.deletedAt is null
        """)
    int softDeleteByContentIdIn(List<UUID> contentIds, Instant now);
}
