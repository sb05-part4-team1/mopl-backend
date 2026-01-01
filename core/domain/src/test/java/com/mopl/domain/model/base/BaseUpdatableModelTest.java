package com.mopl.domain.model.base;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BaseUpdatableModel 단위 테스트")
class BaseUpdatableModelTest {

    @SuperBuilder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    static class TestModel extends BaseUpdatableModel {
    }

    @Nested
    @DisplayName("기본 생성자")
    class DefaultConstructorTest {

        @Test
        @DisplayName("모든 필드가 null로 초기화됨")
        void withDefaultConstructor_initializesAllFieldsToNull() {
            // when
            TestModel model = new TestModel();

            // then
            assertThat(model.getId()).isNull();
            assertThat(model.getCreatedAt()).isNull();
            assertThat(model.getDeletedAt()).isNull();
            assertThat(model.getUpdatedAt()).isNull();
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
            TestModel model = TestModel.builder()
                .id(id)
                .createdAt(createdAt)
                .deletedAt(deletedAt)
                .updatedAt(updatedAt)
                .build();

            // then
            assertThat(model.getId()).isEqualTo(id);
            assertThat(model.getCreatedAt()).isEqualTo(createdAt);
            assertThat(model.getDeletedAt()).isNull();
            assertThat(model.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("BaseModel 필드가 상속됨")
        void withBuilder_inheritsBaseModelFields() {
            // given
            UUID id = UUID.randomUUID();
            Instant createdAt = Instant.now();
            Instant deletedAt = Instant.now();
            Instant updatedAt = Instant.now();

            // when
            TestModel model = TestModel.builder()
                .id(id)
                .createdAt(createdAt)
                .deletedAt(deletedAt)
                .updatedAt(updatedAt)
                .build();

            // then
            assertThat(model).isInstanceOf(BaseModel.class);
            assertThat(model.getId()).isEqualTo(id);
            assertThat(model.getCreatedAt()).isEqualTo(createdAt);
            assertThat(model.getDeletedAt()).isEqualTo(deletedAt);
        }
    }
}
