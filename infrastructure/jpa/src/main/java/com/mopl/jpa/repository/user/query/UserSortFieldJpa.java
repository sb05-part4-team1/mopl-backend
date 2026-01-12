package com.mopl.jpa.repository.user.query;

import com.mopl.domain.repository.user.UserSortField;
import com.mopl.jpa.entity.user.UserEntity;
import com.mopl.jpa.support.cursor.SortField;
import com.querydsl.core.types.dsl.ComparableExpression;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.function.Function;

import static com.mopl.jpa.entity.user.QUserEntity.userEntity;

@Getter
@RequiredArgsConstructor
public enum UserSortFieldJpa implements SortField<Comparable<?>> {

    NAME(
        UserSortField.name,
        cast(userEntity.name),
        UserEntity::getName,
        Object::toString, cursor -> cursor
    ),

    EMAIL(
        UserSortField.email,
        cast(userEntity.email),
        UserEntity::getEmail,
        Object::toString, cursor -> cursor
    ),

    CREATED_AT(UserSortField.createdAt, cast(userEntity.createdAt),
        UserEntity::getCreatedAt, value -> ((Instant) value).toString(), Instant::parse),

    IS_LOCKED(
        UserSortField.isLocked,
        cast(userEntity.locked),
        UserEntity::isLocked,
        Object::toString, Boolean::parseBoolean
    ),

    ROLE(UserSortField.role, cast(userEntity.role.stringValue()),
        entity -> entity.getRole().name(), Object::toString, cursor -> cursor);

    private final UserSortField domainField;
    private final ComparableExpression<Comparable<?>> expression;
    private final Function<UserEntity, Object> valueExtractor;
    private final Function<Object, String> serializer;
    private final Function<String, Comparable<?>> deserializer;

    @SuppressWarnings("unchecked")
    private static ComparableExpression<Comparable<?>> cast(ComparableExpression<?> expression) {
        return (ComparableExpression<Comparable<?>>) expression;
    }

    public static UserSortFieldJpa from(UserSortField domainField) {
        return switch (domainField) {
            case name -> NAME;
            case email -> EMAIL;
            case createdAt -> CREATED_AT;
            case isLocked -> IS_LOCKED;
            case role -> ROLE;
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

    public Object extractValue(UserEntity entity) {
        return valueExtractor.apply(entity);
    }

    @Override
    public String getFieldName() {
        return domainField.name();
    }
}
