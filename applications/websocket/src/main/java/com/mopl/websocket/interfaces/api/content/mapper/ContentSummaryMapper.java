package com.mopl.websocket.interfaces.api.content.mapper;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.storage.provider.StorageProvider;
import com.mopl.websocket.interfaces.api.content.dto.ContentSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ContentSummaryMapper {

    private final StorageProvider storageProvider;

    public ContentSummary toSummary(ContentModel contentModel, List<String> tags) {
        return new ContentSummary(
            contentModel.getId(),
            contentModel.getType(),
            contentModel.getTitle(),
            contentModel.getDescription(),
            storageProvider.getUrl(contentModel.getThumbnailPath()),
            tags,
            contentModel.getAverageRating(),
            contentModel.getReviewCount()
        );
    }
}
