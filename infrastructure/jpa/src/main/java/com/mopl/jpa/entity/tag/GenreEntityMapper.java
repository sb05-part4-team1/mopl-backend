package com.mopl.jpa.entity.tag;

import com.mopl.domain.model.tag.GenreModel;
import org.springframework.stereotype.Component;

@Component
public class GenreEntityMapper {

    public GenreModel toModel(GenreEntity entity) {
        if (entity == null) {
            return null;
        }

        return GenreModel.builder()
            .id(entity.getId())
            .tmdbId(entity.getTmdbId())
            .name(entity.getName())
            .createdAt(entity.getCreatedAt())
            .deletedAt(entity.getDeletedAt())
            .build();
    }

    public GenreEntity toEntity(GenreModel model) {
        if (model == null) {
            return null;
        }

        return GenreEntity.builder()
            .id(model.getId())
            .tmdbId(model.getTmdbId())
            .name(model.getName())
            .createdAt(model.getCreatedAt())
            .deletedAt(model.getDeletedAt())
            .build();
    }
}
