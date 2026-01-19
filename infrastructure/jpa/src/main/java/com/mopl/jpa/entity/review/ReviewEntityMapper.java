package com.mopl.jpa.entity.review;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.jpa.entity.content.ContentEntityMapper;
import com.mopl.jpa.entity.user.UserEntity;
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

        return buildReviewModel(
            reviewEntity,
            toContentIdOnly(reviewEntity.getContent()),
            toAuthorIdOnly(reviewEntity.getAuthor())
        );
    }

    public ReviewModel toModelWithContent(ReviewEntity reviewEntity) {
        if (reviewEntity == null) {
            return null;
        }

        return buildReviewModel(
            reviewEntity,
            toContentModel(reviewEntity.getContent()),
            toAuthorIdOnly(reviewEntity.getAuthor())
        );
    }

    public ReviewModel toModelWithAuthor(ReviewEntity reviewEntity) {
        if (reviewEntity == null) {
            return null;
        }

        return buildReviewModel(
            reviewEntity,
            toContentIdOnly(reviewEntity.getContent()),
            toAuthorModel(reviewEntity.getAuthor())
        );
    }

    public ReviewModel toModelWithContentAndAuthor(ReviewEntity reviewEntity) {
        if (reviewEntity == null) {
            return null;
        }

        return buildReviewModel(
            reviewEntity,
            toContentModel(reviewEntity.getContent()),
            toAuthorModel(reviewEntity.getAuthor())
        );
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

    private ReviewModel buildReviewModel(
        ReviewEntity reviewEntity,
        ContentModel contentModel,
        UserModel authorModel
    ) {
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

    private ContentModel toContentIdOnly(ContentEntity contentEntity) {
        return contentEntity != null
            ? ContentModel.builder().id(contentEntity.getId()).build()
            : null;
    }

    private ContentModel toContentModel(ContentEntity contentEntity) {
        return contentEntity != null
            ? contentEntityMapper.toModel(contentEntity)
            : null;
    }

    private UserModel toAuthorIdOnly(UserEntity authorEntity) {
        return authorEntity != null
            ? UserModel.builder().id(authorEntity.getId()).build()
            : null;
    }

    private UserModel toAuthorModel(UserEntity authorEntity) {
        return authorEntity != null
            ? userEntityMapper.toModel(authorEntity)
            : null;
    }
}
