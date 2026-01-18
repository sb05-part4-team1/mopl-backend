package com.mopl.jpa.repository.review.query;

import com.mopl.domain.repository.review.ReviewSortField;
import com.mopl.jpa.entity.review.ReviewEntity;
import com.mopl.jpa.support.cursor.SortField;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.function.Function;

import static com.mopl.jpa.entity.review.QReviewEntity.reviewEntity;

@Getter
@RequiredArgsConstructor
public enum ReviewSortFieldJpa implements SortField<Comparable<?>> {

    CREATED_AT(
        ReviewSortField.createdAt,
        cast(reviewEntity.createdAt),
        ReviewEntity::getCreatedAt,
        Object::toString,
        Instant::parse
    ),

    RATING(
        ReviewSortField.rating,
        Expressions.comparableTemplate(
            Double.class,
            "{0}",
            reviewEntity.rating
        ),
        ReviewEntity::getRating,
        Object::toString,
        Double::parseDouble
    );

    private final ReviewSortField domainField;
    private final ComparableExpression<Comparable<?>> expression;
    private final Function<ReviewEntity, Object> valueExtractor;
    private final Function<Object, String> serializer;
    private final Function<String, Comparable<?>> deserializer;

    @SuppressWarnings("unchecked")
    private static ComparableExpression<Comparable<?>> cast(ComparableExpression<?> expression) {
        return (ComparableExpression<Comparable<?>>) expression;
    }

    public static ReviewSortFieldJpa from(ReviewSortField domainField) {
        return switch (domainField) {
            case createdAt -> CREATED_AT;
            case rating -> RATING;
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

    public Object extractValue(ReviewEntity entity) {
        return valueExtractor.apply(entity);
    }

    @Override
    public String getFieldName() {
        return domainField.name();
    }
}
