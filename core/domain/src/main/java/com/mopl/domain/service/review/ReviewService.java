package com.mopl.domain.service.review;

import com.mopl.domain.exception.review.ReviewForbiddenException;
import com.mopl.domain.exception.review.ReviewNotFoundException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.repository.review.ReviewQueryRepository;
import com.mopl.domain.repository.review.ReviewQueryRequest;
import com.mopl.domain.repository.review.ReviewRepository;
import com.mopl.domain.support.cursor.CursorResponse;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewQueryRepository reviewQueryRepository;
    private final ContentRepository contentRepository;

    public CursorResponse<ReviewModel> getAll(ReviewQueryRequest request) {
        return reviewQueryRepository.findAll(request);
    }

    public ReviewModel create(
        ContentModel content,
        UserModel author,
        String text,
        double rating
    ) {
        ReviewModel reviewModel = ReviewModel.create(content, author, text, rating);
        ReviewModel savedReviewModel = reviewRepository.save(reviewModel);

        ContentModel updatedContent = content.applyReview(rating);
        contentRepository.save(updatedContent);

        return savedReviewModel;
    }

    public ReviewModel update(
        UUID reviewId,
        UUID requesterId,
        String text,
        Double rating
    ) {
        ReviewModel review = getById(reviewId);
        validateAuthor(review, requesterId);

        double oldRating = review.getRating();

        review.update(text, rating);
        ReviewModel saved = reviewRepository.save(review);

        if (rating != null && rating != oldRating) {
            ContentModel content = saved.getContent();
            contentRepository.save(content.updateReview(oldRating, rating));
        }

        return saved;
    }

    public void delete(
        UUID reviewId,
        UUID requesterId
    ) {
        ReviewModel review = getById(reviewId);
        validateAuthor(review, requesterId);

        double rating = review.getRating();

        review.delete();
        reviewRepository.save(review);

        ContentModel content = review.getContent();
        contentRepository.save(content.removeReview(rating));
    }

    private ReviewModel getById(UUID reviewId) {
        return reviewRepository.findById(reviewId)
            .orElseThrow(() -> ReviewNotFoundException.withId(reviewId));
    }

    private void validateAuthor(ReviewModel review, UUID requesterId) {
        UUID authorId = review.getAuthor() != null ? review.getAuthor().getId() : null;
        if (authorId == null || !authorId.equals(requesterId)) {
            throw ReviewForbiddenException.withIds(review.getId(), requesterId, authorId);
        }
    }
}
