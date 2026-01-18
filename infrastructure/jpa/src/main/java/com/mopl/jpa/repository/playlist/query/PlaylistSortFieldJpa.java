package com.mopl.jpa.repository.playlist.query;

import com.mopl.domain.repository.playlist.PlaylistSortField;
import com.mopl.jpa.entity.playlist.PlaylistEntity;
import com.mopl.jpa.support.cursor.SortField;
import com.querydsl.core.types.dsl.ComparableExpression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.function.Function;

import static com.mopl.jpa.entity.playlist.QPlaylistEntity.playlistEntity;

@Getter
@RequiredArgsConstructor
public enum PlaylistSortFieldJpa implements SortField<Comparable<?>> {

    UPDATED_AT(
        PlaylistSortField.updatedAt,
        cast(playlistEntity.updatedAt),
        PlaylistEntity::getUpdatedAt,
        Object::toString,
        Instant::parse
    ),

    SUBSCRIBE_COUNT(
        PlaylistSortField.subscribeCount,
        null,
        null,
        Object::toString,
        Long::parseLong
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
            case updatedAt -> UPDATED_AT;
            case subscribeCount -> SUBSCRIBE_COUNT;
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

    public Object extractValue(PlaylistEntity entity, Long subscribeCount) {
        if (this == SUBSCRIBE_COUNT) {
            return subscribeCount;
        }
        return valueExtractor != null ? valueExtractor.apply(entity) : null;
    }

    @Override
    public String getFieldName() {
        return domainField.name();
    }
}
