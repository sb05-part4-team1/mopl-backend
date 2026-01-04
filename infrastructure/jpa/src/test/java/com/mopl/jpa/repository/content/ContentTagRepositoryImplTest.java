package com.mopl.jpa.repository.content;

import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.repository.content.ContentTagRepository;
import com.mopl.domain.repository.tag.TagRepository;
import com.mopl.jpa.config.JpaConfig;
import com.mopl.jpa.entity.content.ContentEntityMapper;
import com.mopl.jpa.entity.content.ContentTagEntity;
import com.mopl.jpa.entity.tag.TagEntityMapper;
import com.mopl.jpa.repository.tag.TagRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@Import({
    JpaConfig.class,
    ContentTagRepositoryImpl.class,
    ContentRepositoryImpl.class,
    TagRepositoryImpl.class,
    ContentEntityMapper.class,
    TagEntityMapper.class
})
@DisplayName("ContentTagRepositoryImpl 슬라이스 테스트")
class ContentTagRepositoryImplTest {

    @Autowired
    private ContentTagRepository contentTagRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private JpaContentTagRepository jpaContentTagRepository;

    @Nested
    @DisplayName("saveAll()")
    class SaveAllTest {

        @Test
        @DisplayName("콘텐츠와 태그를 연결한다")
        void saveAll_linksContentAndTags() {
            // given
            ContentModel content = ContentModel.create(
                "영화",
                "인셉션",
                "꿈속의 꿈",
                "https://mopl.com/inception.png"
            );
            ContentModel savedContent = contentRepository.save(content);

            TagModel tag1 = tagRepository.save(TagModel.create("SF"));
            TagModel tag2 = tagRepository.save(TagModel.create("액션"));

            // when
            contentTagRepository.saveAll(
                savedContent.getId(),
                List.of(tag1, tag2)
            );

            // then
            List<ContentTagEntity> contentTags = jpaContentTagRepository.findAll();

            assertThat(contentTags).hasSize(2);

            assertThat(contentTags)
                .allMatch(ct -> ct.getContent().getId()
                    .equals(savedContent.getId()));

            assertThat(contentTags)
                .extracting(ct -> ct.getTag().getName())
                .containsExactlyInAnyOrder("SF", "액션");
        }
    }

    @Nested
    @DisplayName("findTagsByContentId()")
    class FindTagsByContentIdTest {

        @Test
        @DisplayName("콘텐츠 ID로 연결된 모든 태그 모델 목록을 조회한다")
        void findTagsByContentId_returnsTagModels() {
            // given
            ContentModel savedContent = contentRepository.save(
                ContentModel.create("영화", "인셉션", "꿈", "url")
            );

            TagModel tag1 = tagRepository.save(TagModel.create("SF"));
            TagModel tag2 = tagRepository.save(TagModel.create("액션"));

            contentTagRepository.saveAll(
                savedContent.getId(),
                List.of(tag1, tag2)
            );

            // when
            List<TagModel> result = contentTagRepository.findTagsByContentId(savedContent.getId());

            // then
            assertThat(result).hasSize(2);
            assertThat(result)
                .extracting(TagModel::getName)
                .containsExactlyInAnyOrder("SF", "액션");

            assertThat(result)
                .extracting(TagModel::getId)
                .containsExactlyInAnyOrder(tag1.getId(), tag2.getId());
        }

        @Test
        @DisplayName("태그가 연결되지 않은 콘텐츠 ID로 조회 시 빈 리스트를 반환한다")
        void withNoTags_returnsEmptyList() {
            // given
            ContentModel savedContent = contentRepository.save(
                ContentModel.create("영화", "인셉션", "꿈", "url")
            );

            // when
            List<TagModel> result = contentTagRepository.findTagsByContentId(savedContent.getId());

            // then
            assertThat(result).isEmpty();
        }
    }
}
