package com.mopl.jpa.repository.content;

import com.mopl.jpa.entity.content.ContentTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface JpaContentTagRepository extends JpaRepository<ContentTagEntity, Long> {

    @Query("""
            select ct
            from ContentTagEntity ct
            join fetch ct.tag
            where ct.content.id = :contentId
        """)
    List<ContentTagEntity> findAllByContentId(UUID contentId);

    @Query("""
            select ct
            from ContentTagEntity ct
            join fetch ct.tag
            where ct.content.id in :contentIds
        """)
    List<ContentTagEntity> findAllByContentIdIn(List<UUID> contentIds);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "delete from content_tags where content_id = :contentId", nativeQuery = true)
    void deleteByContentId(UUID contentId);
}
