package com.mopl.jpa.entity.tag;

import com.mopl.domain.model.tag.GenreModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GenreEntityMapper 단위 테스트")
class GenreEntityMapperTest {

    private final GenreEntityMapper mapper = new GenreEntityMapper();

    @Nested
    @DisplayName("toModel()")
    class ToModelTest {

        @Test
        @DisplayName("GenreEntity를 GenreModel로 변환")
        void withGenreEntity_returnsGenreModel() {
            // given
            UUID id = UUID.randomUUID();
            Instant createdAt = Instant.now();
            GenreEntity entity = GenreEntity.builder()
                .id(id)
                .tmdbId(28L)
                .name("액션")
                .createdAt(createdAt)
                .deletedAt(null)
                .build();

            // when
            GenreModel result = mapper.toModel(entity);

            // then
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getTmdbId()).isEqualTo(28L);
            assertThat(result.getName()).isEqualTo("액션");
            assertThat(result.getCreatedAt()).isEqualTo(createdAt);
            assertThat(result.getDeletedAt()).isNull();
            assertThat(result.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("null 입력 시 null 반환")
        void withNull_returnsNull() {
            // when
            GenreModel result = mapper.toModel(null);

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("toEntity()")
    class ToEntityTest {

        @Test
        @DisplayName("GenreModel을 GenreEntity로 변환")
        void withGenreModel_returnsGenreEntity() {
            // given
            UUID id = UUID.randomUUID();
            Instant createdAt = Instant.now();
            GenreModel model = GenreModel.builder()
                .id(id)
                .tmdbId(35L)
                .name("코미디")
                .createdAt(createdAt)
                .deletedAt(null)
                .build();

            // when
            GenreEntity result = mapper.toEntity(model);

            // then
            assertThat(result.getId()).isEqualTo(id);
            assertThat(result.getTmdbId()).isEqualTo(35L);
            assertThat(result.getName()).isEqualTo("코미디");
            assertThat(result.getCreatedAt()).isEqualTo(createdAt);
            assertThat(result.getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("null 입력 시 null 반환")
        void withNull_returnsNull() {
            // when
            GenreEntity result = mapper.toEntity(null);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("양방향 변환 시 데이터 유지")
        void roundTrip_preservesData() {
            // given
            UUID id = UUID.randomUUID();
            Instant createdAt = Instant.now();
            GenreModel originalModel = GenreModel.builder()
                .id(id)
                .tmdbId(18L)
                .name("드라마")
                .createdAt(createdAt)
                .deletedAt(null)
                .build();

            // when
            GenreEntity entity = mapper.toEntity(originalModel);
            GenreModel resultModel = mapper.toModel(entity);

            // then
            assertThat(resultModel.getId()).isEqualTo(originalModel.getId());
            assertThat(resultModel.getTmdbId()).isEqualTo(originalModel.getTmdbId());
            assertThat(resultModel.getName()).isEqualTo(originalModel.getName());
            assertThat(resultModel.getCreatedAt()).isEqualTo(originalModel.getCreatedAt());
            assertThat(resultModel.getDeletedAt()).isNull();
        }
    }
}
