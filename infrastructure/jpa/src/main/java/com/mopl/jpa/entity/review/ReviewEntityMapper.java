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
            // .author(userEntityMapper.toModel(reviewEntity.getAuthor()))
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
            //.author(userEntityMapper.toEntity(reviewModel.getAuthor()))
            // [수정] ID를 이용해 Author Entity 생성 (private 메서드 활용)
            .author(toAuthorEntity(reviewModel.getAuthorId()))
            .text(reviewModel.getText())
            .rating(reviewModel.getRating())
            .build();
    }

    // [추가] Content와 똑같은 방식으로 User도 ID만 가진 엔티티 생성 (이것은 전체 객체가 아니라 ID만 빼워서 의존성 지움)
    private UserEntity toAuthorEntity(UUID authorId) {
        if (authorId == null) {
            return null;
        }
        return UserEntity.builder()
            .id(authorId)
            .build();
    }

    // TODO: 팀원이 ContentEntityMapper 구현 완료 시, 주입받아 처리하도록 수정 필요
    private ContentEntity toContentEntity(UUID contentId) {
        if (contentId == null) {
            return null;
        }

        return ContentEntity.builder()
            .id(contentId)
            .build();
    }

}
