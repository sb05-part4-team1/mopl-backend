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
}
