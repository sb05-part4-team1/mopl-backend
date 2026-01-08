package com.mopl.domain.support.cursor;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public record CursorResponse<T>(
    List<T> data,
    String nextCursor,
    UUID nextIdAfter,
    boolean hasNext,
    long totalCount,
    String sortBy,
    SortDirection sortDirection
) {

    public static <T> CursorResponse<T> of(
        List<T> data,
        String nextCursor,
        UUID nextIdAfter,
        boolean hasNext,
        long totalCount,
        String sortBy,
        SortDirection sortDirection
    ) {
        return new CursorResponse<>(
            data,
            nextCursor,
            nextIdAfter,
            hasNext,
            totalCount,
            sortBy,
            sortDirection
        );
    }

    public static <T> CursorResponse<T> empty(
        String sortBy,
        SortDirection sortDirection
    ) {
        return new CursorResponse<>(
            List.of(),
            null,
            null,
            false,
            0,
            sortBy,
            sortDirection
        );
    }

    public <R> CursorResponse<R> map(Function<T, R> mapper) {
        return new CursorResponse<>(
            data.stream().map(mapper).toList(),
            nextCursor,
            nextIdAfter,
            hasNext,
            totalCount,
            sortBy,
            sortDirection
        );
    }
}
