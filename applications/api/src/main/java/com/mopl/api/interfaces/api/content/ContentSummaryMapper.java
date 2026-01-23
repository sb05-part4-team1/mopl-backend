package com.mopl.api.interfaces.api.content;

import com.mopl.domain.model.content.ContentModel;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ContentSummaryMapper {

    public ContentSummary toSummary(ContentModel model, List<String> tags) {
        return new ContentSummary(
            model.getId(),
            model.getType(),
            model.getTitle(),
            model.getDescription(),
            model.getThumbnailUrl(),
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
