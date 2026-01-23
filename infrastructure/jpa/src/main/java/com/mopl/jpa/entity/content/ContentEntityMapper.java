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
            .createdAt(entity.getCreatedAt())
            .deletedAt(entity.getDeletedAt())
            .updatedAt(entity.getUpdatedAt())
            .type(entity.getType())
            .title(entity.getTitle())
            .description(entity.getDescription())
            .thumbnailPath(entity.getThumbnailPath())
            .reviewCount(entity.getReviewCount())
            .averageRating(entity.getAverageRating())
            .build();
    }

    public ContentEntity toEntity(ContentModel model) {
        if (model == null) {
            return null;
        }

        return ContentEntity.builder()
            .id(model.getId())
            .createdAt(model.getCreatedAt())
            .deletedAt(model.getDeletedAt())
            .updatedAt(model.getUpdatedAt())
            .type(model.getType())
            .title(model.getTitle())
            .description(model.getDescription())
            .thumbnailPath(model.getThumbnailPath())
            .reviewCount(model.getReviewCount())
            .averageRating(model.getAverageRating())
            .build();
    }
}
