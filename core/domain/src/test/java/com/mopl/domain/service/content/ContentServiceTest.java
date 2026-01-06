package com.mopl.domain.service.content;

import com.mopl.domain.exception.content.ContentNotFoundException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.repository.content.ContentRepository;
import com.mopl.domain.repository.content.ContentTagRepository;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContentService 단위 테스트")
class ContentServiceTest {

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
        @DisplayName("콘텐츠와 태그를 저장하고 태그명이 포함된 모델을 반환한다")
        void withModelAndTags_returnsSavedModelWithTags() {
            UUID contentId = UUID.randomUUID();
            ContentModel contentModel = ContentModel.builder()
                .id(contentId)
                .title("인셉션")
                .build();

            List<TagModel> tags = List.of(
                TagModel.builder().name("SF").build(),
                TagModel.builder().name("액션").build()
            );

            given(contentRepository.save(contentModel)).willReturn(contentModel);

            ContentModel result = contentService.create(contentModel, tags);

            assertThat(result.getTags()).containsExactly("SF", "액션");
            then(contentRepository).should().save(contentModel);
            then(contentTagRepository).should().saveAll(contentId, tags);
        }

        @Test
        @DisplayName("태그 리스트가 비어있으면 콘텐츠만 저장하고 빈 태그 리스트를 반환한다")
        void withEmptyTags_onlySaveContent() {
            ContentModel contentModel = ContentModel.builder()
                .title("인셉션")
                .build();

            given(contentRepository.save(contentModel)).willReturn(contentModel);

            ContentModel result = contentService.create(contentModel, List.of());

            assertThat(result.getTags()).isEmpty();
            then(contentRepository).should().save(contentModel);
            then(contentTagRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("태그가 null이면 콘텐츠만 저장하고 빈 태그 리스트를 반환한다")
        void withNullTags_onlySaveContent() {
            ContentModel contentModel = ContentModel.builder()
                .title("인셉션")
                .build();

            given(contentRepository.save(contentModel)).willReturn(contentModel);

            ContentModel result = contentService.create(contentModel, null);

            assertThat(result.getTags()).isEmpty();
            then(contentRepository).should().save(contentModel);
            then(contentTagRepository).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTest {

        @Test
        @DisplayName("ID로 조회 시 태그 정보가 결합된 모델을 반환한다")
        void withContentId_returnsModelWithTags() {
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

            assertThat(result.getId()).isEqualTo(contentId);
            assertThat(result.getTags()).containsExactly("SF", "액션");
            then(contentRepository).should().findById(contentId);
            then(contentTagRepository).should().findTagsByContentId(contentId);
        }

        @Test
        @DisplayName("태그가 없으면 빈 태그 리스트를 포함한 모델을 반환한다")
        void withNoTags_returnsModelWithEmptyTags() {
            UUID contentId = UUID.randomUUID();
            ContentModel contentModel = ContentModel.builder()
                .id(contentId)
                .title("인셉션")
                .build();

            given(contentRepository.findById(contentId)).willReturn(Optional.of(contentModel));
            given(contentTagRepository.findTagsByContentId(contentId)).willReturn(List.of());

            ContentModel result = contentService.getById(contentId);

            assertThat(result.getTags()).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 ID 조회 시 예외 발생")
        void withNonExistentId_throwsException() {
            UUID contentId = UUID.randomUUID();
            given(contentRepository.findById(contentId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> contentService.getById(contentId))
                .isInstanceOf(ContentNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("exists()")
    class ExistsTest {

        @Test
        @DisplayName("콘텐츠가 존재하면 true를 반환한다")
        void exists_returnsTrue() {
            UUID contentId = UUID.randomUUID();
            given(contentRepository.existsById(contentId)).willReturn(true);

            boolean result = contentService.exists(contentId);

            assertThat(result).isTrue();
            then(contentRepository).should().existsById(contentId);
        }

        @Test
        @DisplayName("콘텐츠가 존재하지 않으면 false를 반환한다")
        void notExists_returnsFalse() {
            UUID contentId = UUID.randomUUID();
            given(contentRepository.existsById(contentId)).willReturn(false);

            boolean result = contentService.exists(contentId);

            assertThat(result).isFalse();
            then(contentRepository).should().existsById(contentId);
        }
    }
}
