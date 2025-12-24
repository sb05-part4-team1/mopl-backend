package com.mopl.domain.model.base;

import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BaseModel 단위 테스트")
class BaseModelTest {

    @SuperBuilder
    static class TestModel extends BaseModel {

        TestModel() {
            super();
        }
    }

    @Nested
    @DisplayName("기본 생성자")
    class DefaultConstructorTest {

        @Test
        @DisplayName("모든 필드가 null로 초기화됨")
        void allFieldsAreNull() {
            // when
            TestModel model = new TestModel();

            // then
            assertThat(model.getId()).isNull();
            assertThat(model.getCreatedAt()).isNull();
            assertThat(model.getDeletedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("SuperBuilder")
    class SuperBuilderTest {

        @Test
        @DisplayName("모든 필드가 주어진 값으로 초기화됨")
        void allFieldsAreInitialized() {
            // given
            UUID id = UUID.randomUUID();
            Instant createdAt = Instant.now();
            Instant deletedAt = null;

            // when
            TestModel model = TestModel.builder()
                .id(id)
                .createdAt(createdAt)
                .deletedAt(deletedAt)
                .build();

            // then
            assertThat(model.getId()).isEqualTo(id);
            assertThat(model.getCreatedAt()).isEqualTo(createdAt);
            assertThat(model.getDeletedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTest {

        @Test
        @DisplayName("삭제되지 않은 모델을 삭제하면 deletedAt이 설정됨")
        void deletesModel() {
            // given
            TestModel model = TestModel.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.now())
                .deletedAt(null)
                .build();
            assertThat(model.isDeleted()).isFalse();

            // when
            model.delete();

            // then
            assertThat(model.isDeleted()).isTrue();
            assertThat(model.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("이미 삭제된 모델을 삭제해도 deletedAt이 변경되지 않음")
        void doesNotChangeDeletedAtIfAlreadyDeleted() {
            // given
            Instant originalDeletedAt = Instant.now().minusSeconds(100);
            TestModel model = TestModel.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.now())
                .deletedAt(originalDeletedAt)
                .build();

            // when
            model.delete();

            // then
            assertThat(model.getDeletedAt()).isEqualTo(originalDeletedAt);
        }
    }

    @Nested
    @DisplayName("restore()")
    class RestoreTest {

        @Test
        @DisplayName("삭제된 모델을 복원하면 deletedAt이 null이 됨")
        void restoresModel() {
            // given
            TestModel model = TestModel.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.now())
                .deletedAt(Instant.now())
                .build();
            assertThat(model.isDeleted()).isTrue();

            // when
            model.restore();

            // then
            assertThat(model.isDeleted()).isFalse();
            assertThat(model.getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("삭제되지 않은 모델을 복원해도 아무 일도 일어나지 않음")
        void doesNothingIfNotDeleted() {
            // given
            TestModel model = TestModel.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.now())
                .deletedAt(null)
                .build();

            // when
            model.restore();

            // then
            assertThat(model.getDeletedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("isDeleted()")
    class IsDeletedTest {

        @Test
        @DisplayName("deletedAt이 null이면 false 반환")
        void returnsFalseWhenDeletedAtIsNull() {
            // given
            TestModel model = TestModel.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.now())
                .deletedAt(null)
                .build();

            // then
            assertThat(model.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("deletedAt이 설정되어 있으면 true 반환")
        void returnsTrueWhenDeletedAtIsSet() {
            // given
            TestModel model = TestModel.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.now())
                .deletedAt(Instant.now())
                .build();

            // then
            assertThat(model.isDeleted()).isTrue();
        }
    }
}
