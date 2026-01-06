package com.mopl.jpa.entity.content;

import com.mopl.domain.model.content.ContentModel;
import org.springframework.stereotype.Component;

@Component
public class ContentEntityMapper {

    public ContentModel toModel(ContentEntity entity) {
        if (entity == null) {
            return null;
        }

        return ContentModel.builder()
            .id(entity.getId())
            .type(entity.getType())
            .title(entity.getTitle())
            .description(entity.getDescription())
            .thumbnailUrl(entity.getThumbnailUrl())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .deletedAt(entity.getDeletedAt())
            .build();
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
