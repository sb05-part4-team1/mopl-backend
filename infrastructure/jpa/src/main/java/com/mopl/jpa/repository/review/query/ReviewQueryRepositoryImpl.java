package com.mopl.jpa.repository.review.query;

import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.repository.review.ReviewQueryRepository;
import com.mopl.domain.repository.review.ReviewQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.jpa.entity.review.ReviewEntity;
import com.mopl.jpa.entity.review.ReviewEntityMapper;
import com.mopl.jpa.support.cursor.CursorPaginationHelper;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static com.mopl.jpa.entity.review.QReviewEntity.reviewEntity;

@Repository
@RequiredArgsConstructor
public class ReviewQueryRepositoryImpl implements ReviewQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final ReviewEntityMapper reviewEntityMapper;

    @Override
    public CursorResponse<ReviewModel> findAll(ReviewQueryRequest request) {
        ReviewSortFieldJpa sortFieldJpa = ReviewSortFieldJpa.from(request.sortBy());

        JPAQuery<ReviewEntity> jpaQuery = queryFactory
            .selectFrom(reviewEntity)
            .where(
                contentIdEqual(request.contentId())
            );

        CursorPaginationHelper.applyCursorPagination(
            request,
            sortFieldJpa,
            jpaQuery,
            reviewEntity.id
        );

        List<ReviewEntity> rows = jpaQuery.fetch();
        long totalCount = countTotal(request.contentId());

        return CursorPaginationHelper.buildResponse(
            rows,
            request,
            sortFieldJpa,
            totalCount,
            reviewEntityMapper::toModelWithAuthor,
            sortFieldJpa::extractValue,
            ReviewEntity::getId
        );
    }

    private long countTotal(UUID contentId) {
        Long total = queryFactory
            .select(reviewEntity.count())
            .from(reviewEntity)
            .where(
                contentIdEqual(contentId)
            )
            .fetchOne();
        return total != null ? total : 0;
    }

    private BooleanExpression contentIdEqual(UUID contentId) {
        return contentId != null ? reviewEntity.content.id.eq(contentId) : null;
    }
}
