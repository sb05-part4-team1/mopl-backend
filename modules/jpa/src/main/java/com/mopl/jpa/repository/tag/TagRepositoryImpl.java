package com.mopl.jpa.repository.tag;

import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.repository.tag.TagRepository;
import com.mopl.jpa.entity.tag.TagEntity;
import com.mopl.jpa.entity.tag.TagEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TagRepositoryImpl implements TagRepository {

    private final JpaTagRepository jpaTagRepository;
    private final TagEntityMapper tagEntityMapper;

    @Override
    public TagModel save(TagModel tagModel) {
        TagEntity tagEntity = tagEntityMapper.toEntity(tagModel);
        TagEntity savedTagEntity = jpaTagRepository.save(tagEntity);
        return tagEntityMapper.toModel(savedTagEntity);
    }

    @Override
    public List<TagModel> saveAll(List<TagModel> tags) {
        List<TagEntity> entities = tags.stream()
            .map(tagEntityMapper::toEntity)
            .toList();

        List<TagEntity> savedEntities = jpaTagRepository.saveAll(entities);

        return savedEntities.stream()
            .map(tagEntityMapper::toModel)
            .toList();
    }

    @Override
    public Optional<TagModel> findByName(String tagName) {
        return jpaTagRepository.findByName(tagName).map(tagEntityMapper::toModel);
    }
}
