package com.mopl.jpa.support.cursor;

import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.springframework.util.StringUtils.hasText;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CursorPaginationHelper {

    public static <T, S extends SortField<?>> void applyCursorPagination(
        CursorRequest<S> request,
        JPAQuery<T> query,
        ComparableExpression<UUID> idExpression
    ) {
        query.where(buildCursorCondition(request, idExpression))
            .orderBy(buildOrderSpecifiers(request, idExpression))
            .limit(request.limit() + 1);
    }

    public static <T, D, S extends SortField<?>> CursorResponse<D> buildResponse(
        List<T> rows,
        CursorRequest<S> request,
        long totalCount,
        Function<T, D> mapper,
        Function<T, Object> cursorValueExtractor,
        Function<T, UUID> idExtractor
    ) {
        S sortField = request.sortBy();
        SortDirection direction = request.sortDirection();
        String sortByStr = sortField.toString().toLowerCase();

        if (rows.isEmpty()) {
            return CursorResponse.empty(sortByStr, direction);
        }

        boolean hasNext = rows.size() > request.limit();
        List<T> resultRows = hasNext ? rows.subList(0, request.limit()) : rows;
        List<D> data = resultRows.stream().map(mapper).toList();

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

        T lastRow = resultRows.get(resultRows.size() - 1);
        String nextCursor = sortField.serializeCursor(cursorValueExtractor.apply(lastRow));
        UUID nextIdAfter = idExtractor.apply(lastRow);

        return CursorResponse.of(data, nextCursor, nextIdAfter, true, totalCount, sortByStr, direction);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <S extends SortField<?>> BooleanExpression buildCursorCondition(
        CursorRequest<S> request,
        ComparableExpression<UUID> idExpression
    ) {
        String cursor = request.cursor();
        UUID idAfter = request.idAfter();

        if (idAfter == null || !hasText(cursor)) {
            return null;
        }

        S sortField = request.sortBy();
        boolean isAscending = request.sortDirection().isAscending();

        ComparableExpression expression = sortField.getExpression();
        Comparable cursorValue = sortField.deserializeCursor(cursor);

        if (isAscending) {
            return expression.gt(cursorValue)
                .or(expression.eq(cursorValue).and(idExpression.gt(idAfter)));
        } else {
            return expression.lt(cursorValue)
                .or(expression.eq(cursorValue).and(idExpression.lt(idAfter)));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <S extends SortField<?>> OrderSpecifier<?>[] buildOrderSpecifiers(
        CursorRequest<S> request,
        ComparableExpression<UUID> idExpression
    ) {
        S sortField = request.sortBy();
        Order order = request.sortDirection().isAscending() ? Order.ASC : Order.DESC;

        ComparableExpression expression = sortField.getExpression();

        return new OrderSpecifier<?>[]{
            new OrderSpecifier<>(order, expression),
            new OrderSpecifier<>(order, idExpression)
        };
    }
}
