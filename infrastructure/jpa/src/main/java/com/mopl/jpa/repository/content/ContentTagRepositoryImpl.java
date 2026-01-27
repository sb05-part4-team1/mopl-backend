package com.mopl.jpa.repository.content;

import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.repository.content.ContentTagRepository;
import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.jpa.entity.content.ContentTagEntity;
import com.mopl.jpa.entity.tag.TagEntityMapper;
import com.mopl.jpa.repository.tag.JpaTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ContentTagRepositoryImpl implements ContentTagRepository {

    private final JpaContentTagRepository jpaContentTagRepository;
    private final JpaTagRepository jpaTagRepository;
    private final JpaContentRepository jpaContentRepository;
    private final TagEntityMapper tagEntityMapper;

    @Override
    public List<TagModel> findTagsByContentId(UUID contentId) {
        List<ContentTagEntity> contentTagEntities = jpaContentTagRepository.findAllByContentId(
            contentId);

        return contentTagEntities.stream()
            .map(contentTag -> tagEntityMapper.toModel(contentTag.getTag()))
            .toList();
    }

    @Override
    public Map<UUID, List<TagModel>> findTagsByContentIdIn(List<UUID> contentIds) {

        if (contentIds == null || contentIds.isEmpty()) {
            return Map.of();
        }

        List<ContentTagEntity> entities = jpaContentTagRepository.findAllByContentIdIn(contentIds);

        return entities.stream()
            .collect(Collectors.groupingBy(
                contentTag -> contentTag.getContent().getId(),
                Collectors.mapping(
                    contentTag -> tagEntityMapper.toModel(contentTag.getTag()),
                    Collectors.toList()
                )
            ));
    }

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

    @Override
    public void deleteByContentId(UUID contentId) {
        jpaContentTagRepository.deleteByContentId(contentId);
    }
}
