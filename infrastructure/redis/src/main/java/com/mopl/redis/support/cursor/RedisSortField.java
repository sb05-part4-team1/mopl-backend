package com.mopl.redis.support.cursor;

public interface RedisSortField<T extends Comparable<T>> {

    String serializeCursor(T value);

    T deserializeCursor(String cursor);

    String getFieldName();
}
