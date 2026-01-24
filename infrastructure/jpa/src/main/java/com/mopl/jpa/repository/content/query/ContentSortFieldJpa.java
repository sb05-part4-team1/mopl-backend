package com.mopl.jpa.repository.content.query;

import com.mopl.domain.repository.content.ContentSortField;
import com.mopl.jpa.entity.content.ContentEntity;
import com.mopl.jpa.support.cursor.SortField;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.function.Function;

import static com.mopl.jpa.entity.content.QContentEntity.contentEntity;

@Getter
@RequiredArgsConstructor
public enum ContentSortFieldJpa implements SortField<Comparable<?>> {

    WATCHER_COUNT(
        ContentSortField.watcherCount,
        Expressions.comparableTemplate(
            Integer.class,
            "{0}",
            contentEntity.reviewCount
        ),
        ContentEntity::getReviewCount,
        Object::toString,
        Integer::parseInt
    ),

    CREATED_AT(
        ContentSortField.createdAt,
        cast(contentEntity.createdAt),
        ContentEntity::getCreatedAt,
        Object::toString,
        Instant::parse
    ),

    RATE(
        ContentSortField.rate,
        Expressions.comparableTemplate(
            Double.class,
            "{0}",
            contentEntity.averageRating
        ),
        ContentEntity::getAverageRating,
        Object::toString,
        Double::parseDouble
    );

    private final ContentSortField domainField;
    private final ComparableExpression<Comparable<?>> expression;
    private final Function<ContentEntity, Object> valueExtractor;
    private final Function<Object, String> serializer;
    private final Function<String, Comparable<?>> deserializer;

    @SuppressWarnings("unchecked")
    private static ComparableExpression<Comparable<?>> cast(
        ComparableExpression<?> expression
    ) {
        return (ComparableExpression<Comparable<?>>) expression;
    }

    public static ContentSortFieldJpa from(ContentSortField domainField) {
        return switch (domainField) {
            case createdAt -> CREATED_AT;
            case watcherCount -> WATCHER_COUNT;
            case rate -> RATE;
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

    public Object extractValue(ContentEntity entity) {
        return valueExtractor.apply(entity);
    }

    @Override
    public String getFieldName() {
        return domainField.name();
    }
}
