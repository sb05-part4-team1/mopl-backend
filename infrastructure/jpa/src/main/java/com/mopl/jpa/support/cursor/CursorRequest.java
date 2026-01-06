package com.mopl.jpa.support.cursor;

import com.mopl.domain.support.cursor.SortDirection;

import java.util.UUID;

public interface CursorRequest<S extends SortField<?>> {

    String cursor();

    UUID idAfter();

    Integer limit();

    SortDirection sortDirection();

    S sortBy();
}
