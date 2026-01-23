package com.mopl.jpa.repository.content;

import com.mopl.jpa.entity.content.ContentTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaContentTagRepository extends JpaRepository<ContentTagEntity, Long> {

    @Query("""
            select ct
            from ContentTagEntity ct
            join fetch ct.tag
            where ct.content.id = :contentId
        """)
    List<ContentTagEntity> findAllByContentId(@Param("contentId") UUID contentId);

    @Query("""
            select ct
            from ContentTagEntity ct
            join fetch ct.tag
            where ct.content.id in :contentIds
        """)
    List<ContentTagEntity> findAllByContentIdIn(@Param("contentIds") List<UUID> contentIds);

    // 벌크 삭제 수행 (주의: 영속성 컨텍스트를 무시하고 DB에 직접 쿼리함)
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            delete from ContentTagEntity ct
            where ct.content.id = :contentId
        """)
    void deleteAllByContentId(@Param("contentId") UUID contentId);

    // 이하 메서드들 cleanup batch 전용
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            delete from ContentTagEntity ct
            where ct.content.id in :contentIds
        """)
    int deleteAllByContentIds(@Param("contentIds") List<UUID> contentIds);
}
