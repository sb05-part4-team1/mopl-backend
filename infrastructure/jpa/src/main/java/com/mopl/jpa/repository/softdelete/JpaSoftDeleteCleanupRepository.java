package com.mopl.jpa.repository.softdelete;

import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.jpa.repository.softdelete.projection.ContentThumbnailProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Soft-delete된 레코드를 hard-delete하기 위한 Repository.
 */
public interface JpaSoftDeleteCleanupRepository extends JpaRepository<ContentEntity, UUID> {

    @Query(
        value = """
            select BIN_TO_UUID(id)
            from contents
            where deleted_at is not null
              and deleted_at < :threshold
            order by deleted_at
            limit :limit
            """,
        nativeQuery = true
    )
    List<UUID> findCleanupTargets(Instant threshold, int limit);

    @Query(
        value = """
            select
                BIN_TO_UUID(id) as id,
                thumbnail_path as thumbnailPath
            from contents
            where id in (:contentIds)
            """,
        nativeQuery = true
    )
    List<ContentThumbnailProjection> findThumbnailPathsByIds(List<UUID> contentIds);

    @Modifying
    @Query(value = "delete from contents where id in (:contentIds)", nativeQuery = true)
    int deleteContentsByIdIn(List<UUID> contentIds);
}
