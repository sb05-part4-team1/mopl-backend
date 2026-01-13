package com.mopl.api.interfaces.api.content;

import com.mopl.domain.model.content.ContentModel;
import org.springframework.stereotype.Component;

@Component
public class ContentResponseMapper {

    public ContentResponse toResponse(
        ContentModel model
    ) {
        return new ContentResponse(
            model.getId(),
            model.getType(),
            model.getTitle(),
            model.getDescription(),
            model.getThumbnailUrl(),
            model.getTags(),
            model.getAverageRating(),
            model.getReviewCount(),
            // TODO: 아래 `watcherCounts` 는 추후 실제 값으로 대체 필요
            0
        );
    }
}
