package com.mopl.jpa.repository.review;

import com.mopl.jpa.entity.review.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaReviewRepository extends JpaRepository<ReviewEntity, UUID> {
}
