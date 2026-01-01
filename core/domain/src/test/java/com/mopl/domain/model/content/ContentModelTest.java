package com.mopl.domain.model.content;

import com.mopl.domain.exception.content.InvalidContentDataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.mopl.domain.model.content.ContentModel.THUMBNAIL_URL_MAX_LENGTH;
import static com.mopl.domain.model.content.ContentModel.TITLE_MAX_LENGTH;
import static com.mopl.domain.model.content.ContentModel.TYPE_MAX_LENGTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ContentModel 단위 테스트")
class ContentModelTest {

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("유효한 데이터로 ContentModel 생성")
        void withValidData_createsContentModel() {
            // when
            ContentModel content = ContentModel.create(
                "영화",
                "인셉션",
                "꿈속의 꿈",
                "https://mopl.com/inception.png"
            );

            // then
            assertThat(content.getId()).isNull();
            assertThat(content.getType()).isEqualTo("영화");
            assertThat(content.getTitle()).isEqualTo("인셉션");
            assertThat(content.getDescription()).isEqualTo("꿈속의 꿈");
            assertThat(content.getThumbnailUrl()).isEqualTo("https://mopl.com/inception.png");
            assertThat(content.getTags()).isEmpty();
            assertThat(content.getCreatedAt()).isNull();
            assertThat(content.getUpdatedAt()).isNull();
            assertThat(content.getDeletedAt()).isNull();
        }

        static Stream<Arguments> emptyFieldsProvider() {
            return Stream.of(
                Arguments.of("null", null),
                Arguments.of("빈 문자열", ""),
                Arguments.of("공백만", "   ")
            );
        }

        @ParameterizedTest(name = "타입이 {0}인 경우")
        @MethodSource("emptyFieldsProvider")
        @DisplayName("컨텐츠 타입이 비어있으면 예외 발생")
        void withEmptyType_throwsException(String description, String type) {
            assertThatThrownBy(() -> ContentModel.create(
                type,
                "제목",
                "설명",
                "url")
            )
                .isInstanceOf(InvalidContentDataException.class)
                .satisfies(e -> {
                    InvalidContentDataException ex = (InvalidContentDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("컨텐츠 타입은 비어있을 수 없습니다.");
                });
        }

        @ParameterizedTest(name = "제목이 {0}인 경우")
        @MethodSource("emptyFieldsProvider")
        @DisplayName("제목이 비어있으면 예외 발생")
        void withEmptyTitle_throwsException(String description, String title) {
            assertThatThrownBy(() -> ContentModel.create(
                "TYPE",
                title,
                "설명",
                "url")
            )
                .isInstanceOf(InvalidContentDataException.class)
                .satisfies(e -> {
                    InvalidContentDataException ex = (InvalidContentDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("제목은 비어있을 수 없습니다.");
                });
        }

        @Test
        @DisplayName("타입이 제한 길이를 초과하면 예외 발생")
        void withTypeExceedingMaxLength_throwsException() {
            String longType = "a".repeat(TYPE_MAX_LENGTH + 1);

            assertThatThrownBy(() -> ContentModel.create(
                longType,
                "제목",
                "설명",
                "url")
            )
                .isInstanceOf(InvalidContentDataException.class)
                .satisfies(e -> {
                    InvalidContentDataException ex = (InvalidContentDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("타입은 " + TYPE_MAX_LENGTH + "자를 초과할 수 없습니다.");
                });
        }

        @Test
        @DisplayName("제목이 제한 길이를 초과하면 예외 발생")
        void withTitleExceedingMaxLength_throwsException() {
            String longTitle = "a".repeat(TITLE_MAX_LENGTH + 1);

            assertThatThrownBy(() -> ContentModel.create(
                "TYPE",
                longTitle,
                "설명",
                "url")
            )
                .isInstanceOf(InvalidContentDataException.class)
                .satisfies(e -> {
                    InvalidContentDataException ex = (InvalidContentDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("제목은 " + TITLE_MAX_LENGTH + "자를 초과할 수 없습니다.");
                });
        }

        @Test
        @DisplayName("썸네일 URL이 제한 길이를 초과하면 예외 발생")
        void withThumbnailUrlExceedingMaxLength_throwsException() {
            String longUrl = "a".repeat(THUMBNAIL_URL_MAX_LENGTH + 1);

            assertThatThrownBy(() -> ContentModel.create(
                "TYPE",
                "제목",
                "설명",
                longUrl)
            )
                .isInstanceOf(InvalidContentDataException.class)
                .satisfies(e -> {
                    InvalidContentDataException ex = (InvalidContentDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("썸네일 URL은 " + THUMBNAIL_URL_MAX_LENGTH + "자를 초과할 수 없습니다.");
                });
        }
    }

    @Nested
    @DisplayName("SuperBuilder")
    class BuilderTest {

        @Test
        @DisplayName("빌더를 통해 태그 목록을 포함하여 생성")
        void withBuilder_includesTags() {
            ContentModel content = ContentModel.builder()
                .type("영화")
                .title("인셉션")
                .description("꿈속의 꿈")
                .thumbnailUrl("https://mopl.com/inception.png")
                .tags(java.util.List.of("SF", "액션"))
                .build();

            assertThat(content.getId()).isNull();
            assertThat(content.getType()).isEqualTo("영화");
            assertThat(content.getTitle()).isEqualTo("인셉션");
            assertThat(content.getDescription()).isEqualTo("꿈속의 꿈");
            assertThat(content.getThumbnailUrl()).isEqualTo("https://mopl.com/inception.png");
            assertThat(content.getTags()).containsExactly("SF", "액션");
            assertThat(content.getCreatedAt()).isNull();
            assertThat(content.getUpdatedAt()).isNull();
            assertThat(content.getDeletedAt()).isNull();
        }
    }
}
