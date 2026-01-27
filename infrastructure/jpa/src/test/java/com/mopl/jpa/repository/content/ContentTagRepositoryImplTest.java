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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    private ContentModel content;
    private TagModel tag1;
    private TagModel tag2;

    @BeforeEach
    void setUp() {
        content = contentRepository.save(
            ContentModel.create(
                ContentModel.ContentType.movie,
                "인셉션",
                "꿈속의 꿈",
                "https://mopl.com/inception.png"
            )
        );

        List<TagModel> savedTags = tagRepository.saveAll(List.of(
            TagModel.create("SF"),
            TagModel.create("액션")
        ));
        tag1 = savedTags.get(0);
        tag2 = savedTags.get(1);
    }

    @Nested
    @DisplayName("findTagsByContentId()")
    class FindTagsByContentIdTest {

        @Test
        @DisplayName("콘텐츠에 연결된 태그 목록을 반환한다")
        void whenTagsExist_returnsTags() {
            // given
            contentTagRepository.saveAll(content.getId(), List.of(tag1, tag2));

            // when
            List<TagModel> tags = contentTagRepository.findTagsByContentId(content.getId());

            // then
            assertThat(tags).hasSize(2);
            assertThat(tags).extracting(TagModel::getName)
                .containsExactlyInAnyOrder("SF", "액션");
        }

        @Test
        @DisplayName("콘텐츠에 연결된 태그가 없으면 빈 목록을 반환한다")
        void whenNoTags_returnsEmptyList() {
            // when
            List<TagModel> tags = contentTagRepository.findTagsByContentId(content.getId());

            // then
            assertThat(tags).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠 ID로 조회하면 빈 목록을 반환한다")
        void whenContentNotExists_returnsEmptyList() {
            // when
            List<TagModel> tags = contentTagRepository.findTagsByContentId(UUID.randomUUID());

            // then
            assertThat(tags).isEmpty();
        }
    }

    @Nested
    @DisplayName("findTagsByContentIdIn()")
    class FindTagsByContentIdInTest {

        @Test
        @DisplayName("여러 콘텐츠의 태그 맵을 반환한다")
        void whenMultipleContents_returnsTagMap() {
            // given
            ContentModel anotherContent = contentRepository.save(
                ContentModel.create(
                    ContentModel.ContentType.movie,
                    "다크나이트",
                    "배트맨",
                    "https://mopl.com/darkknight.png"
                )
            );
            TagModel tag3 = tagRepository.saveAll(List.of(TagModel.create("범죄"))).getFirst();

            contentTagRepository.saveAll(content.getId(), List.of(tag1, tag2));
            contentTagRepository.saveAll(anotherContent.getId(), List.of(tag2, tag3));

            // when
            Map<UUID, List<TagModel>> tagMap = contentTagRepository.findTagsByContentIdIn(
                List.of(content.getId(), anotherContent.getId())
            );

            // then
            assertThat(tagMap).hasSize(2);
            assertThat(tagMap.get(content.getId())).hasSize(2);
            assertThat(tagMap.get(content.getId())).extracting(TagModel::getName)
                .containsExactlyInAnyOrder("SF", "액션");
            assertThat(tagMap.get(anotherContent.getId())).hasSize(2);
            assertThat(tagMap.get(anotherContent.getId())).extracting(TagModel::getName)
                .containsExactlyInAnyOrder("액션", "범죄");
        }

        @Test
        @DisplayName("빈 목록을 전달하면 빈 맵을 반환한다")
        void whenEmptyList_returnsEmptyMap() {
            // when
            Map<UUID, List<TagModel>> tagMap = contentTagRepository.findTagsByContentIdIn(List.of());

            // then
            assertThat(tagMap).isEmpty();
        }

        @Test
        @DisplayName("null을 전달하면 빈 맵을 반환한다")
        void whenNull_returnsEmptyMap() {
            // when
            Map<UUID, List<TagModel>> tagMap = contentTagRepository.findTagsByContentIdIn(null);

            // then
            assertThat(tagMap).isEmpty();
        }

        @Test
        @DisplayName("태그가 없는 콘텐츠는 맵에 포함되지 않는다")
        void whenContentHasNoTags_notIncludedInMap() {
            // given
            ContentModel contentWithTags = contentRepository.save(
                ContentModel.create(
                    ContentModel.ContentType.movie,
                    "다크나이트",
                    "배트맨",
                    "https://mopl.com/darkknight.png"
                )
            );
            contentTagRepository.saveAll(contentWithTags.getId(), List.of(tag1));

            // when
            Map<UUID, List<TagModel>> tagMap = contentTagRepository.findTagsByContentIdIn(
                List.of(content.getId(), contentWithTags.getId())
            );

            // then
            assertThat(tagMap).hasSize(1);
            assertThat(tagMap).containsKey(contentWithTags.getId());
            assertThat(tagMap).doesNotContainKey(content.getId());
        }
    }

    @Nested
    @DisplayName("saveAll()")
    class SaveAllTest {

        @Test
        @DisplayName("콘텐츠에 태그 목록을 저장한다")
        void saveAll_savesTagsToContent() {
            // when
            contentTagRepository.saveAll(content.getId(), List.of(tag1, tag2));

            // then
            List<ContentTagEntity> contentTags = jpaContentTagRepository.findAll();
            assertThat(contentTags).hasSize(2);
            assertThat(contentTags).allMatch(ct -> ct.getContent().getId().equals(content.getId()));
        }

        @Test
        @DisplayName("빈 태그 목록을 저장해도 오류가 발생하지 않는다")
        void saveAll_withEmptyList_succeeds() {
            // when
            contentTagRepository.saveAll(content.getId(), List.of());

            // then
            List<ContentTagEntity> contentTags = jpaContentTagRepository.findAll();
            assertThat(contentTags).isEmpty();
        }

        @Test
        @DisplayName("같은 태그를 여러 콘텐츠에 연결할 수 있다")
        void saveAll_sameTagToMultipleContents() {
            // given
            ContentModel anotherContent = contentRepository.save(
                ContentModel.create(
                    ContentModel.ContentType.movie,
                    "다크나이트",
                    "배트맨",
                    "https://mopl.com/darkknight.png"
                )
            );

            // when
            contentTagRepository.saveAll(content.getId(), List.of(tag1));
            contentTagRepository.saveAll(anotherContent.getId(), List.of(tag1));

            // then
            List<ContentTagEntity> contentTags = jpaContentTagRepository.findAll();
            assertThat(contentTags).hasSize(2);
        }
    }

    @Nested
    @DisplayName("deleteByContentId()")
    class DeleteByContentIdTest {

        @Test
        @DisplayName("콘텐츠의 모든 태그 연결을 삭제한다")
        void deleteByContentId_removesAllTags() {
            // given
            contentTagRepository.saveAll(content.getId(), List.of(tag1, tag2));
            assertThat(jpaContentTagRepository.findAll()).hasSize(2);

            // when
            contentTagRepository.deleteByContentId(content.getId());

            // then
            assertThat(jpaContentTagRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("다른 콘텐츠의 태그 연결은 유지된다")
        void deleteByContentId_keepsOtherContentTags() {
            // given
            ContentModel anotherContent = contentRepository.save(
                ContentModel.create(
                    ContentModel.ContentType.movie,
                    "다크나이트",
                    "배트맨",
                    "https://mopl.com/darkknight.png"
                )
            );
            contentTagRepository.saveAll(content.getId(), List.of(tag1));
            contentTagRepository.saveAll(anotherContent.getId(), List.of(tag2));
            assertThat(jpaContentTagRepository.findAll()).hasSize(2);

            // when
            contentTagRepository.deleteByContentId(content.getId());

            // then
            List<ContentTagEntity> remaining = jpaContentTagRepository.findAll();
            assertThat(remaining).hasSize(1);
            assertThat(remaining.getFirst().getContent().getId()).isEqualTo(anotherContent.getId());
        }

        @Test
        @DisplayName("태그가 없는 콘텐츠 삭제 시 오류가 발생하지 않는다")
        void deleteByContentId_whenNoTags_succeeds() {
            // when & then - no exception
            contentTagRepository.deleteByContentId(content.getId());
            assertThat(jpaContentTagRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠 ID로 삭제해도 오류가 발생하지 않는다")
        void deleteByContentId_whenContentNotExists_succeeds() {
            // when & then - no exception
            contentTagRepository.deleteByContentId(UUID.randomUUID());
            assertThat(jpaContentTagRepository.findAll()).isEmpty();
        }
    }
}
