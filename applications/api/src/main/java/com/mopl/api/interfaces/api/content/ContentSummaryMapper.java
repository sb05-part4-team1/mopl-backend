package com.mopl.api.interfaces.api.content;

import com.mopl.domain.model.content.ContentModel;
import org.springframework.stereotype.Component;

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
            // TODO: 아래 수치 데이터들은 추후 도메인 로직 구현 시 실제 값으로 대체 필요
            averageRating,
            reviewCount
        );
    }

    public List<ContentSummary> toSummaries(List<ContentModel> models) {
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }

        return models.stream()
            .map(this::toSummary)
            .toList();
    }
}
