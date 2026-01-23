package com.mopl.api.interfaces.api.content;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.tag.TagModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContentResponseMapper {

    public ContentResponse toResponse(
        ContentModel contentModel,
        String thumbnailUrl,
        List<TagModel> tags
    ) {
        return new ContentResponse(
            contentModel.getId(),
            contentModel.getType(),
            contentModel.getTitle(),
            contentModel.getDescription(),
            thumbnailUrl,
            toTagNames(tags),
            // TODO: 아래 3개 구현
            contentModel.getAverageRating(),
            contentModel.getReviewCount(),
            0
        );
    }

    private List<String> toTagNames(List<TagModel> tags) {
        return tags.stream()
            .map(TagModel::getName)
            .toList();
    }
}
