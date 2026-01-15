package com.mopl.domain.repository.notification;

import com.mopl.domain.support.cursor.CursorRequest;
import com.mopl.domain.support.cursor.SortDirection;

import java.util.UUID;

public record NotificationQueryRequest(
    String cursor,
    UUID idAfter,
    Integer limit,
    SortDirection sortDirection,
    NotificationSortField sortBy
) implements CursorRequest<NotificationSortField> {

    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_LIMIT = 1000;

    public NotificationQueryRequest {
        limit = limit != null ? Math.min(limit, MAX_LIMIT) : DEFAULT_LIMIT;
        sortDirection = sortDirection != null ? sortDirection : SortDirection.ASCENDING;
        sortBy = sortBy != null ? sortBy : NotificationSortField.createdAt;
    }
}
