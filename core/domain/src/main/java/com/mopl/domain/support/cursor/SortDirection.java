package com.mopl.domain.support.cursor;

public enum SortDirection {

    ASCENDING,
    DESCENDING;

    public boolean isAscending() {
        return this == ASCENDING;
    }
}
