package com.mopl.domain.service.review;

import com.mopl.domain.exception.review.ReviewAlreadyExistsException;
import com.mopl.domain.exception.review.ReviewForbiddenException;
import com.mopl.domain.exception.review.ReviewNotFoundException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.repository.review.ReviewQueryRepository;
import com.mopl.domain.repository.review.ReviewQueryRequest;
import com.mopl.domain.repository.review.ReviewRepository;
import com.mopl.domain.support.cache.CacheName;
import com.mopl.domain.support.cache.CachePort;
import com.mopl.domain.support.cursor.CursorResponse;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class ReviewService {

    private final ReviewQueryRepository reviewQueryRepository;
    private final ReviewRepository reviewRepository;
    private final ContentRepository contentRepository;
    private final CachePort cachePort;

    public CursorResponse<ReviewModel> getAll(ReviewQueryRequest request) {
        return reviewQueryRepository.findAll(request);
    }

    public ReviewModel create(
        ContentModel content,
        UserModel author,
        String text,
        double rating
    ) {
        if (reviewRepository.existsByContentIdAndAuthorId(content.getId(), author.getId())) {
            throw ReviewAlreadyExistsException.withContentIdAndAuthorId(content.getId(), author.getId());
        }

        ReviewModel reviewModel = ReviewModel.create(content, author, text, rating);
        ReviewModel savedReviewModel = reviewRepository.save(reviewModel);

        ContentModel updatedContent = content.addReview(rating);
        contentRepository.save(updatedContent);
        cachePort.put(CacheName.CONTENTS, updatedContent.getId(), updatedContent);

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

        ReviewModel updatedReview = review.update(text, rating);
        ReviewModel savedReview = reviewRepository.save(updatedReview);

        if (rating != null && Double.compare(rating, oldRating) != 0) {
            ContentModel updatedContent = savedReview.getContent().updateReview(oldRating, rating);
            contentRepository.save(updatedContent);
            cachePort.put(CacheName.CONTENTS, updatedContent.getId(), updatedContent);
        }

        return savedReview;
    }

    public UUID deleteAndGetContentId(UUID reviewId, UUID requesterId) {
        ReviewModel review = getById(reviewId);
        validateAuthor(review, requesterId);

        ContentModel content = review.getContent();
        double rating = review.getRating();

        reviewRepository.delete(reviewId);

        ContentModel updatedContent = content.removeReview(rating);
        contentRepository.save(updatedContent);
        cachePort.put(CacheName.CONTENTS, updatedContent.getId(), updatedContent);

        return content.getId();
    }

    private ReviewModel getById(UUID reviewId) {
        return reviewRepository.findById(reviewId)
            .orElseThrow(() -> ReviewNotFoundException.withId(reviewId));
    }

    private void validateAuthor(ReviewModel review, UUID requesterId) {
        UUID authorId = review.getAuthor() != null ? review.getAuthor().getId() : null;
        if (authorId == null || !authorId.equals(requesterId)) {
            throw ReviewForbiddenException.withReviewIdAndRequesterIdAndAuthorId(review.getId(), requesterId, authorId);
        }
    }
}
