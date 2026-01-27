package com.mopl.jpa.repository.content.sync.es;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.repository.content.sync.es.ContentEsSyncRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ContentEsSyncRepositoryImpl implements ContentEsSyncRepository {

    private final JpaContentEsSyncRepository jpaRepository;

    @Override
    public List<ContentModel> findSyncTargets(Instant lastCreatedAt, String lastId, int limit) {
        return jpaRepository.findSyncTargets(lastCreatedAt, lastId, limit).stream()
            .map(this::toModel)
            .toList();
    }

    private ContentModel toModel(ContentEsSyncRow row) {
        return ContentModel.builder()
            .id(row.getId())
            .type(ContentModel.ContentType.valueOf(row.getType()))
            .title(row.getTitle())
            .description(row.getDescription())
            .thumbnailPath(row.getThumbnailPath())
            .reviewCount(row.getReviewCount())
            .averageRating(row.getAverageRating())
            .popularityScore(row.getPopularityScore())
            .createdAt(row.getCreatedAt())
            .updatedAt(row.getUpdatedAt())
            .build();
    }
}
