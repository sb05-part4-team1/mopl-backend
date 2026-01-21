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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
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
    @DisplayName("findTagsByContentId()")
    class FindTagsByContentIdTest {

        @Test
        @DisplayName("콘텐츠 ID로 연결된 태그를 조회한다")
        void findTagsByContentId_returnsTags() {
            ContentModel savedContent = contentRepository.save(
                ContentModel.create(ContentModel.ContentType.movie, "인셉션", "꿈", "url")
            );

            TagModel tag1 = tagRepository.save(TagModel.create("SF"));
            TagModel tag2 = tagRepository.save(TagModel.create("액션"));

            contentTagRepository.saveAll(
                savedContent.getId(),
                List.of(tag1, tag2)
            );

            List<TagModel> result = contentTagRepository.findTagsByContentId(savedContent.getId());

            assertThat(result).hasSize(2);
            assertThat(result)
                .extracting(TagModel::getName)
                .containsExactlyInAnyOrder("SF", "액션");
        }

        @Test
        @DisplayName("연결된 태그가 없으면 빈 리스트를 반환한다")
        void findTagsByContentId_noTags() {
            ContentModel savedContent = contentRepository.save(
                ContentModel.create(ContentModel.ContentType.movie, "인셉션", "꿈", "url")
            );

            List<TagModel> result = contentTagRepository.findTagsByContentId(savedContent.getId());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findTagsByContentIds()")
    class FindTagsByContentIdsTest {

        @Test
        @DisplayName("여러 콘텐츠 ID로 태그를 조회하면 Map으로 그룹핑하여 반환한다")
        void findTagsByContentIds_returnsGroupedMap() {
            ContentModel content1 = contentRepository.save(
                ContentModel.create(ContentModel.ContentType.movie, "인셉션", "꿈", "url1")
            );
            ContentModel content2 = contentRepository.save(
                ContentModel.create(ContentModel.ContentType.movie, "다크나이트", "배트맨", "url2")
            );

            TagModel tagSf = tagRepository.save(TagModel.create("SF"));
            TagModel tagAction = tagRepository.save(TagModel.create("액션"));
            TagModel tagDrama = tagRepository.save(TagModel.create("드라마"));

            contentTagRepository.saveAll(content1.getId(), List.of(tagSf, tagAction));
            contentTagRepository.saveAll(content2.getId(), List.of(tagAction, tagDrama));

            var result = contentTagRepository.findTagsByContentIds(
                List.of(content1.getId(), content2.getId())
            );

            assertThat(result).hasSize(2);
            assertThat(result.get(content1.getId()))
                .extracting(TagModel::getName)
                .containsExactlyInAnyOrder("SF", "액션");
            assertThat(result.get(content2.getId()))
                .extracting(TagModel::getName)
                .containsExactlyInAnyOrder("액션", "드라마");
        }

        @Test
        @DisplayName("null을 입력하면 빈 Map을 반환한다")
        void findTagsByContentIds_nullInput_returnsEmptyMap() {
            var result = contentTagRepository.findTagsByContentIds(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("빈 리스트를 입력하면 빈 Map을 반환한다")
        void findTagsByContentIds_emptyList_returnsEmptyMap() {
            var result = contentTagRepository.findTagsByContentIds(List.of());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("일부 콘텐츠만 태그가 있는 경우 해당 콘텐츠만 Map에 포함된다")
        void findTagsByContentIds_partialTags_returnsOnlyContentWithTags() {
            ContentModel contentWithTags = contentRepository.save(
                ContentModel.create(ContentModel.ContentType.movie, "인셉션", "꿈", "url1")
            );
            ContentModel contentWithoutTags = contentRepository.save(
                ContentModel.create(ContentModel.ContentType.movie, "다크나이트", "배트맨", "url2")
            );

            TagModel tag = tagRepository.save(TagModel.create("SF"));
            contentTagRepository.saveAll(contentWithTags.getId(), List.of(tag));

            var result = contentTagRepository.findTagsByContentIds(
                List.of(contentWithTags.getId(), contentWithoutTags.getId())
            );

            assertThat(result).hasSize(1);
            assertThat(result).containsKey(contentWithTags.getId());
            assertThat(result).doesNotContainKey(contentWithoutTags.getId());
            assertThat(result.get(contentWithTags.getId()))
                .extracting(TagModel::getName)
                .containsExactly("SF");
        }
    }

    @Nested
    @DisplayName("saveAll()")
    class SaveAllTest {

        @Test
        @DisplayName("콘텐츠와 태그를 연결한다")
        void saveAll_linksContentAndTags() {
            ContentModel savedContent = contentRepository.save(
                ContentModel.create(ContentModel.ContentType.movie, "인셉션", "꿈속의 꿈", "url")
            );

            TagModel tag1 = tagRepository.save(TagModel.create("SF"));
            TagModel tag2 = tagRepository.save(TagModel.create("액션"));

            contentTagRepository.saveAll(
                savedContent.getId(),
                List.of(tag1, tag2)
            );

            List<ContentTagEntity> contentTags = jpaContentTagRepository.findAll();

            assertThat(contentTags).hasSize(2);
            assertThat(contentTags)
                .extracting(ct -> ct.getTag().getName())
                .containsExactlyInAnyOrder("SF", "액션");
        }
    }

    @Nested
    @DisplayName("deleteAllByContentId()")
    class DeleteAllByContentIdTest {

        @Test
        @DisplayName("특정 콘텐츠의 태그 연결만 삭제한다")
        void deleteAllByContentId_deletesOnlyTargetContentTags() {
            ContentModel content1 = contentRepository.save(
                ContentModel.create(ContentModel.ContentType.movie, "인셉션", "꿈", "url1")
            );
            ContentModel content2 = contentRepository.save(
                ContentModel.create(ContentModel.ContentType.movie, "다크나이트", "배트맨", "url2")
            );

            TagModel tag1 = tagRepository.save(TagModel.create("SF"));
            TagModel tag2 = tagRepository.save(TagModel.create("액션"));

            contentTagRepository.saveAll(content1.getId(), List.of(tag1, tag2));
            contentTagRepository.saveAll(content2.getId(), List.of(tag1));

            assertThat(jpaContentTagRepository.findAll()).hasSize(3);

            contentTagRepository.deleteAllByContentId(content1.getId());

            List<ContentTagEntity> remaining = jpaContentTagRepository.findAll();

            assertThat(remaining).hasSize(1);
            assertThat(remaining.get(0).getContent().getId())
                .isEqualTo(content2.getId());
        }

        @Test
        @DisplayName("연결된 태그가 없어도 예외 없이 동작한다")
        void deleteAllByContentId_noTags() {
            ContentModel savedContent = contentRepository.save(
                ContentModel.create(ContentModel.ContentType.movie, "인셉션", "꿈", "url")
            );

            contentTagRepository.deleteAllByContentId(savedContent.getId());

            assertThat(jpaContentTagRepository.findAll()).isEmpty();
        }
    }
}
