package com.mopl.jpa.repository.content;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.jpa.entity.content.ContentEntityMapper;
import com.mopl.jpa.entity.content.ContentTagEntity;
import com.mopl.jpa.entity.tag.TagEntity;
import com.mopl.jpa.repository.tag.JpaContentTagRepository; 
import com.mopl.jpa.repository.tag.JpaTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ContentRepositoryImpl implements ContentRepository {

    private final JpaContentRepository jpaContentRepository;
    private final JpaTagRepository jpaTagRepository;
    private final JpaContentTagRepository jpaContentTagRepository;
    private final ContentEntityMapper contentEntityMapper;

    @Override
    public ContentModel save(ContentModel contentModel, List<TagModel> tags) {
        ContentEntity contentEntity = contentEntityMapper.toEntity(contentModel);
        ContentEntity savedContent = jpaContentRepository.save(contentEntity);

        if (tags != null && !tags.isEmpty()) {
            tags.forEach(tagModel -> {
                /**
                 * [성능 최적화] getReferenceById 활용
                 * 실제 DB를 조회(SELECT)하지 않고, ID값만 가진 프록시(Proxy) 객체를 생성합니다.
                 * 매핑 테이블(content_tags) 저장 시 태그 ID만 필요하므로 불필요한 IO를 방지합니다.
                 */
                TagEntity tagEntity = jpaTagRepository.getReferenceById(tagModel.getId());

                ContentTagEntity contentTag = ContentTagEntity.builder()
                    .content(savedContent)
                    .tag(tagEntity)
                    .build();

                jpaContentTagRepository.save(contentTag);
            });
        }

        List<String> tagNames = (tags == null) ? List.of() : tags.stream().map(TagModel::getName)
            .toList();
        
        return contentEntityMapper.toModel(savedContent, tagNames);
    } 

    @Override
    public boolean existsById(UUID contentId) {
        return jpaContentRepository.existsById(contentId);
    }
}