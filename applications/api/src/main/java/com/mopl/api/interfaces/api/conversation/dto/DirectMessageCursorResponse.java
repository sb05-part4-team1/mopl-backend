package com.mopl.api.interfaces.api.conversation.dto;

import com.mopl.domain.support.cursor.SortDirection;
import java.util.List;
import java.util.UUID;

public record DirectMessageCursorResponse(
    List<DirectMessageResponse> data,
    String nextCursor,
    UUID nextIdAfter,
    boolean hasNext,
    long totalCount,
    String sortBy,
    SortDirection sortDirection
) {
}
