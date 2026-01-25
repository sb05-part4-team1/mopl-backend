package com.mopl.jpa.repository.content.batch;

import com.mopl.domain.model.content.ContentExternalProvider;
import com.mopl.domain.repository.content.batch.ContentExternalMappingRepository;
import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.jpa.entity.content.ContentExternalMappingEntity;
import com.mopl.jpa.repository.content.JpaContentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ContentExternalMappingRepositoryImpl implements ContentExternalMappingRepository {

    private final JpaContentExternalMappingRepository jpaRepository;
    private final JpaContentRepository jpaContentRepository;

    @Override
    public void save(
        ContentExternalProvider provider,
        Long externalId,
        UUID contentId
    ) {
        ContentEntity contentRef = jpaContentRepository.getReferenceById(contentId);

        ContentExternalMappingEntity entity = ContentExternalMappingEntity.builder()
            .provider(provider)
            .externalId(externalId)
            .content(contentRef)
            .build();

        jpaRepository.save(entity);
    }

    @Override
    public boolean exists(
        ContentExternalProvider provider,
        Long externalId
    ) {
        return jpaRepository.existsByProviderAndExternalId(provider, externalId);
    }

    @Override
    public int deleteAllByContentIds(List<UUID> contentIds) {
        return jpaRepository.deleteAllByContentIds(contentIds);
    }
}
