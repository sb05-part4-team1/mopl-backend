package com.mopl.domain.support.cursor;

import java.util.UUID;

public interface CursorRequest<S extends Enum<S>> {

    String cursor();

    UUID idAfter();

    Integer limit();

    SortDirection sortDirection();

    S sortBy();
}
