package com.mopl.jpa.repository.content.batch;

import com.mopl.domain.model.content.ContentExternalProvider;
import com.mopl.jpa.entity.content.ContentExternalMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaContentExternalMappingRepository extends
    JpaRepository<ContentExternalMappingEntity, UUID> {

    boolean existsByProviderAndExternalId(
        ContentExternalProvider provider,
        Long externalId
    );
}
