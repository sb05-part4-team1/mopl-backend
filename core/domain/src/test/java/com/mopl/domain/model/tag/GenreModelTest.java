package com.mopl.domain.model.tag;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GenreModel 단위 테스트")
class GenreModelTest {

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("유효한 값으로 GenreModel 생성")
        void withValidValues_createsGenreModel() {
            // when
            GenreModel genre = GenreModel.create(28L, "Action");

            // then
            assertThat(genre.getTmdbId()).isEqualTo(28L);
            assertThat(genre.getName()).isEqualTo("Action");
        }

        @Test
        @DisplayName("이름의 앞뒤 공백이 제거된다")
        void withWhitespaceName_stripsWhitespace() {
            // when
            GenreModel genre = GenreModel.create(35L, "  Comedy  ");

            // then
            assertThat(genre.getName()).isEqualTo("Comedy");
        }
    }

    @Nested
    @DisplayName("toBuilder()")
    class ToBuilderTest {

        @Test
        @DisplayName("toBuilder로 복사 후 일부 필드 수정")
        void withToBuilder_createsModifiedCopy() {
            // given
            GenreModel original = GenreModel.create(28L, "Action");

            // when
            GenreModel modified = original.toBuilder()
                .name("Adventure")
                .build();

            // then
            assertThat(modified.getTmdbId()).isEqualTo(28L);
            assertThat(modified.getName()).isEqualTo("Adventure");
            assertThat(original.getName()).isEqualTo("Action");
        }
    }
}
