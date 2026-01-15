package com.mopl.jpa.entity.review;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.jpa.entity.content.ContentEntityMapper;
import com.mopl.jpa.entity.user.UserEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewEntityMapper {

    private final UserEntityMapper userEntityMapper;
    private final ContentEntityMapper contentEntityMapper;

    public ReviewModel toModel(ReviewEntity reviewEntity) {
        if (reviewEntity == null) {
            return null;
        }

        return ReviewModel.builder()
            .id(reviewEntity.getId())
            .createdAt(reviewEntity.getCreatedAt())
            .deletedAt(reviewEntity.getDeletedAt())
            .updatedAt(reviewEntity.getUpdatedAt())
            .content(contentEntityMapper.toModel(reviewEntity.getContent()))
            .author(userEntityMapper.toModel(reviewEntity.getAuthor()))
            .text(reviewEntity.getText())
            .rating(reviewEntity.getRating())
            .build();
    }

    public ReviewModel toModelWithIds(ReviewEntity reviewEntity) {
        if (reviewEntity == null) {
            return null;
        }

        ContentModel contentModel = reviewEntity.getContent() != null
            ? ContentModel.builder().id(reviewEntity.getContent().getId()).build()
            : null;

        UserModel authorModel = reviewEntity.getAuthor() != null
            ? UserModel.builder().id(reviewEntity.getAuthor().getId()).build()
            : null;

        return ReviewModel.builder()
            .id(reviewEntity.getId())
            .createdAt(reviewEntity.getCreatedAt())
            .deletedAt(reviewEntity.getDeletedAt())
            .updatedAt(reviewEntity.getUpdatedAt())
            .content(contentModel)
            .author(authorModel)
            .text(reviewEntity.getText())
            .rating(reviewEntity.getRating())
            .build();
    }

    public ReviewEntity toEntity(ReviewModel reviewModel) {
        if (reviewModel == null) {
            return null;
        }

        return ReviewEntity.builder()
            .id(reviewModel.getId())
            .createdAt(reviewModel.getCreatedAt())
            .deletedAt(reviewModel.getDeletedAt())
            .updatedAt(reviewModel.getUpdatedAt())
            .content(contentEntityMapper.toEntity(reviewModel.getContent()))
            .author(userEntityMapper.toEntity(reviewModel.getAuthor()))
            .text(reviewModel.getText())
            .rating(reviewModel.getRating())
            .build();
    }
}
