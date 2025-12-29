package com.mopl.domain.model.tag;

import com.mopl.domain.exception.tag.InvalidTagDataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.mopl.domain.model.tag.TagModel.NAME_MAX_LENGTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TagModel 단위 테스트")
class TagModelTest {

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("유효한 이름으로 TagModel 생성 및 공백 제거 확인")
        void withValidName_createsTagModel() {
            // when
            TagModel tag = TagModel.create("  SF  ");

            // then
            assertThat(tag.getName()).isEqualTo("SF");
            assertThat(tag.isDeleted()).isFalse();
        }

        static Stream<Arguments> invalidNameProvider() {
            return Stream.of(
                Arguments.of("null", null),
                Arguments.of("빈 문자열", ""),
                Arguments.of("공백만", "   ")
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidNameProvider")
        @DisplayName("이름이 비어있으면 예외 발생")
        void withEmptyName_throwsException(String description, String name) {
            assertThatThrownBy(() -> TagModel.create(name))
                .isInstanceOf(InvalidTagDataException.class)
                .satisfies(e -> {
                    InvalidTagDataException ex = (InvalidTagDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("태그 이름은 비어있을 수 없습니다.");
                });
        }

        @Test
        @DisplayName("이름이 제한 길이를 초과하면 예외 발생")
        void withNameExceedingMaxLength_throwsException() {
            String longName = "a".repeat(NAME_MAX_LENGTH + 1);

            assertThatThrownBy(() -> TagModel.create(longName))
                .isInstanceOf(InvalidTagDataException.class)
                .satisfies(e -> {
                    InvalidTagDataException ex = (InvalidTagDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("태그 이름은 " + NAME_MAX_LENGTH + "자를 초과할 수 없습니다.");
                });
        }
    }

    @Nested
    @DisplayName("BaseModel 기능 테스트 (Soft Delete)")
    class BaseModelTest {

        @Test
        @DisplayName("delete() 호출 시 deletedAt이 설정되고 isDeleted가 true가 된다")
        void delete_setsDeletedAt() {
            // given
            TagModel tag = TagModel.create("SF");

            // when
            tag.delete();

            // then
            assertThat(tag.getDeletedAt()).isNotNull();
            assertThat(tag.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("restore() 호출 시 deletedAt이 null이 되고 isDeleted가 false가 된다")
        void restore_clearsDeletedAt() {
            // given
            TagModel tag = TagModel.create("SF");
            tag.delete();

            // when
            tag.restore();

            // then
            assertThat(tag.getDeletedAt()).isNull();
            assertThat(tag.isDeleted()).isFalse();
        }
    }
}
