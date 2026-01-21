package com.mopl.jpa.repository.review;

import com.mopl.jpa.entity.review.ReviewEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaReviewRepository extends JpaRepository<ReviewEntity, UUID> {

    @EntityGraph(attributePaths = {"content", "author"})
    Optional<ReviewEntity> findWithContentAndAuthorById(UUID id);

    // 이하 메서드들 cleanup batch 전용
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
    List<UUID> findCleanupTargets(
        @Param("threshold") Instant threshold,
        @Param("limit") int limit
    );

    @Modifying
    @Query(
        value = """
                delete from reviews
                where id in (:reviewIds)
            """,
        nativeQuery = true
    )
    int deleteAllByIds(@Param("reviewIds") List<UUID> reviewIds);

    @Modifying
    @Query(
        value = """
                update reviews
                set deleted_at = utc_timestamp(6)
                where content_id in (:contentIds)
                  and deleted_at is null
            """,
        nativeQuery = true
    )
    int softDeleteByContentIds(@Param("contentIds") List<UUID> contentIds);
}
