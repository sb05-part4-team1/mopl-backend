package com.mopl.domain.service.content;

import com.mopl.domain.exception.content.ContentNotFoundException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.repository.content.ContentTagRepository;
import com.mopl.domain.service.tag.TagService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContentService 단위 테스트")
class ContentServiceTest {

    @Mock
    private TagService tagService;

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private ContentTagRepository contentTagRepository;

    @InjectMocks
    private ContentService contentService;

    @Nested
    @DisplayName("create()")
    class CreateTest {

        @Test
        @DisplayName("태그명이 있으면 태그 생성 후 연관관계 저장")
        void withTagNames_createsTagsAndRelations() {
            UUID contentId = UUID.randomUUID();
            ContentModel contentModel = ContentModel.builder()
                .id(contentId)
                .title("인셉션")
                .build();

            List<String> tagNames = List.of("SF", "액션");
            List<TagModel> tags = List.of(
                TagModel.builder().id(UUID.randomUUID()).name("SF").build(),
                TagModel.builder().id(UUID.randomUUID()).name("액션").build()
            );

            given(contentRepository.save(contentModel)).willReturn(contentModel);
            given(tagService.findOrCreateTags(tagNames)).willReturn(tags);

            ContentModel result = contentService.create(contentModel, tagNames);

            assertThat(result.getTags()).containsExactly("SF", "액션");
            then(contentRepository).should().save(contentModel);
            then(tagService).should().findOrCreateTags(tagNames);
            then(contentTagRepository).should().saveAll(contentId, tags);
        }

        @Test
        @DisplayName("태그명이 없으면 태그 로직을 타지 않는다")
        void withoutTagNames_onlySaveContent() {
            ContentModel contentModel = ContentModel.builder()
                .title("인셉션")
                .build();

            given(contentRepository.save(contentModel)).willReturn(contentModel);

            ContentModel result = contentService.create(contentModel, List.of());

            assertThat(result.getTags()).isEmpty();
            then(tagService).shouldHaveNoInteractions();
            then(contentTagRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("태그명이 null이면 태그 로직을 타지 않는다")
        void withNullTagNames_onlySaveContent() {
            ContentModel contentModel = ContentModel.builder()
                .title("인셉션")
                .build();

            given(contentRepository.save(contentModel)).willReturn(contentModel);

            ContentModel result = contentService.create(contentModel, null);

            assertThat(result.getTags()).isEmpty();
            then(tagService).shouldHaveNoInteractions();
            then(contentTagRepository).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTest {

        @Test
        @DisplayName("콘텐츠와 태그를 함께 조회한다")
        void returnsContentWithTags() {
            UUID contentId = UUID.randomUUID();
            ContentModel contentModel = ContentModel.builder()
                .id(contentId)
                .title("인셉션")
                .build();

            List<TagModel> tags = List.of(
                TagModel.builder().name("SF").build(),
                TagModel.builder().name("액션").build()
            );

            given(contentRepository.findById(contentId)).willReturn(Optional.of(contentModel));
            given(contentTagRepository.findTagsByContentId(contentId)).willReturn(tags);

            ContentModel result = contentService.getById(contentId);

            assertThat(result.getTags()).containsExactly("SF", "액션");
        }

        @Test
        @DisplayName("존재하지 않으면 예외 발생")
        void notFound_throwsException() {
            UUID contentId = UUID.randomUUID();
            given(contentRepository.findById(contentId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> contentService.getById(contentId))
                .isInstanceOf(ContentNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTest {

        @Test
        @DisplayName("기존 태그 삭제 후 새 태그로 갱신")
        void update_replacesTags() {
            UUID contentId = UUID.randomUUID();
            ContentModel original = ContentModel.builder()
                .id(contentId)
                .type("영화")
                .title("기존 제목")
                .thumbnailUrl("old.png")
                .build();

            List<String> tagNames = List.of("드라마");
            List<TagModel> tags = List.of(
                TagModel.builder().id(UUID.randomUUID()).name("드라마").build()
            );

            given(contentRepository.findById(contentId)).willReturn(Optional.of(original));
            given(contentRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
            given(tagService.findOrCreateTags(tagNames)).willReturn(tags);

            ContentModel result = contentService.update(
                contentId,
                "새 제목",
                "설명",
                null,
                tagNames
            );

            assertThat(result.getTitle()).isEqualTo("새 제목");
            assertThat(result.getTags()).containsExactly("드라마");

            then(contentTagRepository).should().deleteAllByContentId(contentId);
            then(contentTagRepository).should().saveAll(contentId, tags);
        }
    }

    @Nested
    @DisplayName("exists()")
    class ExistsTest {

        @Test
        void exists_returnsTrue() {
            UUID id = UUID.randomUUID();
            given(contentRepository.existsById(id)).willReturn(true);

            assertThat(contentService.exists(id)).isTrue();
        }

        @Test
        void notExists_returnsFalse() {
            UUID id = UUID.randomUUID();
            given(contentRepository.existsById(id)).willReturn(false);

            assertThat(contentService.exists(id)).isFalse();
        }
    }
}
