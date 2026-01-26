package com.mopl.jpa.repository.playlist.query;

import com.mopl.domain.repository.playlist.PlaylistSortField;
import com.mopl.jpa.entity.playlist.PlaylistEntity;
import com.mopl.jpa.support.cursor.SortField;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.function.Function;

import static com.mopl.jpa.entity.playlist.QPlaylistEntity.playlistEntity;

@Getter
@RequiredArgsConstructor
public enum PlaylistSortFieldJpa implements SortField<Comparable<?>> {

    UPDATED_AT(
        PlaylistSortField.UPDATED_AT,
        cast(playlistEntity.updatedAt),
        PlaylistEntity::getUpdatedAt,
        Object::toString,
        Instant::parse
    ),

    SUBSCRIBER_COUNT(
        PlaylistSortField.SUBSCRIBER_COUNT,
        Expressions.comparableTemplate(
            Integer.class,
            "{0}",
            playlistEntity.subscriberCount
        ),
        PlaylistEntity::getSubscriberCount,
        Object::toString,
        Integer::parseInt
    );

    private final PlaylistSortField domainField;
    private final ComparableExpression<Comparable<?>> expression;
    private final Function<PlaylistEntity, Object> valueExtractor;
    private final Function<Object, String> serializer;
    private final Function<String, Comparable<?>> deserializer;

    @SuppressWarnings("unchecked")
    private static ComparableExpression<Comparable<?>> cast(ComparableExpression<?> expression) {
        return (ComparableExpression<Comparable<?>>) expression;
    }

    public static PlaylistSortFieldJpa from(PlaylistSortField domainField) {
        return switch (domainField) {
            case UPDATED_AT -> UPDATED_AT;
            case SUBSCRIBER_COUNT -> SUBSCRIBER_COUNT;
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

    public Object extractValue(PlaylistEntity entity) {
        return valueExtractor.apply(entity);
    }

    @Override
    public String getFieldName() {
        return domainField.name();
    }
}
