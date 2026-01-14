package com.mopl.jpa.repository.notification.query;

import com.mopl.domain.repository.notification.NotificationSortField;
import com.mopl.jpa.entity.notification.NotificationEntity;
import com.mopl.jpa.support.cursor.SortField;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.function.Function;

import static com.mopl.jpa.entity.notification.QNotificationEntity.notificationEntity;

@Getter
@RequiredArgsConstructor
public enum NotificationSortFieldJpa implements SortField<Comparable<?>> {

    CREATED_AT(
        NotificationSortField.createdAt,
        cast(notificationEntity.createdAt),
        NotificationEntity::getCreatedAt,
        value -> ((Instant) value).toString(),
        Instant::parse
    );

    private final NotificationSortField domainField;
    private final ComparableExpression<Comparable<?>> expression;
    private final Function<NotificationEntity, Object> valueExtractor;
    private final Function<Object, String> serializer;
    private final Function<String, Comparable<?>> deserializer;

    @SuppressWarnings("unchecked")
    private static ComparableExpression<Comparable<?>> cast(ComparableExpression<?> expression) {
        return (ComparableExpression<Comparable<?>>) expression;
    }

    public static NotificationSortFieldJpa from(NotificationSortField domainField) {
        return switch (domainField) {
            case createdAt -> CREATED_AT;
        };
    }

    @Override
    public SimpleExpression<Comparable<?>> getExpression() {
        return expression;
    }

    @Override
    public BooleanExpression gt(Comparable<?> value) {
        return expression.gt(value);
    }

    @Override
    public BooleanExpression lt(Comparable<?> value) {
        return expression.lt(value);
    }

    @Override
    public BooleanExpression eq(Comparable<?> value) {
        return expression.eq(value);
    }

    @Override
    public String serializeCursor(Object value) {
        return value != null ? serializer.apply(value) : "";
    }

    @Override
    public Comparable<?> deserializeCursor(String cursor) {
        return deserializer.apply(cursor);
    }

    public Object extractValue(NotificationEntity entity) {
        return valueExtractor.apply(entity);
    }

    @Override
    public String getFieldName() {
        return domainField.name();
    }
}
