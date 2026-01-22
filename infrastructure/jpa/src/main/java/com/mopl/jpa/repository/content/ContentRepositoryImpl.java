package com.mopl.jpa.repository.content;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.jpa.entity.content.ContentEntityMapper;
import com.mopl.jpa.repository.content.projection.ContentThumbnailRow;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ContentRepositoryImpl implements ContentRepository {

    private final JpaContentRepository jpaContentRepository;
    private final ContentEntityMapper contentEntityMapper;

    @Override
    public Optional<ContentModel> findById(UUID contentId) {
        Optional<ContentEntity> entity = jpaContentRepository.findById(contentId);
        return entity.map(contentEntityMapper::toModel);
    }

    @Override
    public boolean existsById(UUID contentId) {
        return jpaContentRepository.existsById(contentId);
    }

    @Override
    public ContentModel save(ContentModel contentModel) {
        ContentEntity entity = contentEntityMapper.toEntity(contentModel);
        ContentEntity saved = jpaContentRepository.save(entity);
        return contentEntityMapper.toModel(saved);
    }

    // cleanup batch 전용
    @Override
    public List<UUID> findCleanupTargets(Instant threshold, int limit) {
        return jpaContentRepository.findCleanupTargets(threshold, limit);
    }

    @Override
    public Map<UUID, String> findThumbnailPathsByIds(List<UUID> contentIds) {
        List<ContentThumbnailRow> rows = jpaContentRepository.findThumbnailPathsByIds(contentIds);

        Map<UUID, String> result = new HashMap<>();
        for (ContentThumbnailRow row : rows) {
            result.put(row.getId(), row.getThumbnailUrl());
        }
        return result;
    }

    @Override
    public int deleteAllByIds(List<UUID> contentIds) {
        return jpaContentRepository.deleteAllByIds(contentIds);
    }

}
