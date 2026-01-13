package com.mopl.domain.service.review;

import com.mopl.domain.exception.review.ReviewForbiddenException;
import com.mopl.domain.exception.review.ReviewNotFoundException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.repository.review.ReviewRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ContentRepository contentRepository;

    public ReviewModel create(
        ContentModel content,
        UserModel author,
        String text,
        double rating
    ) {
        ReviewModel reviewModel = ReviewModel.create(content, author, text, rating);
        ReviewModel saved = reviewRepository.save(reviewModel);

        ContentModel updatedContent = content.applyReview(rating);
        contentRepository.save(updatedContent);

        return saved;
    }

    public ReviewModel update(
        UUID reviewId,
        UUID requesterId,
        String text,
        double rating
    ) {
        ReviewModel review = getByIdAndValidateAuthor(reviewId, requesterId);

        double oldRating = review.getRating();

        review.update(text, rating);
        ReviewModel saved = reviewRepository.save(review);

        ContentModel content = saved.getContent();
        contentRepository.save(content.updateReview(oldRating, rating));

        return saved;
    }

    public void delete(
        UUID reviewId,
        UUID requesterId
    ) {
        ReviewModel review = getByIdAndValidateAuthor(reviewId, requesterId);
        double rating = review.getRating();

        review.delete();
        reviewRepository.save(review);

        ContentModel content = review.getContent();
        contentRepository.save(content.removeReview(rating));
    }

    private ReviewModel getByIdAndValidateAuthor(UUID reviewId, UUID requesterId) {
        ReviewModel review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        UUID authorId = review.getAuthor() != null ? review.getAuthor().getId() : null;
        if (authorId == null || !authorId.equals(requesterId)) {
            throw new ReviewForbiddenException(reviewId, requesterId, authorId);
        }
        return review;
    }
}
