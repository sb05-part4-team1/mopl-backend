package com.mopl.jpa.repository.content;

import com.mopl.domain.model.content.ContentExternalProvider;
import com.mopl.jpa.entity.content.ContentExternalMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaContentExternalMappingRepository extends
    JpaRepository<ContentExternalMappingEntity, Long> {

    Optional<ContentExternalMappingEntity> findByProviderAndExternalId(
        ContentExternalProvider provider,
        Long externalId
    );

    boolean existsByProviderAndExternalId(
        ContentExternalProvider provider,
        Long externalId
    );
}
