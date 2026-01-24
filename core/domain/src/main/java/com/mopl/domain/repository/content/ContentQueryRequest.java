package com.mopl.domain.repository.content;

import com.mopl.domain.model.content.ContentModel.ContentType;
import com.mopl.domain.support.cursor.CursorRequest;
import com.mopl.domain.support.cursor.SortDirection;

import java.util.List;
import java.util.UUID;

public record ContentQueryRequest(
    ContentType typeEqual,
    String keywordLike,
    List<String> tagsIn,
    String cursor,
    UUID idAfter,
    Integer limit,
    SortDirection sortDirection,
    ContentSortField sortBy
) implements CursorRequest<ContentSortField> {

    public static final int DEFAULT_LIMIT = 20;
    public static final int MAX_LIMIT = 100;

    public ContentQueryRequest {
        limit = limit != null ? Math.min(limit, MAX_LIMIT) : DEFAULT_LIMIT;
        sortDirection = sortDirection != null ? sortDirection : SortDirection.DESCENDING;
        sortBy = sortBy != null ? sortBy : ContentSortField.watcherCount;
    }
}
