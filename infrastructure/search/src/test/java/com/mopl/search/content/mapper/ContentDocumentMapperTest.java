package com.mopl.search.content.mapper;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.search.document.ContentDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ContentDocumentMapper 단위 테스트")
class ContentDocumentMapperTest {

    private final ContentDocumentMapper mapper = new ContentDocumentMapper();

    @Nested
    @DisplayName("toDocument()")
    class ToDocumentTest {

        @Test
        @DisplayName("ContentModel을 ContentDocument로 변환")
        void withContentModel_returnsContentDocument() {
            // given
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();

            ContentModel model = ContentModel.builder()
                .id(id)
                .type(ContentModel.ContentType.movie)
                .title("인셉션")
                .description("꿈속의 꿈")
                .thumbnailPath("contents/inception.png")
                .reviewCount(100)
                .averageRating(4.5)
                .popularityScore(85.0)
                .createdAt(now)
                .updatedAt(now)
                .build();

            // when
            ContentDocument result = mapper.toDocument(model);

            // then
            assertThat(result.getId()).isEqualTo(id.toString());
            assertThat(result.getContentId()).isEqualTo(id.toString());
            assertThat(result.getType()).isEqualTo("movie");
            assertThat(result.getTitle()).isEqualTo("인셉션");
            assertThat(result.getDescription()).isEqualTo("꿈속의 꿈");
            assertThat(result.getThumbnailPath()).isEqualTo("contents/inception.png");
            assertThat(result.getReviewCount()).isEqualTo(100);
            assertThat(result.getAverageRating()).isEqualTo(4.5);
            assertThat(result.getPopularityScore()).isEqualTo(85.0);
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("모든 ContentType 변환 확인")
        void withAllContentTypes_convertsCorrectly() {
            // given
            UUID id = UUID.randomUUID();

            for (ContentModel.ContentType type : ContentModel.ContentType.values()) {
                ContentModel model = ContentModel.builder()
                    .id(id)
                    .type(type)
                    .title("테스트")
                    .build();

                // when
                ContentDocument result = mapper.toDocument(model);

                // then
                assertThat(result.getType()).isEqualTo(type.name());
            }
        }
    }

    @Nested
    @DisplayName("toModel()")
    class ToModelTest {

        @Test
        @DisplayName("ContentDocument를 ContentModel로 변환")
        void withContentDocument_returnsContentModel() {
            // given
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();

            ContentDocument document = ContentDocument.builder()
                .id(id.toString())
                .contentId(id.toString())
                .type("movie")
                .title("인셉션")
                .description("꿈속의 꿈")
                .thumbnailPath("contents/inception.png")
                .reviewCount(100)
                .averageRating(4.5)
                .popularityScore(85.0)
                .createdAt(now)
                .updatedAt(now)
                .build();

            // when
            ContentModel result = mapper.toModel(document);

            // then
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getType()).isEqualTo(ContentModel.ContentType.movie);
            assertThat(result.getTitle()).isEqualTo("인셉션");
            assertThat(result.getDescription()).isEqualTo("꿈속의 꿈");
            assertThat(result.getThumbnailPath()).isEqualTo("contents/inception.png");
            assertThat(result.getReviewCount()).isEqualTo(100);
            assertThat(result.getAverageRating()).isEqualTo(4.5);
            assertThat(result.getPopularityScore()).isEqualTo(85.0);
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("null 필드 기본값 처리")
        void withNullFields_returnsDefaultValues() {
            // given
            UUID id = UUID.randomUUID();

            ContentDocument document = ContentDocument.builder()
                .id(id.toString())
                .type("movie")
                .title("테스트")
                .reviewCount(null)
                .averageRating(null)
                .popularityScore(null)
                .build();

            // when
            ContentModel result = mapper.toModel(document);

            // then
            assertThat(result.getReviewCount()).isZero();
            assertThat(result.getAverageRating()).isZero();
            assertThat(result.getPopularityScore()).isZero();
        }

        @Test
        @DisplayName("모든 ContentType 역변환 확인")
        void withAllContentTypes_convertsCorrectly() {
            // given
            UUID id = UUID.randomUUID();

            for (ContentModel.ContentType type : ContentModel.ContentType.values()) {
                ContentDocument document = ContentDocument.builder()
                    .id(id.toString())
                    .type(type.name())
                    .title("테스트")
                    .build();

                // when
                ContentModel result = mapper.toModel(document);

                // then
                assertThat(result.getType()).isEqualTo(type);
            }
        }
    }

    @Nested
    @DisplayName("데이터 정합성 테스트")
    class DataIntegrityTest {

        @Test
        @DisplayName("양방향 변환 시 데이터 유지 확인")
        void roundTrip_preservesData() {
            // given
            UUID id = UUID.randomUUID();
            Instant now = Instant.now();

            ContentModel originalModel = ContentModel.builder()
                .id(id)
                .type(ContentModel.ContentType.tvSeries)
                .title("브레이킹 배드")
                .description("화학 선생님의 이야기")
                .thumbnailPath("contents/breaking-bad.png")
                .reviewCount(500)
                .averageRating(4.9)
                .popularityScore(95.0)
                .createdAt(now)
                .updatedAt(now)
                .build();

            // when
            ContentDocument document = mapper.toDocument(originalModel);
            ContentModel resultModel = mapper.toModel(document);

            // then
            assertThat(resultModel.getId()).isEqualTo(originalModel.getId());
            assertThat(resultModel.getType()).isEqualTo(originalModel.getType());
            assertThat(resultModel.getTitle()).isEqualTo(originalModel.getTitle());
            assertThat(resultModel.getDescription()).isEqualTo(originalModel.getDescription());
            assertThat(resultModel.getThumbnailPath()).isEqualTo(originalModel.getThumbnailPath());
            assertThat(resultModel.getReviewCount()).isEqualTo(originalModel.getReviewCount());
            assertThat(resultModel.getAverageRating()).isEqualTo(originalModel.getAverageRating());
            assertThat(resultModel.getPopularityScore()).isEqualTo(originalModel.getPopularityScore());
            assertThat(resultModel.getCreatedAt()).isEqualTo(originalModel.getCreatedAt());
            assertThat(resultModel.getUpdatedAt()).isEqualTo(originalModel.getUpdatedAt());
        }
    }
}
