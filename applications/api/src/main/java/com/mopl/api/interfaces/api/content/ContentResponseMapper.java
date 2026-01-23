package com.mopl.api.interfaces.api.content;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.storage.provider.FileStorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContentResponseMapper {

    private final FileStorageProvider fileStorageProvider;

    public ContentResponse toResponse(
        ContentModel model
    ) {
        String thumbnailUrl = fileStorageProvider.getUrl(model.getThumbnailUrl());
        return new ContentResponse(
            model.getId(),
            model.getType(),
            model.getTitle(),
            model.getDescription(),
            thumbnailUrl,
            model.getTags(),
            model.getAverageRating(),
            model.getReviewCount(),
            // TODO: 아래 `watcherCounts` 는 추후 실제 값으로 대체 필요
            0
        );
    }
}
