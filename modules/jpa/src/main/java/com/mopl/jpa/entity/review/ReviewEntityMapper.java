package com.mopl.jpa.entity.review;

import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.jpa.entity.user.UserEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ReviewEntityMapper {

    public ReviewModel toModel(ReviewEntity reviewEntity) {
        if (reviewEntity == null) {
            return null;
        }

        return ReviewModel.builder()
            .id(reviewEntity.getId())
            .createdAt(reviewEntity.getCreatedAt())
            .deletedAt(reviewEntity.getDeletedAt())
            .updatedAt(reviewEntity.getUpdatedAt())
            .contentId(reviewEntity.getContent().getId())
            .author(toAuthorModel(reviewEntity.getAuthor()))
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
            .content(toContentEntity(reviewModel.getContentId()))
            .author(toAuthorEntity(reviewModel.getAuthor()))
            .text(reviewModel.getText())
            .rating(reviewModel.getRating())
            .build();
    }

    // ============ 여기 아래서부터는 추후 리팩토링 가능할 수 있음========================

    private UserEntity toAuthorEntity(UserModel author) {
        if (author == null) {
            return null;
        }

        return UserEntity.builder()
            .id(author.getId())
            .build();
    }

    private UserModel toAuthorModel(UserEntity authorEntity) {
        if (authorEntity == null) {
            return null;
        }

        return UserModel.builder()
            .id(authorEntity.getId())
            .build();
    }

    private ContentEntity toContentEntity(UUID contentId) {
        if (contentId == null) {
            return null;
        }

        return ContentEntity.builder()
            .id(contentId)
            .build();
    }

}
