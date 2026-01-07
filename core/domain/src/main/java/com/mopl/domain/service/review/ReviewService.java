package com.mopl.domain.service.review;

import com.mopl.domain.exception.review.ReviewForbiddenException;
import com.mopl.domain.exception.review.ReviewNotFoundException;
import com.mopl.domain.model.content.ContentModel;
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
        ContentModel content,
        UserModel author,
        String text,
        BigDecimal rating
    ) {

        ReviewModel reviewModel = ReviewModel.create(content, author, text, rating);

        return reviewRepository.save(reviewModel);
    }

    public ReviewModel update(
        UUID reviewId,
        UUID requesterId,
        String text,
        BigDecimal rating
    ) {
        ReviewModel reviewModel = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        UUID authorId = reviewModel.getAuthor() != null ? reviewModel.getAuthor().getId() : null;

        // 이건 비즈니스 검증로직이라 Service로 옮김
        if (authorId == null || !authorId.equals(requesterId)) {
            throw new ReviewForbiddenException(reviewId, requesterId, authorId);
        }

        reviewModel.update(text, rating);

        return reviewRepository.save(reviewModel);
    }

    public void delete(
        UUID reviewId,
        UUID requesterId
    ) {
        ReviewModel review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        UUID authorId = review.getAuthor() != null ? review.getAuthor().getId() : null;

        if (authorId == null || !authorId.equals(requesterId)) {
            throw new ReviewForbiddenException(reviewId, requesterId, authorId);
        }

        review.deleteReview();

        reviewRepository.save(review);
    }
}
