package com.mopl.jpa.repository.conversation.query;

import static com.mopl.jpa.entity.conversation.QConversationEntity.conversationEntity;
import com.mopl.domain.repository.conversation.ConversationSortField;
import com.mopl.jpa.entity.conversation.ConversationEntity;
import com.mopl.jpa.support.cursor.SortField;
import com.querydsl.core.types.dsl.ComparableExpression;
import java.time.Instant;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConversationSortFieldJpa implements SortField<Comparable<?>> {

    CREATED_AT(
        ConversationSortField.CREATED_AT,
        cast(conversationEntity.createdAt),
        ConversationEntity::getCreatedAt,
        Object::toString,
        Instant::parse
    );

    private final ConversationSortField domainField;
    private final ComparableExpression<Comparable<?>> expression;
    private final Function<ConversationEntity, Object> valueExtractor;
    private final Function<Object, String> serializer;
    private final Function<String, Comparable<?>> deserializer;

    @SuppressWarnings("unchecked")
    private static ComparableExpression<Comparable<?>> cast(ComparableExpression<?> expression) {
        return (ComparableExpression<Comparable<?>>) expression;
    }

    public static ConversationSortFieldJpa from(ConversationSortField domainField) {
        return switch (domainField) {
            case CREATED_AT -> CREATED_AT;
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

    public Object extractValue(ConversationEntity entity) {
        return valueExtractor.apply(entity);
    }

    @Override
    public String getFieldName() {
        return domainField.name();
    }
}
