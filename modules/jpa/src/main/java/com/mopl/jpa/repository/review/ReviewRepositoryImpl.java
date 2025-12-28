package com.mopl.jpa.repository.review;

import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.repository.review.ReviewRepository;
import com.mopl.jpa.entity.review.ReviewEntity;
import com.mopl.jpa.entity.review.ReviewEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {

    private final JpaReviewRepository jpaReviewRepository;
    private final ReviewEntityMapper reviewEntityMapper;

    @Override
    public ReviewModel save(ReviewModel reviewModel) {
        ReviewEntity reviewEntity = reviewEntityMapper.toEntity(reviewModel);
        ReviewEntity savedReviewEntity = jpaReviewRepository.save(reviewEntity);
        return reviewEntityMapper.toModel(savedReviewEntity);
    }
}
