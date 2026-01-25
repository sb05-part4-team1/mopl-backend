package com.mopl.jpa.repository.content.batch;

import com.mopl.domain.model.content.ContentExternalProvider;
import com.mopl.jpa.entity.content.ContentExternalMappingEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaContentExternalMappingRepository extends
    JpaRepository<ContentExternalMappingEntity, UUID> {

    boolean existsByProviderAndExternalId(
        ContentExternalProvider provider,
        Long externalId
    );

    // 이하 메서드들 cleanup batch 전용
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from ContentExternalMappingEntity m
            where m.content.id in :contentIds
        """)
    int deleteAllByContentIds(@Param("contentIds") List<UUID> contentIds);
}
