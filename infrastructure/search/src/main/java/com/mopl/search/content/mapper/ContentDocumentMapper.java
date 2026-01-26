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
            .reviewCount(doc.getReviewCount())
            .averageRating(doc.getAverageRating())
            .createdAt(doc.getCreatedAt())
            .updatedAt(doc.getUpdatedAt())
            .build();
    }
}
