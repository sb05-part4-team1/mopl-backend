package com.mopl.dto.content;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.storage.provider.StorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ContentResponseMapper {

    private final StorageProvider storageProvider;

    public ContentResponse toResponse(
        ContentModel contentModel,
        List<String> tags,
        long watcherCount
    ) {
        return new ContentResponse(
            contentModel.getId(),
            contentModel.getType(),
            contentModel.getTitle(),
            contentModel.getDescription(),
            storageProvider.getUrl(contentModel.getThumbnailPath()),
            tags,
            contentModel.getAverageRating(),
            contentModel.getReviewCount(),
            watcherCount
        );
    }
}
