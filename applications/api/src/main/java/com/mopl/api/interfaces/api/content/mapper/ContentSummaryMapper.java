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

    public ContentSummary toSummary(ContentModel model, List<String> tags) {
        return new ContentSummary(
            model.getId(),
            model.getType(),
            model.getTitle(),
            model.getDescription(),
            storageProvider.getUrl(model.getThumbnailPath()),
            tags,
            model.getAverageRating(),
            model.getReviewCount()
        );
    }

    public List<ContentSummary> toSummaries(
        Collection<ContentModel> models,
        Map<UUID, List<String>> tagsByContentId
    ) {
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }

        return models.stream()
            .map(model -> toSummary(
                model,
                tagsByContentId.getOrDefault(model.getId(), List.of())
            ))
            .toList();
    }
}
