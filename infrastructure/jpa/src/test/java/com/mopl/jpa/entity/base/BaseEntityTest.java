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

@DisplayName("BaseEntity 단위 테스트")
class BaseEntityTest {

    @SuperBuilder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    static class TestEntity extends BaseEntity {
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
            Instant deletedAt = Instant.now();

            // when
            TestEntity entity = TestEntity.builder()
                .id(id)
                .createdAt(createdAt)
                .deletedAt(deletedAt)
                .build();

            // then
            assertThat(entity.getId()).isEqualTo(id);
            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getDeletedAt()).isEqualTo(deletedAt);
        }
    }

    @Nested
    @DisplayName("equals() / hashCode()")
    class EqualsHashCodeTest {

        @Test
        @DisplayName("같은 id를 가진 엔티티는 동등함")
        void withSameId_returnsEqual() {
            // given
            UUID id = UUID.randomUUID();
            TestEntity entity1 = TestEntity.builder()
                .id(id)
                .createdAt(Instant.now())
                .build();
            TestEntity entity2 = TestEntity.builder()
                .id(id)
                .createdAt(Instant.now().plusSeconds(100))
                .build();

            // then
            assertThat(entity1).isEqualTo(entity2);
            assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
        }

        @Test
        @DisplayName("다른 id를 가진 엔티티는 동등하지 않음")
        void withDifferentId_returnsNotEqual() {
            // given
            TestEntity entity1 = TestEntity.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.now())
                .build();
            TestEntity entity2 = TestEntity.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.now())
                .build();

            // then
            assertThat(entity1).isNotEqualTo(entity2);
        }
    }
}
