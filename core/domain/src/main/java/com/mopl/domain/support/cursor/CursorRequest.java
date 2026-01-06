package com.mopl.domain.support.cursor;

import java.util.UUID;

public interface CursorRequest {

    String cursor();

    UUID idAfter();

    Integer limit();

    SortDirection sortDirection();
}
