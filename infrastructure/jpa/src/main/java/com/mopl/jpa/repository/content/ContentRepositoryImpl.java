package com.mopl.jpa.repository.content;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.jpa.entity.content.ContentEntityMapper;
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
        return jpaContentRepository.findById(contentId)
            .map(contentEntityMapper::toModel);
    }

    @Override
    public ContentModel save(ContentModel contentModel) {
        ContentEntity contentEntity = contentEntityMapper.toEntity(contentModel);
        ContentEntity savedContentEntity = jpaContentRepository.save(contentEntity);
        return contentEntityMapper.toModel(savedContentEntity);
    }
}
