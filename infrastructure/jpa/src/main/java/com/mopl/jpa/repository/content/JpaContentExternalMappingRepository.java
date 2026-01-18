package com.mopl.jpa.repository.content;

import com.mopl.domain.model.content.ContentExternalProvider;
import com.mopl.jpa.entity.content.ContentExternalMappingEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaContentExternalMappingRepository extends
    JpaRepository<ContentExternalMappingEntity, UUID> {

    boolean existsByProviderAndExternalId(
        ContentExternalProvider provider,
        Long externalId
    );
}
