package com.mopl.jpa.support.cursor;

import com.mopl.domain.support.cursor.CursorRequest;
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

    public static <T, S extends Enum<S>> void applyCursorPagination(
        CursorRequest<S> request,
        SortField<?> sortField,
        JPAQuery<T> query,
        ComparableExpression<UUID> idExpression
    ) {
        query.where(buildCursorCondition(request, sortField, idExpression))
            .orderBy(buildOrderSpecifiers(request, sortField, idExpression))
            .limit(request.limit() + 1);
    }

    public static <T, D, S extends Enum<S>> CursorResponse<D> buildResponse(
        List<T> rows,
        CursorRequest<S> request,
        SortField<?> sortField,
        long totalCount,
        Function<T, D> mapper,
        Function<T, Object> cursorValueExtractor,
        Function<T, UUID> idExtractor
    ) {
        SortDirection direction = request.sortDirection();
        String sortByStr = request.sortBy().name().toLowerCase();

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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <S extends Enum<S>> BooleanExpression buildCursorCondition(
        CursorRequest<S> request,
        SortField<?> sortField,
        ComparableExpression<UUID> idExpression
    ) {
        String cursor = request.cursor();
        UUID idAfter = request.idAfter();

        if (idAfter == null || !hasText(cursor)) {
            return null;
        }

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
    private static <S extends Enum<S>> OrderSpecifier<?>[] buildOrderSpecifiers(
        CursorRequest<S> request,
        SortField<?> sortField,
        ComparableExpression<UUID> idExpression
    ) {
        Order order = request.sortDirection().isAscending() ? Order.ASC : Order.DESC;

        ComparableExpression expression = sortField.getExpression();

        return new OrderSpecifier<?>[]{
            new OrderSpecifier<>(order, expression),
            new OrderSpecifier<>(order, idExpression)
        };
    }
}
