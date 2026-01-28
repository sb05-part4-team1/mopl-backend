package com.mopl.jpa.repository.tag;

import com.mopl.domain.model.tag.GenreModel;
import com.mopl.domain.repository.tag.GenreRepository;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.entity.tag.GenreEntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@Import({
    JpaConfig.class,
    GenreRepositoryImpl.class,
    GenreEntityMapper.class
})
@DisplayName("GenreRepositoryImpl 슬라이스 테스트")
class GenreRepositoryImplTest {

    @Autowired
    private GenreRepository genreRepository;

    private GenreModel savedGenre1;

    @BeforeEach
    void setUp() {
        savedGenre1 = genreRepository.save(GenreModel.create(28L, "액션"));
        genreRepository.save(GenreModel.create(35L, "코미디"));
        genreRepository.save(GenreModel.create(18L, "드라마"));
    }

    @Nested
    @DisplayName("save()")
    class SaveTest {

        @Test
        @DisplayName("새 장르를 저장한다")
        void savesNewGenre() {
            // given
            GenreModel newGenre = GenreModel.create(27L, "호러");

            // when
            GenreModel savedGenre = genreRepository.save(newGenre);

            // then
            assertThat(savedGenre.getId()).isNotNull();
            assertThat(savedGenre.getTmdbId()).isEqualTo(27L);
            assertThat(savedGenre.getName()).isEqualTo("호러");
        }

        @Test
        @DisplayName("기존 장르를 수정한다")
        void updatesExistingGenre() {
            // given
            GenreModel updatedGenre = GenreModel.builder()
                .id(savedGenre1.getId())
                .createdAt(savedGenre1.getCreatedAt())
                .tmdbId(28L)
                .name("액션/어드벤처")
                .build();

            // when
            GenreModel savedGenre = genreRepository.save(updatedGenre);

            // then
            assertThat(savedGenre.getId()).isEqualTo(savedGenre1.getId());
            assertThat(savedGenre.getName()).isEqualTo("액션/어드벤처");
            assertThat(genreRepository.count()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("findByTmdbId()")
    class FindByTmdbIdTest {

        @Test
        @DisplayName("존재하는 TMDB ID로 조회하면 장르를 반환한다")
        void withExistingTmdbId_returnsGenre() {
            // when
            Optional<GenreModel> found = genreRepository.findByTmdbId(28L);

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getTmdbId()).isEqualTo(28L);
            assertThat(found.get().getName()).isEqualTo("액션");
        }

        @Test
        @DisplayName("존재하지 않는 TMDB ID로 조회하면 빈 Optional을 반환한다")
        void withNonExistingTmdbId_returnsEmpty() {
            // when
            Optional<GenreModel> found = genreRepository.findByTmdbId(999L);

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllByTmdbIdIn()")
    class FindAllByTmdbIdInTest {

        @Test
        @DisplayName("TMDB ID 목록으로 장르를 조회한다")
        void withTmdbIds_returnsGenres() {
            // when
            List<GenreModel> genres = genreRepository.findAllByTmdbIdIn(List.of(28L, 35L));

            // then
            assertThat(genres).hasSize(2);
            assertThat(genres)
                .extracting(GenreModel::getTmdbId)
                .containsExactlyInAnyOrder(28L, 35L);
            assertThat(genres)
                .extracting(GenreModel::getName)
                .containsExactlyInAnyOrder("액션", "코미디");
        }

        @Test
        @DisplayName("빈 목록으로 조회하면 빈 리스트를 반환한다")
        void withEmptyList_returnsEmptyList() {
            // when
            List<GenreModel> genres = genreRepository.findAllByTmdbIdIn(List.of());

            // then
            assertThat(genres).isEmpty();
        }

        @Test
        @DisplayName("null 목록으로 조회하면 빈 리스트를 반환한다")
        void withNullList_returnsEmptyList() {
            // when
            List<GenreModel> genres = genreRepository.findAllByTmdbIdIn(null);

            // then
            assertThat(genres).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 TMDB ID로 조회하면 빈 리스트를 반환한다")
        void withNonExistingTmdbIds_returnsEmptyList() {
            // when
            List<GenreModel> genres = genreRepository.findAllByTmdbIdIn(List.of(999L, 888L));

            // then
            assertThat(genres).isEmpty();
        }

        @Test
        @DisplayName("일부만 존재하는 TMDB ID로 조회하면 존재하는 장르만 반환한다")
        void withPartiallyExistingTmdbIds_returnsExistingGenres() {
            // when
            List<GenreModel> genres = genreRepository.findAllByTmdbIdIn(
                List.of(28L, 999L)
            );

            // then
            assertThat(genres).hasSize(1);
            assertThat(genres.getFirst().getTmdbId()).isEqualTo(28L);
            assertThat(genres.getFirst().getName()).isEqualTo("액션");
        }
    }

    @Nested
    @DisplayName("count()")
    class CountTest {

        @Test
        @DisplayName("전체 장르 개수를 반환한다")
        void returnsTotalCount() {
            // when
            long count = genreRepository.count();

            // then
            assertThat(count).isEqualTo(3);
        }
    }
}
