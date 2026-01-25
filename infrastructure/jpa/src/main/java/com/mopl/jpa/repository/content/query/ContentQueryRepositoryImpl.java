package com.mopl.jpa.repository.content.query;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.repository.content.ContentQueryRepository;
import com.mopl.domain.repository.content.ContentQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.jpa.entity.content.ContentEntityMapper;
import com.mopl.jpa.support.cursor.CursorPaginationHelper;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.mopl.jpa.entity.content.QContentEntity.contentEntity;
import static org.springframework.util.StringUtils.hasText;

@Repository
@RequiredArgsConstructor
public class ContentQueryRepositoryImpl implements ContentQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final ContentEntityMapper contentEntityMapper;

    @Override
    public CursorResponse<ContentModel> findAll(ContentQueryRequest request) {
        ContentSortFieldJpa sortFieldJpa = ContentSortFieldJpa.from(request.sortBy());

        JPAQuery<ContentEntity> jpaQuery = baseQuery(request)
            .select(contentEntity);

        CursorPaginationHelper.applyCursorPagination(
            request,
            sortFieldJpa,
            jpaQuery,
            contentEntity.id
        );

        List<ContentEntity> rows = jpaQuery.fetch();

        if (rows.isEmpty()) {
            return CursorResponse.empty(
                sortFieldJpa.getFieldName(),
                request.sortDirection()
            );
        }

        long totalCount = countTotal(request);

        return CursorPaginationHelper.buildResponse(
            rows,
            request,
            sortFieldJpa,
            totalCount,
            contentEntityMapper::toModel,
            sortFieldJpa::extractValue,
            ContentEntity::getId
        );
    }

    private JPAQuery<?> baseQuery(ContentQueryRequest request) {
        return queryFactory
            .from(contentEntity)
            .where(
                typeEqual(request.typeEqual()),
                keywordLike(request.keywordLike())
            );
    }

    private long countTotal(ContentQueryRequest request) {
        Long total = baseQuery(request)
            .select(contentEntity.count())
            .fetchOne();
        return total != null ? total : 0;
    }

    private BooleanExpression typeEqual(ContentModel.ContentType type) {
        return type != null ? contentEntity.type.eq(type) : null;
    }

    private BooleanExpression keywordLike(String keyword) {
        return hasText(keyword)
            ? contentEntity.title.containsIgnoreCase(keyword)
                .or(contentEntity.description.containsIgnoreCase(keyword))
            : null;
    }
}
