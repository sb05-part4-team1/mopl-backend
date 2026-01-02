package com.mopl.domain.service.review;

import com.mopl.domain.exception.review.ReviewForbiddenException;
import com.mopl.domain.exception.review.ReviewNotFoundException;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.review.ReviewRepository;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewModel create(
        UUID contentId,
        UserModel author,
        String text,
        BigDecimal rating
    ) {

        ReviewModel reviewModel = ReviewModel.create(contentId, author.getId(), text, rating);

        return reviewRepository.save(reviewModel);
    }

    public ReviewModel update(
        UUID reviewId,
        UUID requesterId,
        String text,
        BigDecimal reting
    ) {
        ReviewModel reviewModel = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        UUID authorId = reviewModel.getAuthorId();

        // 이건 비즈니스 검증로직이라 Service로 옮김
        if (authorId == null || !authorId.equals(requesterId)) {
            throw new ReviewForbiddenException(reviewId, requesterId, authorId);
        }

        reviewModel.update(text, reting);

        return reviewRepository.save(reviewModel);
    }
}
