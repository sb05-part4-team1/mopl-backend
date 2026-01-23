package com.mopl.jpa.repository.content;

import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.jpa.repository.content.projection.ContentThumbnailRow;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaContentRepository extends JpaRepository<ContentEntity, UUID> {

    // 이하 메서드들 cleanup batch 전용
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
    List<UUID> findCleanupTargets(
        @Param("threshold") Instant threshold,
        @Param("limit") int limit
    );

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
    List<ContentThumbnailRow> findThumbnailPathsByIds(@Param("contentIds") List<UUID> contentIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = """
                delete from contents
                where id in (:contentIds)
            """,
        nativeQuery = true
    )
    int deleteAllByIds(@Param("contentIds") List<UUID> contentIds);
}
