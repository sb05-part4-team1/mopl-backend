package com.mopl.jpa.repository.content;

import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.repository.content.ContentTagRepository;
import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.jpa.entity.content.ContentTagEntity;
import com.mopl.jpa.repository.tag.JpaTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ContentTagRepositoryImpl implements ContentTagRepository {

    private final JpaContentTagRepository jpaContentTagRepository;
    private final JpaTagRepository jpaTagRepository;
    private final JpaContentRepository jpaContentRepository;

    @Override
    public void saveAll(UUID contentId, List<TagModel> tags) {
        ContentEntity contentRef = jpaContentRepository.getReferenceById(contentId);

        List<ContentTagEntity> entities = tags.stream()
            .map(tag -> ContentTagEntity.builder()
                .content(contentRef)
                .tag(jpaTagRepository.getReferenceById(tag.getId()))
                .build()
            )
            .collect(Collectors.toList());

        jpaContentTagRepository.saveAll(entities);
    }
}
