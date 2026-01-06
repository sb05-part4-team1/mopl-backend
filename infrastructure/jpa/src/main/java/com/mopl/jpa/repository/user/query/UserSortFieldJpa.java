package com.mopl.jpa.repository.user.query;

import com.mopl.domain.repository.user.UserSortField;
import com.mopl.jpa.entity.user.UserEntity;
import com.mopl.jpa.support.cursor.SortField;
import com.querydsl.core.types.dsl.ComparableExpression;

import java.time.Instant;

import static com.mopl.jpa.entity.user.QUserEntity.userEntity;

public enum UserSortFieldJpa implements SortField<Comparable<?>> {

    name {
        @Override
        public ComparableExpression<Comparable<?>> getExpression() {
            return cast(userEntity.name);
        }

        @Override
        public String serializeCursor(Object value) {
            return value != null ? value.toString() : "";
        }

        @Override
        public Comparable<?> deserializeCursor(String cursor) {
            return cursor;
        }
    },

    email {
        @Override
        public ComparableExpression<Comparable<?>> getExpression() {
            return cast(userEntity.email);
        }

        @Override
        public String serializeCursor(Object value) {
            return value != null ? value.toString() : "";
        }

        @Override
        public Comparable<?> deserializeCursor(String cursor) {
            return cursor;
        }
    },

    createdAt {
        @Override
        public ComparableExpression<Comparable<?>> getExpression() {
            return cast(userEntity.createdAt);
        }

        @Override
        public String serializeCursor(Object value) {
            return value instanceof Instant instant ? instant.toString() : "";
        }

        @Override
        public Comparable<?> deserializeCursor(String cursor) {
            return Instant.parse(cursor);
        }
    },

    isLocked {
        @Override
        public ComparableExpression<Comparable<?>> getExpression() {
            return cast(userEntity.locked);
        }

        @Override
        public String serializeCursor(Object value) {
            return value != null ? value.toString() : "false";
        }

        @Override
        public Comparable<?> deserializeCursor(String cursor) {
            return Boolean.parseBoolean(cursor);
        }
    },

    role {
        @Override
        public ComparableExpression<Comparable<?>> getExpression() {
            return cast(userEntity.role.stringValue());
        }

        @Override
        public String serializeCursor(Object value) {
            return value != null ? value.toString() : "";
        }

        @Override
        public Comparable<?> deserializeCursor(String cursor) {
            return cursor;
        }
    };

    @SuppressWarnings("unchecked")
    private static ComparableExpression<Comparable<?>> cast(ComparableExpression<?> expression) {
        return (ComparableExpression<Comparable<?>>) expression;
    }

    public static UserSortFieldJpa from(UserSortField domainField) {
        return switch (domainField) {
            case name -> UserSortFieldJpa.name;
            case email -> UserSortFieldJpa.email;
            case createdAt -> UserSortFieldJpa.createdAt;
            case isLocked -> UserSortFieldJpa.isLocked;
            case role -> UserSortFieldJpa.role;
        };
    }

    public Object extractValue(UserEntity entity) {
        return switch (this) {
            case name -> entity.getName();
            case email -> entity.getEmail();
            case createdAt -> entity.getCreatedAt();
            case isLocked -> entity.isLocked();
            case role -> entity.getRole().name();
        };
    }
}
