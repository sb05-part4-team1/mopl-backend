package com.mopl.jpa.repository.conversation.query;

import static com.mopl.jpa.entity.conversation.QDirectMessageEntity.directMessageEntity;

import com.mopl.domain.repository.conversation.DirectMessageSortField;
import com.mopl.jpa.entity.conversation.DirectMessageEntity;
import com.mopl.jpa.support.cursor.SortField;
import com.querydsl.core.types.dsl.ComparableExpression;
import java.time.Instant;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DirectMessageSortFieldJpa implements SortField<Comparable<?>> {

    CREATED_AT(
        DirectMessageSortField.createdAt,
        cast(directMessageEntity.createdAt),
        DirectMessageEntity::getCreatedAt,
        Object::toString,
        Instant::parse
    );

    private final DirectMessageSortField domainField;
    private final ComparableExpression<Comparable<?>> expression;
    private final Function<DirectMessageEntity, Object> valueExtractor;
    private final Function<Object, String> serializer;
    private final Function<String, Comparable<?>> deserializer;

    @SuppressWarnings("unchecked")
    private static ComparableExpression<Comparable<?>> cast(ComparableExpression<?> expression) {
        return (ComparableExpression<Comparable<?>>) expression;
    }

    public static DirectMessageSortFieldJpa from(DirectMessageSortField domainField) {
        return switch (domainField) {
            case createdAt -> CREATED_AT;
        };
    }

    @Override
    public String serializeCursor(Object value) {
        return value != null ? serializer.apply(value) : "";
    }

    @Override
    public Comparable<?> deserializeCursor(String cursor) {
        return deserializer.apply(cursor);
    }

    public Object extractValue(DirectMessageEntity entity) {
        return valueExtractor.apply(entity);
    }

    @Override
    public String getFieldName() {
        return domainField.name();
    }
}
