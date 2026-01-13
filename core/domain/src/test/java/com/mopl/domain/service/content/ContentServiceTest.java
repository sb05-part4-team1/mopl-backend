package com.mopl.domain.service.content;

import com.mopl.domain.exception.content.ContentNotFoundException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.repository.content.ContentTagRepository;
import com.mopl.domain.service.tag.TagService;
import java.time.Instant;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

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
            // given
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

            // when
            ContentModel result = contentService.create(contentModel, tagNames);

            // then
            assertThat(result.getTags()).containsExactly("SF", "액션");
            then(contentTagRepository).should().saveAll(contentId, tags);
        }

        @Test
        @DisplayName("태그명이 null이면 태그 로직을 타지 않는다")
        void withNullTagNames_onlySaveContent() {
            // given
            ContentModel contentModel = ContentModel.builder()
                .title("인셉션")
                .build();

            given(contentRepository.save(contentModel)).willReturn(contentModel);

            // when
            ContentModel result = contentService.create(contentModel, null);

            // then
            assertThat(result.getTags()).isEmpty();
            then(tagService).shouldHaveNoInteractions();
            then(contentTagRepository).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTest {

        @Test
        @DisplayName("태그 조회 결과가 null이면 빈 리스트 반환 (toTagNames 분기)")
        void returnsEmptyTags_whenTagRepositoryReturnsNull() {
            // given
            UUID contentId = UUID.randomUUID();
            ContentModel contentModel = ContentModel.builder()
                .id(contentId)
                .title("인셉션")
                .build();

            given(contentRepository.findById(contentId)).willReturn(Optional.of(contentModel));
            given(contentTagRepository.findTagsByContentId(contentId)).willReturn(null);

            // when
            ContentModel result = contentService.getById(contentId);

            // then
            assertThat(result.getTags()).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않으면 예외 발생")
        void notFound_throwsException() {
            // given
            UUID contentId = UUID.randomUUID();
            given(contentRepository.findById(contentId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> contentService.getById(contentId))
                .isInstanceOf(ContentNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTest {

        @Test
        @DisplayName("tagNames가 null이면 태그를 변경하지 않는다 (early return 분기)")
        void update_withNullTags_doesNotTouchTags() {
            // given
            UUID contentId = UUID.randomUUID();
            ContentModel original = ContentModel.builder()
                .id(contentId)
                .type(ContentModel.ContentType.movie)
                .title("기존 제목")
                .description("설명")
                .thumbnailUrl("old.png")
                .build();

            given(contentRepository.findById(contentId)).willReturn(Optional.of(original));
            given(contentRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            ContentModel result = contentService.update(
                contentId,
                "새 제목",
                null,
                null,
                null
            );

            // then
            assertThat(result.getTitle()).isEqualTo("새 제목");

            then(contentTagRepository).should(never()).deleteAllByContentId(any());
            then(contentTagRepository).should(never()).saveAll(any(), any());
            then(tagService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("기존 태그 삭제 후 새 태그로 갱신")
        void update_replacesTags() {
            // given
            UUID contentId = UUID.randomUUID();
            ContentModel original = ContentModel.builder()
                .id(contentId)
                .type(ContentModel.ContentType.movie)
                .title("기존 제목")
                .description("설명")
                .thumbnailUrl("old.png")
                .build();

            List<String> tagNames = List.of("드라마");
            List<TagModel> tags = List.of(
                TagModel.builder().id(UUID.randomUUID()).name("드라마").build()
            );

            given(contentRepository.findById(contentId)).willReturn(Optional.of(original));
            given(contentRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
            given(tagService.findOrCreateTags(tagNames)).willReturn(tags);

            // when
            ContentModel result = contentService.update(
                contentId,
                "새 제목",
                "새 설명",
                null,
                tagNames
            );

            // then
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
            // given
            UUID id = UUID.randomUUID();
            given(contentRepository.existsById(id)).willReturn(true);

            // when
            boolean result = contentService.exists(id);

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTest {

        @Test
        @DisplayName("콘텐츠 삭제 시 deleteContent 후 저장된다")
        void delete_softDeletesContent() {
            // given
            UUID contentId = UUID.randomUUID();
            ContentModel content = ContentModel.builder()
                .id(contentId)
                .build();

            given(contentRepository.findById(contentId))
                .willReturn(Optional.of(content));
            given(contentRepository.save(any()))
                .willAnswer(inv -> inv.getArgument(0));

            // when
            contentService.delete(contentId);

            // then
            then(contentRepository).should().save(
                argThat(ContentModel::isDeleted)
            );
        }

        @Test
        @DisplayName("이미 삭제된 콘텐츠도 예외 없이 처리된다 (멱등성)")
        void delete_isIdempotent() {
            // given
            UUID contentId = UUID.randomUUID();
            ContentModel content = ContentModel.builder()
                .id(contentId)
                .deletedAt(Instant.now())
                .build();

            given(contentRepository.findById(contentId))
                .willReturn(Optional.of(content));

            // when
            contentService.delete(contentId);

            // then
            then(contentRepository).should().save(
                argThat(model -> model.getId().equals(contentId) &&
                    model.getDeletedAt() != null
                )
            );
        }
    }
}
