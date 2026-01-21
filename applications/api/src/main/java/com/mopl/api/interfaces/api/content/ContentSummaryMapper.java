package com.mopl.api.interfaces.api.content;

import com.mopl.domain.model.content.ContentModel;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class ContentSummaryMapper {

    public ContentSummary toSummary(ContentModel model) {
        return toSummary(model, 0.0, 0);
    }

    public ContentSummary toSummary(
        ContentModel model,
        double averageRating,
        int reviewCount
    ) {
        return new ContentSummary(
            model.getId(),
            model.getType(),
            model.getTitle(),
            model.getDescription(),
            model.getThumbnailUrl(),
            model.getTags(),
            // TODO: Redis 활용하여 계산, 기존 ContentModel에서 제거
            averageRating,
            reviewCount
        );
    }

    public List<ContentSummary> toSummaries(Collection<ContentModel> models) {
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }

        return models.stream()
            .map(this::toSummary)
            .toList();
    }
}
