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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
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
    @DisplayName("save()")
    class SaveTest {

        @Test
        @DisplayName("새 태그 저장")
        void withNewTag_savesAndReturnsTag() {
            // given
            TagModel tagModel = TagModel.create("SF");

            // when
            TagModel savedTag = tagRepository.save(tagModel);

            // then
            assertThat(savedTag.getId()).isNotNull();
            assertThat(savedTag.getName()).isEqualTo("SF");
        }
    }

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
    @DisplayName("findByName()")
    class FindByNameTest {

        @Test
        @DisplayName("존재하는 이름으로 조회 시 TagModel 반환")
        void withExistingName_returnsOptionalTag() {
            // given
            String tagName = "액션";
            tagRepository.save(TagModel.create(tagName));

            // when
            Optional<TagModel> result = tagRepository.findByName(tagName);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo(tagName);
        }

        @Test
        @DisplayName("존재하지 않는 이름으로 조회 시 빈 Optional 반환")
        void withNonExistingName_returnsEmpty() {
            // when
            Optional<TagModel> result = tagRepository.findByName("NonExistent");

            // then
            assertThat(result).isEmpty();
        }
    }
}
