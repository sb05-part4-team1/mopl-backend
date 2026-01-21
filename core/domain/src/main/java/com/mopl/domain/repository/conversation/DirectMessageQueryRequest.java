package com.mopl.domain.repository.conversation;

import com.mopl.domain.support.cursor.CursorRequest;
import com.mopl.domain.support.cursor.SortDirection;
import java.util.UUID;

public record DirectMessageQueryRequest(
    String cursor,
    UUID idAfter,
    Integer limit,
    SortDirection sortDirection,
    DirectMessageSortField sortBy

) implements CursorRequest<DirectMessageSortField> {

    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_LIMIT = 1000;

    public DirectMessageQueryRequest {
        limit = limit != null ? Math.min(limit, MAX_LIMIT) : DEFAULT_LIMIT;
        sortDirection = sortDirection != null ? sortDirection : SortDirection.ASCENDING;
        sortBy = sortBy != null ? sortBy : DirectMessageSortField.createdAt;
    }
}
