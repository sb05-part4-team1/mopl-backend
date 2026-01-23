package com.mopl.api.interfaces.api.content.mapper;

import com.mopl.api.interfaces.api.content.dto.ContentResponse;
import com.mopl.domain.model.content.ContentModel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContentResponseMapper {

    public ContentResponse toResponse(
        ContentModel contentModel,
        String thumbnailUrl,
        List<String> tagNames,
        long watcherCount
    ) {
        return new ContentResponse(
            contentModel.getId(),
            contentModel.getType(),
            contentModel.getTitle(),
            contentModel.getDescription(),
            thumbnailUrl,
            tagNames,
            contentModel.getAverageRating(),
            contentModel.getReviewCount(),
            watcherCount
        );
    }
}
