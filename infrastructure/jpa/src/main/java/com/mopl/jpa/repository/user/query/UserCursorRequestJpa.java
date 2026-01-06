package com.mopl.jpa.repository.user.query;

import com.mopl.domain.repository.user.UserQueryRequest;
import com.mopl.domain.support.cursor.SortDirection;
import com.mopl.jpa.support.cursor.CursorRequest;

import java.util.UUID;

public record UserCursorRequestJpa(
    UserQueryRequest domainRequest,
    UserSortFieldJpa sortFieldJpa
) implements CursorRequest<UserSortFieldJpa> {

    public static UserCursorRequestJpa from(UserQueryRequest request) {
        return new UserCursorRequestJpa(
            request,
            UserSortFieldJpa.from(request.sortBy())
        );
    }

    @Override
    public String cursor() {
        return domainRequest.cursor();
    }

    @Override
    public UUID idAfter() {
        return domainRequest.idAfter();
    }

    @Override
    public Integer limit() {
        return domainRequest.limit();
    }

    @Override
    public UserSortFieldJpa sortBy() {
        return sortFieldJpa;
    }

    @Override
    public SortDirection sortDirection() {
        return domainRequest.sortDirection();
    }
}
