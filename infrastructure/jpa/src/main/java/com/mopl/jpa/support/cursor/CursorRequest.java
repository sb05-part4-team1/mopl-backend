package com.mopl.jpa.support.cursor;

import java.util.UUID;

public interface CursorRequest<S extends SortField<?>> {

    String cursor();

    UUID idAfter();

    Integer limit();

    S sortField();

    com.mopl.domain.support.cursor.SortDirection sortDirection();
}
