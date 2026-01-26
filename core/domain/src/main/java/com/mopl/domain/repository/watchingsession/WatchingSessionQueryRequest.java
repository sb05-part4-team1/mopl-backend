package com.mopl.domain.repository.watchingsession;

import com.mopl.domain.support.cursor.CursorRequest;
import com.mopl.domain.support.cursor.SortDirection;

import java.util.UUID;

public record WatchingSessionQueryRequest(
    String watcherNameLike,
    String cursor,
    UUID idAfter,
    Integer limit,
    SortDirection sortDirection,
    WatchingSessionSortField sortBy
) implements CursorRequest<WatchingSessionSortField> {

    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_LIMIT = 1000;

    public WatchingSessionQueryRequest {
        limit = limit != null ? Math.min(limit, MAX_LIMIT) : DEFAULT_LIMIT;
        sortDirection = sortDirection != null ? sortDirection : SortDirection.ASCENDING;
        sortBy = sortBy != null ? sortBy : WatchingSessionSortField.CREATED_AT;
    }
}
