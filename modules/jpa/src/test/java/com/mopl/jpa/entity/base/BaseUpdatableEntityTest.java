package com.mopl.jpa.entity.base;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BaseUpdatableEntity 단위 테스트")
class BaseUpdatableEntityTest {

    static class TestEntity extends BaseUpdatableEntity {

        TestEntity() {
            super();
        }

        TestEntity(UUID id, Instant createdAt, Instant deletedAt, Instant updatedAt) {
            super(id, createdAt, deletedAt, updatedAt);
        }
    }

    @Nested
    @DisplayName("기본 생성자")
    class DefaultConstructorTest {

        @Test
        @DisplayName("모든 필드가 null로 초기화됨")
        void allFieldsAreNull() {
            // when
            TestEntity entity = new TestEntity();

            // then
            assertThat(entity.getId()).isNull();
            assertThat(entity.getCreatedAt()).isNull();
            assertThat(entity.getDeletedAt()).isNull();
            assertThat(entity.getUpdatedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("전체 필드 생성자")
    class AllArgsConstructorTest {

        @Test
        @DisplayName("모든 필드가 주어진 값으로 초기화됨")
        void allFieldsAreInitialized() {
            // given
            UUID id = UUID.randomUUID();
            Instant createdAt = Instant.now();
            Instant deletedAt = null;
            Instant updatedAt = Instant.now();

            // when
            TestEntity entity = new TestEntity(id, createdAt, deletedAt, updatedAt);

            // then
            assertThat(entity.getId()).isEqualTo(id);
            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getDeletedAt()).isNull();
            assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("BaseEntity 필드가 상속됨")
        void inheritsBaseEntityFields() {
            // given
            UUID id = UUID.randomUUID();
            Instant createdAt = Instant.now();
            Instant deletedAt = Instant.now();
            Instant updatedAt = Instant.now();

            // when
            TestEntity entity = new TestEntity(id, createdAt, deletedAt, updatedAt);

            // then
            assertThat(entity).isInstanceOf(BaseEntity.class);
            assertThat(entity.getId()).isEqualTo(id);
            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getDeletedAt()).isEqualTo(deletedAt);
        }
    }
}
