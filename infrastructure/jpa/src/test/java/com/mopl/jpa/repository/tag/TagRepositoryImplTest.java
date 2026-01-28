package com.mopl.jpa.repository.tag;

import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.repository.tag.TagRepository;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.entity.tag.TagEntityMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
@Import({
    JpaConfig.class,
    TagRepositoryImpl.class,
    TagEntityMapper.class
})
@DisplayName("TagRepositoryImpl 슬라이스 테스트")
class TagRepositoryImplTest {

    @Autowired
    private TagRepository tagRepository;

    @Nested
    @DisplayName("saveAll()")
    class SaveAllTest {

        @Test
        @DisplayName("여러 태그를 한 번에 저장한다")
        void saveAll_savesMultipleTags() {
            // given
            List<TagModel> tags = List.of(
                TagModel.create("SF"),
                TagModel.create("액션")
            );

            // when
            List<TagModel> savedTags = tagRepository.saveAll(tags);

            // then
            assertThat(savedTags).hasSize(2);
            assertThat(savedTags)
                .extracting(TagModel::getId)
                .allMatch(Objects::nonNull);
            assertThat(savedTags)
                .extracting(TagModel::getName)
                .containsExactlyInAnyOrder("SF", "액션");
        }
    }

    @Nested
    @DisplayName("findByNameIn()")
    class FindByNameInTest {

        @Test
        @DisplayName("이름 목록으로 태그를 조회한다")
        void withTagNames_returnsTags() {
            // given
            tagRepository.saveAll(List.of(
                TagModel.create("로맨스"),
                TagModel.create("코미디"),
                TagModel.create("스릴러")
            ));

            // when
            List<TagModel> foundTags = tagRepository.findByNameIn(List.of("로맨스", "스릴러"));

            // then
            assertThat(foundTags).hasSize(2);
            assertThat(foundTags)
                .extracting(TagModel::getName)
                .containsExactlyInAnyOrder("로맨스", "스릴러");
        }

        @Test
        @DisplayName("빈 목록으로 조회하면 빈 리스트를 반환한다")
        void withEmptyList_returnsEmptyList() {
            // when
            List<TagModel> foundTags = tagRepository.findByNameIn(List.of());

            // then
            assertThat(foundTags).isEmpty();
        }

        @Test
        @DisplayName("null 목록으로 조회하면 빈 리스트를 반환한다")
        void withNullList_returnsEmptyList() {
            // when
            List<TagModel> foundTags = tagRepository.findByNameIn(null);

            // then
            assertThat(foundTags).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 태그 이름으로 조회하면 빈 리스트를 반환한다")
        void withNonExistingNames_returnsEmptyList() {
            // when
            List<TagModel> foundTags = tagRepository.findByNameIn(List.of("존재하지않는태그"));

            // then
            assertThat(foundTags).isEmpty();
        }

        @Test
        @DisplayName("일부만 존재하는 태그 이름으로 조회하면 존재하는 태그만 반환한다")
        void withPartiallyExistingNames_returnsExistingTags() {
            // given
            tagRepository.saveAll(List.of(
                TagModel.create("판타지")
            ));

            // when
            List<TagModel> foundTags = tagRepository.findByNameIn(
                List.of("판타지", "존재하지않는태그")
            );

            // then
            assertThat(foundTags).hasSize(1);
            assertThat(foundTags.getFirst().getName()).isEqualTo("판타지");
        }
    }
}
