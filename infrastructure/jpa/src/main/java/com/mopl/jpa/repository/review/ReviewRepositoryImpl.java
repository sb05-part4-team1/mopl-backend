package com.mopl.jpa.repository.review;

import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.repository.review.ReviewRepository;
import com.mopl.jpa.entity.review.ReviewEntity;
import com.mopl.jpa.entity.review.ReviewEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {

    private final JpaReviewRepository jpaReviewRepository;
    private final ReviewEntityMapper reviewEntityMapper;

    @Override
    public Optional<ReviewModel> findById(UUID reviewId) {
        return jpaReviewRepository.findWithContentAndAuthorById(reviewId)
            .map(reviewEntityMapper::toModel);
    }

    @Override
    public ReviewModel save(ReviewModel reviewModel) {
        ReviewEntity reviewEntity = reviewEntityMapper.toEntity(reviewModel);
        ReviewEntity savedReviewEntity = jpaReviewRepository.save(reviewEntity);
        return reviewEntityMapper.toModel(savedReviewEntity);
    }

    // 이하 메서드들 cleanup batch 전용
    @Override
    public List<UUID> findCleanupTargets(Instant threshold, int limit) {
        return jpaReviewRepository.findCleanupTargets(threshold, limit);
    }

    @Override
    public int deleteAllByIds(List<UUID> reviewIds) {
        return jpaReviewRepository.deleteAllByIds(reviewIds);
    }

    @Override
    public int softDeleteByContentIds(List<UUID> contentIds, Instant now) {
        return jpaReviewRepository.softDeleteByContentIds(contentIds, now);
    }
}
