package com.mopl.domain.repository.user;

import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.support.cursor.CursorRequest;
import com.mopl.domain.support.cursor.SortDirection;

import java.util.UUID;

public record UserQueryRequest(
    String emailLike,
    UserModel.Role roleEqual,
    Boolean isLocked,
    String cursor,
    UUID idAfter,
    Integer limit,
    SortDirection sortDirection,
    UserSortField sortBy
) implements CursorRequest<UserSortField> {

    private static final int DEFAULT_LIMIT = 100;

    public UserQueryRequest {
        limit = limit != null ? limit : DEFAULT_LIMIT;
        sortDirection = sortDirection != null ? sortDirection : SortDirection.ASCENDING;
        sortBy = sortBy != null ? sortBy : UserSortField.name;
    }
}
