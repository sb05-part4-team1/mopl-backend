package com.mopl.jpa.repository.playlist.query;

import com.mopl.domain.model.playlist.PlaylistModel;
import com.mopl.domain.repository.playlist.PlaylistQueryRepository;
import com.mopl.domain.repository.playlist.PlaylistQueryRequest;
import com.mopl.domain.repository.playlist.PlaylistSortField;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.mopl.jpa.entity.playlist.PlaylistEntity;
import com.mopl.jpa.entity.playlist.PlaylistEntityMapper;
import com.mopl.jpa.support.cursor.CursorPaginationHelper;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.mopl.jpa.entity.playlist.QPlaylistEntity.playlistEntity;
import static com.mopl.jpa.entity.playlist.QPlaylistSubscriberEntity.playlistSubscriberEntity;
import static org.springframework.util.StringUtils.hasText;

@Repository
@RequiredArgsConstructor
public class PlaylistQueryRepositoryImpl implements PlaylistQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final PlaylistEntityMapper playlistEntityMapper;

    @Override
    public CursorResponse<PlaylistModel> findAll(PlaylistQueryRequest request) {
        PlaylistSortFieldJpa sortFieldJpa = PlaylistSortFieldJpa.from(request.sortBy());

        if (request.sortBy() == PlaylistSortField.subscribeCount) {
            return findAllWithSubscribeCount(request, sortFieldJpa);
        }

        return findAllSimple(request, sortFieldJpa);
    }

    private CursorResponse<PlaylistModel> findAllSimple(
        PlaylistQueryRequest request,
        PlaylistSortFieldJpa sortFieldJpa
    ) {
        JPAQuery<PlaylistEntity> jpaQuery = queryFactory
            .selectFrom(playlistEntity)
            .leftJoin(playlistEntity.owner).fetchJoin()
            .where(
                keywordLike(request.keywordLike()),
                ownerIdEqual(request.ownerIdEqual()),
                subscriberIdEqual(request.subscriberIdEqual())
            );

        CursorPaginationHelper.applyCursorPagination(
            request,
            sortFieldJpa,
            jpaQuery,
            playlistEntity.id
        );

        List<PlaylistEntity> rows = jpaQuery.fetch();
        long totalCount = countTotal(request);

        return CursorPaginationHelper.buildResponse(
            rows,
            request,
            sortFieldJpa,
            totalCount,
            playlistEntityMapper::toModelWithOwner,
            entity -> sortFieldJpa.extractValue(entity, null),
            PlaylistEntity::getId
        );
    }

    private CursorResponse<PlaylistModel> findAllWithSubscribeCount(
        PlaylistQueryRequest request,
        PlaylistSortFieldJpa sortFieldJpa
    ) {
        Expression<Long> subscribeCount = JPAExpressions
            .select(playlistSubscriberEntity.count())
            .from(playlistSubscriberEntity)
            .where(playlistSubscriberEntity.playlist.id.eq(playlistEntity.id));

        JPAQuery<Tuple> jpaQuery = queryFactory
            .select(playlistEntity, subscribeCount)
            .from(playlistEntity)
            .leftJoin(playlistEntity.owner).fetchJoin()
            .where(
                keywordLike(request.keywordLike()),
                ownerIdEqual(request.ownerIdEqual()),
                subscriberIdEqual(request.subscriberIdEqual()),
                buildSubscribeCountCursorCondition(request, subscribeCount)
            )
            .orderBy(buildSubscribeCountOrderSpecifiers(request, subscribeCount))
            .limit(request.limit() + 1);

        List<Tuple> rows = jpaQuery.fetch();
        long totalCount = countTotal(request);

        return buildSubscribeCountResponse(rows, request, sortFieldJpa, totalCount);
    }

    private BooleanExpression buildSubscribeCountCursorCondition(
        PlaylistQueryRequest request,
        Expression<Long> subscribeCount
    ) {
        String cursor = request.cursor();
        UUID idAfter = request.idAfter();

        if (idAfter == null || !hasText(cursor)) {
            return null;
        }

        boolean isAscending = request.sortDirection().isAscending();
        Long cursorValue = Long.parseLong(cursor);

        NumberExpression<Long> countExpr = Expressions.asNumber(subscribeCount);
        if (isAscending) {
            return countExpr.gt(cursorValue)
                .or(countExpr.eq(cursorValue).and(playlistEntity.id.gt(idAfter)));
        } else {
            return countExpr.lt(cursorValue)
                .or(countExpr.eq(cursorValue).and(playlistEntity.id.lt(idAfter)));
        }
    }

    private OrderSpecifier<?>[] buildSubscribeCountOrderSpecifiers(
        PlaylistQueryRequest request,
        Expression<Long> subscribeCount
    ) {
        Order order = request.sortDirection().isAscending() ? Order.ASC : Order.DESC;

        return new OrderSpecifier<?>[]{
            new OrderSpecifier<>(order, subscribeCount),
            new OrderSpecifier<>(order, playlistEntity.id)
        };
    }

    private CursorResponse<PlaylistModel> buildSubscribeCountResponse(
        List<Tuple> rows,
        PlaylistQueryRequest request,
        PlaylistSortFieldJpa sortFieldJpa,
        long totalCount
    ) {
        String sortByStr = sortFieldJpa.getFieldName();
        SortDirection direction = request.sortDirection();

        if (rows.isEmpty()) {
            return CursorResponse.empty(sortByStr, direction);
        }

        boolean hasNext = rows.size() > request.limit();
        List<Tuple> resultRows = hasNext ? rows.subList(0, request.limit()) : rows;
        List<PlaylistModel> data = resultRows.stream()
            .map(tuple -> playlistEntityMapper.toModelWithOwner(tuple.get(playlistEntity)))
            .toList();

        if (!hasNext) {
            return CursorResponse.of(
                data,
                null,
                null,
                false,
                totalCount,
                sortByStr,
                direction
            );
        }

        Tuple lastRow = resultRows.getLast();
        Long lastSubscribeCount = lastRow.get(1, Long.class);
        String nextCursor = sortFieldJpa.serializeCursor(lastSubscribeCount);
        UUID nextIdAfter = Objects.requireNonNull(lastRow.get(playlistEntity)).getId();

        return CursorResponse.of(
            data,
            nextCursor,
            nextIdAfter,
            true,
            totalCount,
            sortByStr,
            direction
        );
    }

    private long countTotal(PlaylistQueryRequest request) {
        Long total = queryFactory
            .select(playlistEntity.count())
            .from(playlistEntity)
            .where(
                keywordLike(request.keywordLike()),
                ownerIdEqual(request.ownerIdEqual()),
                subscriberIdEqual(request.subscriberIdEqual())
            )
            .fetchOne();
        return total != null ? total : 0;
    }

    private BooleanExpression keywordLike(String keyword) {
        if (!hasText(keyword)) {
            return null;
        }
        return playlistEntity.title.containsIgnoreCase(keyword)
            .or(playlistEntity.description.containsIgnoreCase(keyword));
    }

    private BooleanExpression ownerIdEqual(UUID ownerId) {
        return ownerId != null ? playlistEntity.owner.id.eq(ownerId) : null;
    }

    private BooleanExpression subscriberIdEqual(UUID subscriberId) {
        if (subscriberId == null) {
            return null;
        }
        return playlistEntity.id.in(
            JPAExpressions
                .select(playlistSubscriberEntity.playlist.id)
                .from(playlistSubscriberEntity)
                .where(playlistSubscriberEntity.subscriber.id.eq(subscriberId))
        );
    }
}
