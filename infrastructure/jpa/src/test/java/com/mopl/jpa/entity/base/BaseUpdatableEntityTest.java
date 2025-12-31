package com.mopl.jpa.entity.base;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BaseUpdatableEntity 단위 테스트")
class BaseUpdatableEntityTest {

    @SuperBuilder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    static class TestEntity extends BaseUpdatableEntity {
    }

    @Nested
    @DisplayName("기본 생성자")
    class DefaultConstructorTest {

        @Test
        @DisplayName("모든 필드가 null로 초기화됨")
        void withDefaultConstructor_initializesAllFieldsToNull() {
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
    @DisplayName("SuperBuilder")
    class SuperBuilderTest {

        @Test
        @DisplayName("모든 필드가 주어진 값으로 초기화됨")
        void withBuilder_initializesAllFields() {
            // given
            UUID id = UUID.randomUUID();
            Instant createdAt = Instant.now();
            Instant deletedAt = null;
            Instant updatedAt = Instant.now();

            // when
            TestEntity entity = TestEntity.builder()
                .id(id)
                .createdAt(createdAt)
                .deletedAt(deletedAt)
                .updatedAt(updatedAt)
                .build();

            // then
            assertThat(entity.getId()).isEqualTo(id);
            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getDeletedAt()).isNull();
            assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("BaseEntity 필드가 상속됨")
        void withBuilder_inheritsBaseEntityFields() {
            // given
            UUID id = UUID.randomUUID();
            Instant createdAt = Instant.now();
            Instant deletedAt = Instant.now();
            Instant updatedAt = Instant.now();

            // when
            TestEntity entity = TestEntity.builder()
                .id(id)
                .createdAt(createdAt)
                .deletedAt(deletedAt)
                .updatedAt(updatedAt)
                .build();

            // then
            assertThat(entity).isInstanceOf(BaseEntity.class);
            assertThat(entity.getId()).isEqualTo(id);
            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getDeletedAt()).isEqualTo(deletedAt);
        }
    }
}
