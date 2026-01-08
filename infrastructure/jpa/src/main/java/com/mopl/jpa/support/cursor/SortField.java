package com.mopl.jpa.support.cursor;

import com.querydsl.core.types.dsl.ComparableExpression;

public interface SortField<T extends Comparable<?>> {

    ComparableExpression<T> getExpression();

    String serializeCursor(Object value);

    T deserializeCursor(String cursor);

    String getFieldName();
}
