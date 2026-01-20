package com.mopl.jpa.repository.watchingsession.query;

import com.mopl.domain.model.watchingsession.WatchingSessionModel;
import com.mopl.domain.repository.watchingsession.WatchingSessionSortField;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.function.Function;

/**
 * WatchingSession 전용 "정렬 필드 + 커서 직렬화/역직렬화" 도구.
 * <p>
 * - 다른 도메인(UserSortFieldJpa)처럼 "SortFieldJpa" 패턴을 따라가되,
 * WatchingSession은 Entity/Querydsl이 없으므로 ComparableExpression 없이 구현한다.
 * - Fake(인메모리) / Redis 구현체 모두에서 재사용 가능하다.
 */
@Getter
@RequiredArgsConstructor
public enum WatchingSessionSortFieldSupport {

    CREATED_AT(
        WatchingSessionSortField.createdAt,
        WatchingSessionModel::getCreatedAt, value -> ((Instant) value).toString(),
        Instant::parse
    );

    private final WatchingSessionSortField domainField;
    private final Function<WatchingSessionModel, Object> valueExtractor;
    private final Function<Object, String> serializer;
    private final Function<String, Comparable<?>> deserializer;

    public static WatchingSessionSortFieldSupport from(WatchingSessionSortField domainField) {
        return switch (domainField) {
            case createdAt -> CREATED_AT;
        };
    }

    public Object extractValue(WatchingSessionModel model) {
        return valueExtractor.apply(model);
    }

    public String serializeCursor(Object value) {
        return value != null ? serializer.apply(value) : "";
    }

    public Comparable<?> deserializeCursor(String cursor) {
        return deserializer.apply(cursor);
    }

    public String getFieldName() {
        return domainField.name();
    }
}
