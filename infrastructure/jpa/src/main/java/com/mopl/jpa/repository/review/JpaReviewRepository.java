package com.mopl.jpa.repository.review;

import com.mopl.jpa.entity.review.ReviewEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaReviewRepository extends JpaRepository<ReviewEntity, UUID> {

    @EntityGraph(attributePaths = {"content", "author"})
    Optional<ReviewEntity> findWithContentAndAuthorById(UUID id);
}
