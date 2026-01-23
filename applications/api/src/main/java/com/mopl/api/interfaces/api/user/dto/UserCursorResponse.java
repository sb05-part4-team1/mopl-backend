package com.mopl.api.interfaces.api.user.dto;

import com.mopl.domain.support.cursor.SortDirection;

import java.util.List;
import java.util.UUID;

public record UserCursorResponse(
    List<UserResponse> data,
    String nextCursor,
    UUID nextIdAfter,
    boolean hasNext,
    long totalCount,
    String sortBy,
    SortDirection sortDirection
) {
}
