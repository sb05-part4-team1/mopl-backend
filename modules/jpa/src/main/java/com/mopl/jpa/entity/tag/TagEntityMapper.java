package com.mopl.jpa.entity.tag;

import com.mopl.domain.model.tag.TagModel;
import org.springframework.stereotype.Component;

@Component
public class TagEntityMapper {

    public TagModel toModel(TagEntity entity) {
        if (entity == null) {
            return null;
        }

        return TagModel.builder()
            .id(entity.getId())
            .name(entity.getName())
            .build();
    }

    public TagEntity toEntity(TagModel model) {
        if (model == null) {
            return null;
        }

        return TagEntity.builder()
            .id(model.getId())
            .name(model.getName())
            .build();
    }
}
