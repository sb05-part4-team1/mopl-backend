package com.mopl.domain.model.content;

import com.mopl.domain.exception.content.InvalidContentDataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static com.mopl.domain.model.content.ContentModel.DESCRIPTION_MAX_LENGTH;
import static com.mopl.domain.model.content.ContentModel.THUMBNAIL_PATH_MAX_LENGTH;
import static com.mopl.domain.model.content.ContentModel.TITLE_MAX_LENGTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

@DisplayName("ContentModel 단위 테스트")
class ContentModelTest {

    private static final String DEFAULT_TITLE = "인셉션";
    private static final String DEFAULT_DESCRIPTION = "꿈속의 꿈";
    private static final String DEFAULT_THUMBNAIL_PATH = "contents/inception.png";

    static Stream<Arguments> blankStringProvider() {
        return Stream.of(
            Arguments.of("null", null),
            Arguments.of("빈 문자열", ""),
            Arguments.of("공백만", "   ")
        );
    }

    private static ContentModel createDefaultContent() {
        return ContentModel.create(
            ContentModel.ContentType.movie,
            DEFAULT_TITLE,
            DEFAULT_DESCRIPTION,
            DEFAULT_THUMBNAIL_PATH
        );
    }

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("유효한 데이터로 ContentModel 생성")
        void withValidData_createsContentModel() {
            // when
            ContentModel content = ContentModel.create(
                ContentModel.ContentType.movie,
                "인셉션",
                "꿈속의 꿈",
                "contents/inception.png"
            );

            // then
            assertThat(content.getType()).isEqualTo(ContentModel.ContentType.movie);
            assertThat(content.getTitle()).isEqualTo("인셉션");
            assertThat(content.getDescription()).isEqualTo("꿈속의 꿈");
            assertThat(content.getThumbnailPath()).isEqualTo("contents/inception.png");
            assertThat(content.getReviewCount()).isZero();
            assertThat(content.getAverageRating()).isZero();
        }

        @Test
        @DisplayName("타입이 null이면 예외 발생")
        @SuppressWarnings("DataFlowIssue")
        void withNullType_throwsException() {
            assertThatThrownBy(() -> ContentModel.create(
                null, DEFAULT_TITLE, DEFAULT_DESCRIPTION, DEFAULT_THUMBNAIL_PATH))
                .isInstanceOf(InvalidContentDataException.class);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.mopl.domain.model.content.ContentModelTest#blankStringProvider")
        @DisplayName("제목이 비어있으면 예외 발생")
        void withBlankTitle_throwsException(String description, String title) {
            assertThatThrownBy(() -> ContentModel.create(
                ContentModel.ContentType.movie, title, DEFAULT_DESCRIPTION, DEFAULT_THUMBNAIL_PATH))
                .isInstanceOf(InvalidContentDataException.class);
        }

        @Test
        @DisplayName("제목이 정확히 최대 길이면 생성 성공")
        void withTitleAtMaxLength_createsContentModel() {
            String maxTitle = "가".repeat(TITLE_MAX_LENGTH);

            ContentModel content = ContentModel.create(
                ContentModel.ContentType.movie, maxTitle, DEFAULT_DESCRIPTION, DEFAULT_THUMBNAIL_PATH);

            assertThat(content.getTitle()).isEqualTo(maxTitle);
        }

        @Test
        @DisplayName("제목이 최대 길이 초과하면 예외 발생")
        void withTitleExceedingMaxLength_throwsException() {
            String longTitle = "가".repeat(TITLE_MAX_LENGTH + 1);

            assertThatThrownBy(() -> ContentModel.create(
                ContentModel.ContentType.movie, longTitle, DEFAULT_DESCRIPTION, DEFAULT_THUMBNAIL_PATH))
                .isInstanceOf(InvalidContentDataException.class);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.mopl.domain.model.content.ContentModelTest#blankStringProvider")
        @DisplayName("설명이 비어있으면 예외 발생")
        void withBlankDescription_throwsException(String desc, String description) {
            assertThatThrownBy(() -> ContentModel.create(
                ContentModel.ContentType.movie, DEFAULT_TITLE, description, DEFAULT_THUMBNAIL_PATH))
                .isInstanceOf(InvalidContentDataException.class);
        }

        @Test
        @DisplayName("설명이 정확히 최대 길이면 생성 성공")
        void withDescriptionAtMaxLength_createsContentModel() {
            String maxDescription = "가".repeat(DESCRIPTION_MAX_LENGTH);

            ContentModel content = ContentModel.create(
                ContentModel.ContentType.movie, DEFAULT_TITLE, maxDescription, DEFAULT_THUMBNAIL_PATH);

            assertThat(content.getDescription()).isEqualTo(maxDescription);
        }

        @Test
        @DisplayName("설명이 최대 길이 초과하면 예외 발생")
        void withDescriptionExceedingMaxLength_throwsException() {
            String longDescription = "가".repeat(DESCRIPTION_MAX_LENGTH + 1);

            assertThatThrownBy(() -> ContentModel.create(
                ContentModel.ContentType.movie, DEFAULT_TITLE, longDescription, DEFAULT_THUMBNAIL_PATH))
                .isInstanceOf(InvalidContentDataException.class);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.mopl.domain.model.content.ContentModelTest#blankStringProvider")
        @DisplayName("썸네일 경로가 비어있으면 예외 발생")
        void withBlankThumbnailPath_throwsException(String desc, String thumbnailPath) {
            assertThatThrownBy(() -> ContentModel.create(
                ContentModel.ContentType.movie, DEFAULT_TITLE, DEFAULT_DESCRIPTION, thumbnailPath))
                .isInstanceOf(InvalidContentDataException.class);
        }

        @Test
        @DisplayName("썸네일 경로가 정확히 최대 길이면 생성 성공")
        void withThumbnailPathAtMaxLength_createsContentModel() {
            String maxPath = "a".repeat(THUMBNAIL_PATH_MAX_LENGTH);

            ContentModel content = ContentModel.create(
                ContentModel.ContentType.movie, DEFAULT_TITLE, DEFAULT_DESCRIPTION, maxPath);

            assertThat(content.getThumbnailPath()).isEqualTo(maxPath);
        }

        @Test
        @DisplayName("썸네일 경로가 최대 길이 초과하면 예외 발생")
        void withThumbnailPathExceedingMaxLength_throwsException() {
            String longPath = "a".repeat(THUMBNAIL_PATH_MAX_LENGTH + 1);

            assertThatThrownBy(() -> ContentModel.create(
                ContentModel.ContentType.movie, DEFAULT_TITLE, DEFAULT_DESCRIPTION, longPath))
                .isInstanceOf(InvalidContentDataException.class);
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTest {

        @Test
        @DisplayName("유효한 데이터로 변경하면 새 객체 반환")
        void withValidData_returnsNewInstance() {
            // given
            ContentModel content = createDefaultContent();

            // when
            ContentModel result = content.update("새 제목", "새 설명", "new-path");

            // then
            assertThat(result).isNotSameAs(content);
            assertThat(result.getType()).isEqualTo(ContentModel.ContentType.movie);
            assertThat(result.getTitle()).isEqualTo("새 제목");
            assertThat(result.getDescription()).isEqualTo("새 설명");
            assertThat(result.getThumbnailPath()).isEqualTo("new-path");
        }

        @Test
        @DisplayName("제목만 수정하면 제목만 변경")
        void withOnlyTitle_updatesTitleOnly() {
            // given
            ContentModel content = createDefaultContent();

            // when
            ContentModel result = content.update("새 제목", null, null);

            // then
            assertThat(result).isNotSameAs(content);
            assertThat(result.getTitle()).isEqualTo("새 제목");
            assertThat(result.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
            assertThat(result.getThumbnailPath()).isEqualTo(DEFAULT_THUMBNAIL_PATH);
        }

        @Test
        @DisplayName("설명만 수정하면 설명만 변경")
        void withOnlyDescription_updatesDescriptionOnly() {
            // given
            ContentModel content = createDefaultContent();

            // when
            ContentModel result = content.update(null, "새 설명", null);

            // then
            assertThat(result).isNotSameAs(content);
            assertThat(result.getTitle()).isEqualTo(DEFAULT_TITLE);
            assertThat(result.getDescription()).isEqualTo("새 설명");
            assertThat(result.getThumbnailPath()).isEqualTo(DEFAULT_THUMBNAIL_PATH);
        }

        @Test
        @DisplayName("썸네일 경로만 수정하면 썸네일 경로만 변경")
        void withOnlyThumbnailPath_updatesThumbnailPathOnly() {
            // given
            ContentModel content = createDefaultContent();

            // when
            ContentModel result = content.update(null, null, "new-path");

            // then
            assertThat(result).isNotSameAs(content);
            assertThat(result.getTitle()).isEqualTo(DEFAULT_TITLE);
            assertThat(result.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
            assertThat(result.getThumbnailPath()).isEqualTo("new-path");
        }

        @Test
        @DisplayName("모두 null이면 값 유지하고 새 객체 반환")
        void withAllNull_returnsNewInstance() {
            // given
            ContentModel content = createDefaultContent();

            // when
            ContentModel result = content.update(null, null, null);

            // then
            assertThat(result).isNotSameAs(content);
            assertThat(result.getTitle()).isEqualTo(DEFAULT_TITLE);
            assertThat(result.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
            assertThat(result.getThumbnailPath()).isEqualTo(DEFAULT_THUMBNAIL_PATH);
        }

        @ParameterizedTest
        @DisplayName("제목이 비어있으면 예외 발생")
        @ValueSource(strings = {"", "   "})
        void withBlankTitle_throwsException(String blankTitle) {
            // given
            ContentModel content = createDefaultContent();

            // when & then
            assertThatThrownBy(() -> content.update(blankTitle, null, null))
                .isInstanceOf(InvalidContentDataException.class);
        }

        @Test
        @DisplayName("제목이 정확히 최대 길이면 수정 성공")
        void withTitleAtMaxLength_updatesTitle() {
            // given
            ContentModel content = createDefaultContent();
            String maxTitle = "가".repeat(TITLE_MAX_LENGTH);

            // when
            ContentModel result = content.update(maxTitle, null, null);

            // then
            assertThat(result.getTitle()).isEqualTo(maxTitle);
        }

        @Test
        @DisplayName("제목이 최대 길이 초과하면 예외 발생")
        void withTitleExceedingMaxLength_throwsException() {
            // given
            ContentModel content = createDefaultContent();
            String longTitle = "가".repeat(TITLE_MAX_LENGTH + 1);

            // when & then
            assertThatThrownBy(() -> content.update(longTitle, null, null))
                .isInstanceOf(InvalidContentDataException.class);
        }

        @ParameterizedTest
        @DisplayName("설명이 비어있으면 예외 발생")
        @ValueSource(strings = {"", "   "})
        void withBlankDescription_throwsException(String blankDescription) {
            // given
            ContentModel content = createDefaultContent();

            // when & then
            assertThatThrownBy(() -> content.update(null, blankDescription, null))
                .isInstanceOf(InvalidContentDataException.class);
        }

        @Test
        @DisplayName("설명이 정확히 최대 길이면 수정 성공")
        void withDescriptionAtMaxLength_updatesDescription() {
            // given
            ContentModel content = createDefaultContent();
            String maxDescription = "가".repeat(DESCRIPTION_MAX_LENGTH);

            // when
            ContentModel result = content.update(null, maxDescription, null);

            // then
            assertThat(result.getDescription()).isEqualTo(maxDescription);
        }

        @Test
        @DisplayName("설명이 최대 길이 초과하면 예외 발생")
        void withDescriptionExceedingMaxLength_throwsException() {
            // given
            ContentModel content = createDefaultContent();
            String longDescription = "가".repeat(DESCRIPTION_MAX_LENGTH + 1);

            // when & then
            assertThatThrownBy(() -> content.update(null, longDescription, null))
                .isInstanceOf(InvalidContentDataException.class);
        }

        @ParameterizedTest
        @DisplayName("썸네일 경로가 비어있으면 예외 발생")
        @ValueSource(strings = {"", "   "})
        void withBlankThumbnailPath_throwsException(String blankPath) {
            // given
            ContentModel content = createDefaultContent();

            // when & then
            assertThatThrownBy(() -> content.update(null, null, blankPath))
                .isInstanceOf(InvalidContentDataException.class);
        }

        @Test
        @DisplayName("썸네일 경로가 정확히 최대 길이면 수정 성공")
        void withThumbnailPathAtMaxLength_updatesThumbnailPath() {
            // given
            ContentModel content = createDefaultContent();
            String maxPath = "a".repeat(THUMBNAIL_PATH_MAX_LENGTH);

            // when
            ContentModel result = content.update(null, null, maxPath);

            // then
            assertThat(result.getThumbnailPath()).isEqualTo(maxPath);
        }

        @Test
        @DisplayName("썸네일 경로가 최대 길이 초과하면 예외 발생")
        void withThumbnailPathExceedingMaxLength_throwsException() {
            // given
            ContentModel content = createDefaultContent();
            String longPath = "a".repeat(THUMBNAIL_PATH_MAX_LENGTH + 1);

            // when & then
            assertThatThrownBy(() -> content.update(null, null, longPath))
                .isInstanceOf(InvalidContentDataException.class);
        }
    }

    @Nested
    @DisplayName("addReview()")
    class AddReviewTest {

        @Test
        @DisplayName("첫 번째 리뷰 추가 시 평균 평점이 해당 평점이 된다")
        void addFirstReview_setsAverageRating() {
            // given
            ContentModel content = createDefaultContent();

            // when
            ContentModel result = content.addReview(4.5);

            // then
            assertThat(result).isNotSameAs(content);
            assertThat(result.getReviewCount()).isEqualTo(1);
            assertThat(result.getAverageRating()).isEqualTo(4.5);
        }

        @Test
        @DisplayName("여러 리뷰 추가 시 평균 평점이 올바르게 계산된다")
        void addMultipleReviews_calculatesAverageCorrectly() {
            // given
            ContentModel content = createDefaultContent();

            // when
            ContentModel result = content
                .addReview(5.0)
                .addReview(3.0)
                .addReview(4.0);

            // then
            assertThat(result.getReviewCount()).isEqualTo(3);
            assertThat(result.getAverageRating()).isCloseTo(4.0, within(0.001));
        }
    }

    @Nested
    @DisplayName("updateReview()")
    class UpdateReviewTest {

        @Test
        @DisplayName("리뷰 평점 수정 시 평균 평점이 올바르게 재계산된다")
        void updateReview_recalculatesAverage() {
            // given
            ContentModel content = createDefaultContent()
                .addReview(5.0)
                .addReview(3.0);  // 평균: 4.0

            // when
            ContentModel result = content.updateReview(3.0, 5.0);  // 3.0 -> 5.0

            // then
            assertThat(result).isNotSameAs(content);
            assertThat(result.getReviewCount()).isEqualTo(2);
            assertThat(result.getAverageRating()).isCloseTo(5.0, within(0.001));
        }

        @Test
        @DisplayName("리뷰가 없을 때 수정하면 예외 발생")
        void updateReview_withNoReviews_throwsException() {
            // given
            ContentModel content = createDefaultContent();

            // when & then
            assertThatThrownBy(() -> content.updateReview(3.0, 5.0))
                .isInstanceOf(InvalidContentDataException.class);
        }
    }

    @Nested
    @DisplayName("removeReview()")
    class RemoveReviewTest {

        @Test
        @DisplayName("리뷰 삭제 시 평균 평점이 올바르게 재계산된다")
        void removeReview_recalculatesAverage() {
            // given
            ContentModel content = createDefaultContent()
                .addReview(5.0)
                .addReview(3.0)
                .addReview(4.0);  // 평균: 4.0

            // when
            ContentModel result = content.removeReview(3.0);

            // then
            assertThat(result).isNotSameAs(content);
            assertThat(result.getReviewCount()).isEqualTo(2);
            assertThat(result.getAverageRating()).isCloseTo(4.5, within(0.001));
        }

        @Test
        @DisplayName("마지막 리뷰 삭제 시 평균 평점이 0이 된다")
        void removeLastReview_resetsAverageToZero() {
            // given
            ContentModel content = createDefaultContent().addReview(5.0);

            // when
            ContentModel result = content.removeReview(5.0);

            // then
            assertThat(result).isNotSameAs(content);
            assertThat(result.getReviewCount()).isZero();
            assertThat(result.getAverageRating()).isZero();
        }

        @Test
        @DisplayName("리뷰가 없을 때 삭제하면 예외 발생")
        void removeReview_withNoReviews_throwsException() {
            // given
            ContentModel content = createDefaultContent();

            // when & then
            assertThatThrownBy(() -> content.removeReview(5.0))
                .isInstanceOf(InvalidContentDataException.class);
        }
    }
}
