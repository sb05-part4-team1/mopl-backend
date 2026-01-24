package com.mopl.redis.support.cursor;

import com.mopl.domain.support.cursor.CursorRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.springframework.util.StringUtils.hasText;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RedisCursorPaginationHelper {

    public static <T, S extends Enum<S>, F extends Comparable<F>> List<T> applyCursor(
        List<T> items,
        CursorRequest<S> request,
        RedisSortField<F> sortField,
        Function<T, F> fieldExtractor,
        Function<T, UUID> idExtractor
    ) {
        String cursor = request.cursor();
        UUID idAfter = request.idAfter();

        if (idAfter == null || !hasText(cursor)) {
            return items;
        }

        F cursorValue = sortField.deserializeCursor(cursor);
        if (cursorValue == null) {
            return items;
        }

        boolean isAscending = request.sortDirection().isAscending();

        return items.stream()
            .filter(item -> {
                F fieldValue = fieldExtractor.apply(item);
                UUID itemId = idExtractor.apply(item);

                if (fieldValue == null || itemId == null) {
                    return false;
                }

                int cmp = fieldValue.compareTo(cursorValue);

                if (isAscending) {
                    return (cmp > 0) || (cmp == 0 && itemId.compareTo(idAfter) > 0);
                }
                return (cmp < 0) || (cmp == 0 && itemId.compareTo(idAfter) < 0);
            })
            .toList();
    }

    public static <T, S extends Enum<S>, F extends Comparable<F>> CursorResponse<T> buildResponse(
        List<T> afterCursor,
        CursorRequest<S> request,
        RedisSortField<F> sortField,
        long totalCount,
        Function<T, F> cursorValueExtractor,
        Function<T, UUID> idExtractor
    ) {
        SortDirection direction = request.sortDirection();
        String sortByField = sortField.getFieldName();

        if (afterCursor.isEmpty()) {
            return CursorResponse.empty(sortByField, direction);
        }

        int limit = request.limit();
        boolean hasNext = afterCursor.size() > limit;
        int resultSize = Math.min(limit, afterCursor.size());
        List<T> result = afterCursor.subList(0, resultSize);

        if (!hasNext) {
            return CursorResponse.of(
                result,
                null,
                null,
                false,
                totalCount,
                sortByField,
                direction
            );
        }

        T last = result.getLast();
        F cursorValue = cursorValueExtractor.apply(last);
        String nextCursor = sortField.serializeCursor(cursorValue);
        UUID nextIdAfter = idExtractor.apply(last);

        return CursorResponse.of(
            result,
            nextCursor,
            nextIdAfter,
            true,
            totalCount,
            sortByField,
            direction
        );
    }

    public static <T, F extends Comparable<F>> int compareByFieldThenId(
        T a,
        T b,
        Function<T, F> fieldExtractor,
        Function<T, UUID> idExtractor,
        SortDirection direction
    ) {
        F aField = fieldExtractor.apply(a);
        F bField = fieldExtractor.apply(b);
        int cmpField = compareNullSafe(aField, bField);

        UUID aId = idExtractor.apply(a);
        UUID bId = idExtractor.apply(b);
        int cmpId = compareNullSafe(aId, bId);

        int result = (cmpField != 0) ? cmpField : cmpId;

        return direction.isAscending() ? result : -result;
    }

    private static <C extends Comparable<C>> int compareNullSafe(C a, C b) {
        if (a == null && b == null) {
            return 0;
        } else if (a == null) {
            return 1;
        } else if (b == null) {
            return -1;
        }
        return a.compareTo(b);
    }
}
