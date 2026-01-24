package com.mopl.redis.repository.watchingsession.query;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.repository.watchingsession.WatchingSessionSortField;
import com.mopl.redis.support.cursor.RedisSortField;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.function.Function;

@Getter
@RequiredArgsConstructor
public enum WatchingSessionSortFieldSupport implements RedisSortField<Instant> {

    CREATED_AT(
        WatchingSessionSortField.createdAt,
        WatchingSessionModel::getCreatedAt
    );

    private final WatchingSessionSortField domainField;
    private final Function<WatchingSessionModel, Instant> valueExtractor;

    public static WatchingSessionSortFieldSupport from(WatchingSessionSortField domainField) {
        return switch (domainField) {
            case createdAt -> CREATED_AT;
        };
    }

    public Instant extractValue(WatchingSessionModel model) {
        return valueExtractor.apply(model);
    }

    @Override
    public String serializeCursor(Instant value) {
        return value != null ? value.toString() : "";
    }

    @Override
    public Instant deserializeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(cursor.trim());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getFieldName() {
        return domainField.name();
    }
}
