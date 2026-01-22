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

@DisplayName("BaseModel 단위 테스트")
class BaseModelTest {

    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @SuperBuilder(toBuilder = true)
    static class TestModel extends BaseModel {
    }

    @DisplayName("delete()")
    @Nested
    class DeleteTest {

        @DisplayName("삭제되지 않은 모델을 삭제하면 deletedAt이 설정됨")
        @Test
        void withNotDeletedModel_setsDeletedAt() {
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

        @DisplayName("이미 삭제된 모델을 삭제해도 deletedAt이 변경되지 않음")
        @Test
        void withAlreadyDeletedModel_doesNotChangeDeletedAt() {
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

    @DisplayName("restore()")
    @Nested
    class RestoreTest {

        @DisplayName("삭제된 모델을 복원하면 deletedAt이 null이 됨")
        @Test
        void withDeletedModel_setsDeletedAtToNull() {
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

        @DisplayName("삭제되지 않은 모델을 복원해도 아무 일도 일어나지 않음")
        @Test
        void withNotDeletedModel_doesNothing() {
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

    @DisplayName("isDeleted()")
    @Nested
    class IsDeletedTest {

        @DisplayName("deletedAt이 null이면 false 반환")
        @Test
        void withDeletedAtNull_returnsFalse() {
            // given
            TestModel model = TestModel.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.now())
                .deletedAt(null)
                .build();

            // then
            assertThat(model.isDeleted()).isFalse();
        }

        @DisplayName("deletedAt이 설정되어 있으면 true 반환")
        @Test
        void withDeletedAtSet_returnsTrue() {
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
