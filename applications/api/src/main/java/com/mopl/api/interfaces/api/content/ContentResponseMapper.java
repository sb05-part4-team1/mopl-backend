package com.mopl.api.interfaces.api.content;

import com.mopl.domain.model.content.ContentModel;
import org.springframework.stereotype.Component;

@Component
public class ContentResponseMapper {

    public ContentResponse toResponse(ContentModel model) {
        return toResponse(model, 0.0, 0, 0L);
    }

    public ContentResponse toResponse(
        ContentModel model,
        double averageRating,
        int reviewCount,
        long watcherCount
    ) {
        return new ContentResponse(
            model.getId(),
            model.getType(),
            model.getTitle(),
            model.getDescription(),
            model.getThumbnailUrl(),
            model.getTags(),
            // TODO: 아래 수치 데이터들은 추후 도메인 로직 구현 시 실제 값으로 대체 필요
            averageRating,
            reviewCount,
            watcherCount
        );
    }
}
