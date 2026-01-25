package com.mopl.jpa.repository.content.batch;

import com.mopl.jpa.entity.content.ContentDeletionLogEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaContentDeletionLogRepository extends
    JpaRepository<ContentDeletionLogEntity, UUID> {

    // 이하 메서드들 cleanup batch 전용
    @Query("""
            select l.contentId
            from ContentDeletionLogEntity l
            where l.contentId in :contentIds
        """)
    List<UUID> findExistingContentIds(@Param("contentIds") List<UUID> contentIds);

    @Query("""
            select
                l.id as logId,
                l.contentId as contentId,
                l.thumbnailPath as thumbnailPath
            from ContentDeletionLogEntity l
            where l.imageProcessedAt is null
              and l.thumbnailPath is not null
              and l.thumbnailPath <> ''
            order by l.deletedAt, l.id
        """)
    List<ContentDeletionLogRow> findImageCleanupTargets(Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update ContentDeletionLogEntity l
            set l.imageProcessedAt = :now
            where l.id in :logIds
        """)
    void markImageProcessed(
        List<UUID> logIds,
        Instant now
    );

    @Query("""
            select l.id
            from ContentDeletionLogEntity l
            where l.imageProcessedAt is not null
            order by l.deletedAt, l.id
        """)
    List<UUID> findFullyProcessedLogIds(Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from ContentDeletionLogEntity l
            where l.id in :logIds
        """)
    int deleteAllByIds(@Param("logIds") List<UUID> logIds);
}
