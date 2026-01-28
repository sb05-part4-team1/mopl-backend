package com.mopl.jpa.repository.content.sync.es;

import com.mopl.jpa.entity.content.ContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface JpaContentEsSyncRepository extends JpaRepository<ContentEntity, UUID> {

    @Query(
        value = """
            select
                BIN_TO_UUID(c.id) as id,
                c.type as type,
                c.title as title,
                c.description as description,
                c.thumbnail_path as thumbnailPath,
                c.review_count as reviewCount,
                c.average_rating as averageRating,
                c.popularity_score as popularityScore,
                c.created_at as createdAt,
                c.updated_at as updatedAt
            from contents c
            where c.deleted_at is null
              and (
                    :lastCreatedAt is null
                 or (c.created_at > :lastCreatedAt)
                 or (c.created_at = :lastCreatedAt and c.id > UUID_TO_BIN(:lastId))
              )
            order by c.created_at , c.id
            limit :limit
            """,
        nativeQuery = true
    )
    List<ContentEsSyncRow> findSyncTargets(Instant lastCreatedAt, String lastId, int limit);
}
