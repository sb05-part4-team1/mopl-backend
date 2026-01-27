package com.mopl.search.content.mapper;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.search.document.ContentDocument;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ContentDocumentMapper {

    public ContentDocument toDocument(ContentModel model) {
        String id = model.getId().toString();

        return ContentDocument.builder()
            .id(id)
            .contentId(id)
            .type(model.getType().name())
            .title(model.getTitle())
            .description(model.getDescription())
            .thumbnailPath(model.getThumbnailPath())
            .reviewCount(model.getReviewCount())
            .averageRating(model.getAverageRating())
            .popularityScore(model.getPopularityScore())
            .createdAt(model.getCreatedAt())
            .updatedAt(model.getUpdatedAt())
            .build();
    }

    public ContentModel toModel(ContentDocument doc) {
        return ContentModel.builder()
            .id(UUID.fromString(doc.getId()))
            .type(ContentModel.ContentType.valueOf(doc.getType()))
            .title(doc.getTitle())
            .description(doc.getDescription())
            .thumbnailPath(doc.getThumbnailPath())
            .reviewCount(doc.getReviewCount() != null ? doc.getReviewCount() : 0)
            .averageRating(doc.getAverageRating() != null ? doc.getAverageRating() : 0.0)
            .popularityScore(doc.getPopularityScore() != null ? doc.getPopularityScore() : 0.0)
            .createdAt(doc.getCreatedAt())
            .updatedAt(doc.getUpdatedAt())
            .build();
    }
}
