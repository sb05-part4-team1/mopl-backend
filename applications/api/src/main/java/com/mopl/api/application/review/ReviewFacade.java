package com.mopl.api.application.review;

import com.mopl.api.interfaces.api.review.dto.ReviewCreateRequest;
import com.mopl.api.interfaces.api.review.dto.ReviewUpdateRequest;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.repository.review.ReviewQueryRequest;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.review.ReviewService;
import com.mopl.domain.service.user.UserService;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.search.ContentSearchSyncPort;
import com.mopl.domain.support.transaction.AfterCommitExecutor;
import com.mopl.dto.review.ReviewResponse;
import com.mopl.dto.review.ReviewResponseMapper;
import com.mopl.logging.context.LogContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReviewFacade {

    private final ReviewService reviewService;
    private final ContentService contentService;
    private final UserService userService;
    private final ReviewResponseMapper reviewResponseMapper;
    private final ContentSearchSyncPort contentSearchSyncPort;
    private final AfterCommitExecutor afterCommitExecutor;

    public CursorResponse<ReviewResponse> getReviews(ReviewQueryRequest request) {
        return reviewService.getAll(request).map(reviewResponseMapper::toResponse);
    }

    @Transactional
    public ReviewResponse createReview(UUID requesterId, ReviewCreateRequest request) {
        UserModel author = userService.getById(requesterId);
        ContentModel content = contentService.getById(request.contentId());

        ReviewModel savedReview = reviewService.create(
            content,
            author,
            request.text(),
            request.rating()
        );

        syncContentAfterCommit(content.getId());

        LogContext.with("reviewId", savedReview.getId())
            .and("contentId", content.getId())
            .and("authorId", requesterId)
            .and("rating", request.rating())
            .info("Review created");

        return reviewResponseMapper.toResponse(savedReview);
    }

    @Transactional
    public ReviewResponse updateReview(UUID requesterId, UUID reviewId, ReviewUpdateRequest request) {
        userService.getById(requesterId);

        ReviewModel updatedReview = reviewService.update(
            reviewId,
            requesterId,
            request.text(),
            request.rating()
        );

        UUID contentId = updatedReview.getContent().getId();

        syncContentAfterCommit(contentId);

        return reviewResponseMapper.toResponse(updatedReview);
    }

    @Transactional
    public void deleteReview(UUID requesterId, UUID reviewId) {
        userService.getById(requesterId);

        UUID contentId = reviewService.deleteAndGetContentId(reviewId, requesterId);

        syncContentAfterCommit(contentId);

        LogContext.with("reviewId", reviewId)
            .and("contentId", contentId)
            .and("authorId", requesterId)
            .info("Review deleted");
    }

    private void syncContentAfterCommit(UUID contentId) {
        afterCommitExecutor.execute(() -> {
            ContentModel latest = contentService.getById(contentId);
            contentSearchSyncPort.upsert(latest);
        });
    }
}
