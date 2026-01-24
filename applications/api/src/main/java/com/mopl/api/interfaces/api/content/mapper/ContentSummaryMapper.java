package com.mopl.api.interfaces.api.content.mapper;

import com.mopl.api.interfaces.api.content.dto.ContentSummary;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.storage.provider.StorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ContentSummaryMapper {

    private final StorageProvider storageProvider;

    public ContentSummary toSummary(
        ContentModel contentModel,
        List<String> tags
    ) {
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

    public List<ContentSummary> toSummaries(
        Collection<ContentModel> contentModels,
        Map<UUID, List<String>> tagsByContentId
    ) {
        if (contentModels == null || contentModels.isEmpty()) {
            return Collections.emptyList();
        }

        return contentModels.stream()
            .map(model -> toSummary(
                model,
                tagsByContentId.getOrDefault(model.getId(), List.of())
            ))
            .toList();
    }
}
