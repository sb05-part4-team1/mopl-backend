package com.mopl.domain.model.content;

import com.mopl.domain.exception.content.InvalidContentDataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static com.mopl.domain.model.content.ContentModel.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ContentModel 단위 테스트")
class ContentModelTest {

    static Stream<Arguments> emptyFieldsProvider() {
        return Stream.of(
            Arguments.of("null", null),
            Arguments.of("빈 문자열", ""),
            Arguments.of("공백만", "   ")
        );
    }

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("유효한 데이터로 ContentModel 생성")
        void create_withValidData() {
            // given
            String type = "영화";
            String title = "인셉션";
            String description = "꿈속의 꿈";
            String thumbnailUrl = "https://mopl.com/inception.png";

            // when
            ContentModel content = ContentModel.create(type, title, description, thumbnailUrl);

            // then
            assertThat(content.getType()).isEqualTo(type);
            assertThat(content.getTitle()).isEqualTo(title);
            assertThat(content.getDescription()).isEqualTo(description);
            assertThat(content.getThumbnailUrl()).isEqualTo(thumbnailUrl);
            assertThat(content.getTags()).isEmpty();
        }

        @ParameterizedTest
        @MethodSource("com.mopl.domain.model.content.ContentModelTest#emptyFieldsProvider")
        @DisplayName("타입이 비어있으면 예외 발생")
        void create_withEmptyType(String desc, String type) {
            // given / when / then
            assertThatThrownBy(() -> ContentModel.create(type, "제목", "설명", "url")
            ).isInstanceOf(InvalidContentDataException.class);
        }

        @ParameterizedTest
        @MethodSource("com.mopl.domain.model.content.ContentModelTest#emptyFieldsProvider")
        @DisplayName("제목이 비어있으면 예외 발생")
        void create_withEmptyTitle(String desc, String title) {
            // given / when / then
            assertThatThrownBy(() -> ContentModel.create("TYPE", title, "설명", "url")
            ).isInstanceOf(InvalidContentDataException.class);
        }

        @ParameterizedTest
        @MethodSource("com.mopl.domain.model.content.ContentModelTest#emptyFieldsProvider")
        @DisplayName("설명이 비어있으면 예외 발생")
        void create_withEmptyDescription(String desc, String description) {
            // given / when / then
            assertThatThrownBy(() -> ContentModel.create("TYPE", "제목", description, "url")
            ).isInstanceOf(InvalidContentDataException.class);
        }

        @Test
        @DisplayName("타입 길이 초과 시 예외 발생")
        void create_withTypeTooLong() {
            // given
            String longType = "a".repeat(TYPE_MAX_LENGTH + 1);

            // when / then
            assertThatThrownBy(() -> ContentModel.create(longType, "제목", "설명", "url")
            ).isInstanceOf(InvalidContentDataException.class);
        }

        @Test
        @DisplayName("제목 길이 초과 시 예외 발생")
        void create_withTitleTooLong() {
            // given
            String longTitle = "a".repeat(TITLE_MAX_LENGTH + 1);

            // when / then
            assertThatThrownBy(() -> ContentModel.create("TYPE", longTitle, "설명", "url")
            ).isInstanceOf(InvalidContentDataException.class);
        }

        @Test
        @DisplayName("썸네일 URL 길이 초과 시 예외 발생")
        void create_withThumbnailTooLong() {
            // given
            String longUrl = "a".repeat(THUMBNAIL_URL_MAX_LENGTH + 1);

            // when / then
            assertThatThrownBy(() -> ContentModel.create("TYPE", "제목", "설명", longUrl)
            ).isInstanceOf(InvalidContentDataException.class);
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTest {

        @Test
        @DisplayName("유효한 데이터로 update 시 새 객체 반환")
        void update_withValidData() {
            // given
            ContentModel original = ContentModel.create(
                "영화", "기존 제목", "기존 설명", "old-url"
            );

            // when
            ContentModel updated = original.update(
                "새 제목", "새 설명", "new-url"
            );

            // then
            assertThat(updated).isNotSameAs(original);
            assertThat(updated.getType()).isEqualTo("영화");
            assertThat(updated.getTitle()).isEqualTo("새 제목");
            assertThat(updated.getDescription()).isEqualTo("새 설명");
            assertThat(updated.getThumbnailUrl()).isEqualTo("new-url");
        }

        @ParameterizedTest
        @MethodSource("com.mopl.domain.model.content.ContentModelTest#emptyFieldsProvider")
        @DisplayName("update 시 제목이 비어있으면 예외 발생")
        void update_withEmptyTitle(String desc, String title) {
            // given
            ContentModel original = ContentModel.create(
                "영화", "제목", "설명", "url"
            );

            // when / then
            assertThatThrownBy(() -> original.update(title, "설명", "url")
            ).isInstanceOf(InvalidContentDataException.class);
        }

        @ParameterizedTest
        @MethodSource("com.mopl.domain.model.content.ContentModelTest#emptyFieldsProvider")
        @DisplayName("update 시 설명이 비어있으면 예외 발생")
        void update_withEmptyDescription(String desc, String description) {
            // given
            ContentModel original = ContentModel.create(
                "영화", "제목", "설명", "url"
            );

            // when / then
            assertThatThrownBy(() -> original.update("제목", description, "url")
            ).isInstanceOf(InvalidContentDataException.class);
        }

        @Test
        @DisplayName("update 시 썸네일 URL 길이 초과하면 예외 발생")
        void update_withThumbnailTooLong() {
            // given
            ContentModel original = ContentModel.create(
                "영화", "제목", "설명", "url"
            );
            String longUrl = "a".repeat(THUMBNAIL_URL_MAX_LENGTH + 1);

            // when / then
            assertThatThrownBy(() -> original.update("제목", "설명", longUrl)
            ).isInstanceOf(InvalidContentDataException.class);
        }
    }

    @Nested
    @DisplayName("withTags()")
    class WithTagsTest {

        @Test
        @DisplayName("withTags는 새 객체를 반환한다")
        void withTags_returnsNewInstance() {
            // given
            ContentModel original = ContentModel.create(
                "영화", "제목", "설명", "url"
            );

            // when
            ContentModel updated = original.withTags(List.of("SF", "액션"));

            // then
            assertThat(original.getTags()).isEmpty();
            assertThat(updated.getTags()).containsExactly("SF", "액션");
            assertThat(updated).isNotSameAs(original);
        }
    }

    @Nested
    @DisplayName("SuperBuilder")
    class BuilderTest {

        @Test
        @DisplayName("빌더로 태그 포함 생성 가능")
        void builder_withTags() {
            // when
            ContentModel content = ContentModel.builder()
                .type("영화")
                .title("인셉션")
                .description("꿈속의 꿈")
                .thumbnailUrl("url")
                .tags(List.of("SF", "액션"))
                .build();

            // then
            assertThat(content.getTags()).containsExactly("SF", "액션");
        }

        @Test
        @DisplayName("빌더는 유효성 검증을 우회한다")
        void builder_allowsInvalidState() {
            // when
            ContentModel content = ContentModel.builder()
                .type("")
                .title("제목")
                .thumbnailUrl("url")
                .build();

            // then
            assertThat(content.getType()).isEmpty();
        }
    }
}
