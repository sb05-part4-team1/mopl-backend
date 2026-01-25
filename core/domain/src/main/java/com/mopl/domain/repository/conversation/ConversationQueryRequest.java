package com.mopl.domain.repository.conversation;

import com.mopl.domain.support.cursor.CursorRequest;
import com.mopl.domain.support.cursor.SortDirection;

import java.util.UUID;

public record ConversationQueryRequest(
    String keywordLike,
    String cursor,
    UUID idAfter,
    Integer limit,
    SortDirection sortDirection,
    ConversationSortField sortBy
) implements CursorRequest<ConversationSortField> {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    public ConversationQueryRequest {
        limit = limit != null ? Math.min(limit, MAX_LIMIT) : DEFAULT_LIMIT;
        sortDirection = sortDirection != null ? sortDirection : SortDirection.DESCENDING;
        sortBy = sortBy != null ? sortBy : ConversationSortField.createdAt;
    }
}
