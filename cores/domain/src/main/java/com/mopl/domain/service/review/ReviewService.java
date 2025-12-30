package com.mopl.domain.service.review;

import com.mopl.domain.exception.review.InvalidReviewDataException;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.review.ReviewRepository;
import com.mopl.domain.service.content.ContentService;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    // TODO: 추후 ContentService로 리팩토링
    private final ContentService contentService;

    public ReviewModel create(
        UUID contentId,
        UserModel author,
        String text,
        BigDecimal rating
    ) {
        validateContentExists(contentId);

        ReviewModel reviewModel = ReviewModel.create(contentId, author.getId(), text, rating);

        return reviewRepository.save(reviewModel);
    }

    private void validateContentExists(UUID contentId) {
        if (!contentService.exists(contentId)) {
            // TODO: 임시로 이렇게 두고 나중에 ContentNotFoundException(contentId);로 바꾸기
            throw new InvalidReviewDataException(
                "존재하지 않는 콘텐츠입니다. contentId=" + contentId
            );
        }
    }
}
