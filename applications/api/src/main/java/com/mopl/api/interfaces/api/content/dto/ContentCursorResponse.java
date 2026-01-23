package com.mopl.api.interfaces.api.content.dto;

import com.mopl.domain.support.cursor.SortDirection;

import java.util.List;
import java.util.UUID;

public record ContentCursorResponse(
    List<ContentResponse> data,
    String nextCursor,
    UUID nextIdAfter,
    boolean hasNext,
    long totalCount,
    String sortBy,
    SortDirection sortDirection
) {
}
