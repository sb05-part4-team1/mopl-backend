package com.mopl.domain.repository.playlist;

import com.mopl.domain.support.cursor.CursorRequest;
import com.mopl.domain.support.cursor.SortDirection;

import java.util.UUID;

public record PlaylistQueryRequest(
    String keywordLike,
    UUID ownerIdEqual,
    UUID subscriberIdEqual,
    String cursor,
    UUID idAfter,
    Integer limit,
    SortDirection sortDirection,
    PlaylistSortField sortBy
) implements CursorRequest<PlaylistSortField> {

    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_LIMIT = 1000;

    public PlaylistQueryRequest {
        limit = limit != null ? Math.min(limit, MAX_LIMIT) : DEFAULT_LIMIT;
        sortDirection = sortDirection != null ? sortDirection : SortDirection.ASCENDING;
        sortBy = sortBy != null ? sortBy : PlaylistSortField.UPDATED_AT;
    }
}
