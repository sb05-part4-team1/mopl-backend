package com.mopl.domain.repository.review;

import com.mopl.domain.support.cursor.CursorRequest;
import com.mopl.domain.support.cursor.SortDirection;

import java.util.UUID;

public record ReviewQueryRequest(
    UUID contentId,
    String cursor,
    UUID idAfter,
    Integer limit,
    SortDirection sortDirection,
    ReviewSortField sortBy
) implements CursorRequest<ReviewSortField> {

    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_LIMIT = 1000;

    public ReviewQueryRequest {
        limit = limit != null ? Math.min(limit, MAX_LIMIT) : DEFAULT_LIMIT;
        sortDirection = sortDirection != null ? sortDirection : SortDirection.DESCENDING;
        sortBy = sortBy != null ? sortBy : ReviewSortField.createdAt;
    }
}
