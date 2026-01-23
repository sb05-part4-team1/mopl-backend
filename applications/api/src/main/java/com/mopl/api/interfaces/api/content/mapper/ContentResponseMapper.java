package com.mopl.api.interfaces.api.content.mapper;

import com.mopl.api.interfaces.api.content.dto.ContentResponse;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.review.ReviewStats;
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
        return toResponse(
            contentModel,
            thumbnailUrl,
            tags,
            ReviewStats.empty(),
            0L
        );
    }

    public ContentResponse toResponse(
        ContentModel contentModel,
        String thumbnailUrl,
        List<TagModel> tags,
        ReviewStats reviewStats,
        long watcherCount
    ) {
        return new ContentResponse(
            contentModel.getId(),
            contentModel.getType(),
            contentModel.getTitle(),
            contentModel.getDescription(),
            thumbnailUrl,
            toTagNames(tags),
            reviewStats.averageRating(),
            reviewStats.reviewCount(),
            watcherCount
        );
    }

    public ContentResponse toResponseWithTagNames(
        ContentModel contentModel,
        String thumbnailUrl,
        List<String> tagNames,
        ReviewStats reviewStats,
        long watcherCount
    ) {
        return new ContentResponse(
            contentModel.getId(),
            contentModel.getType(),
            contentModel.getTitle(),
            contentModel.getDescription(),
            thumbnailUrl,
            tagNames,
            reviewStats.averageRating(),
            reviewStats.reviewCount(),
            watcherCount
        );
    }

    private List<String> toTagNames(List<TagModel> tags) {
        return tags.stream()
            .map(TagModel::getName)
            .toList();
    }
}
