package com.mopl.jpa.repository.review;

import com.mopl.jpa.entity.review.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaReviewRepository extends JpaRepository<ReviewEntity, UUID> {

    @Query("""
            select r
            from ReviewEntity r
            join fetch r.content c
            join fetch r.author a
            where r.id = :reviewId
        """)
    Optional<ReviewEntity> findByIdWithContentAndAuthor(@Param("reviewId") UUID reviewId);
}
