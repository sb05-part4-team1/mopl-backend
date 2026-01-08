package com.mopl.jpa.entity.review;

import com.mopl.domain.model.review.ReviewModel;
import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.jpa.entity.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
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
            // [수정] UserEntityMapper를 쓰는 게 아니라 ID만 꺼냅니다.
            .authorId(reviewEntity.getAuthor() != null ? reviewEntity.getAuthor().getId() : null)
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
            // [수정] ID를 이용해 Author Entity 생성 (private 메서드 활용)
            .author(toAuthorEntity(reviewModel.getAuthorId()))
            .text(reviewModel.getText())
            .rating(reviewModel.getRating())
            .build();
    }

    private UserEntity toAuthorEntity(UUID authorId) {
        if (authorId == null) {
            return null;
        }
        return UserEntity.builder()
            .id(authorId)
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
