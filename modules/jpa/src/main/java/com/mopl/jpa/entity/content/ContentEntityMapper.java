package com.mopl.jpa.entity.content;

import com.mopl.domain.model.content.ContentModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContentEntityMapper {

    /**
     * 조회 유스케이스에서 Content + Tag 조합 시 사용
     */
    public ContentModel toModel(ContentEntity entity, List<String> tags) {
        if (entity == null) {
            return null;
        }

        return ContentModel.builder()
            .id(entity.getId())
            .type(entity.getType())
            .title(entity.getTitle())
            .description(entity.getDescription())
            .thumbnailUrl(entity.getThumbnailUrl())
            .tags(tags)
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .deletedAt(entity.getDeletedAt())
            .build();
    }

    public ContentModel toModel(ContentEntity entity) {
        return toModel(entity, List.of());
    }

    public ContentEntity toEntity(ContentModel model) {
        if (model == null) {
            return null;
        }

        return ContentEntity.builder()
            .id(model.getId())
            .type(model.getType())
            .title(model.getTitle())
            .description(model.getDescription())
            .thumbnailUrl(model.getThumbnailUrl())
            .createdAt(model.getCreatedAt())
            .updatedAt(model.getUpdatedAt())
            .deletedAt(model.getDeletedAt())
            .build();
    }
}
