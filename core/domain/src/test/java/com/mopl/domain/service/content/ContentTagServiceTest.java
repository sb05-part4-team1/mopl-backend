package com.mopl.domain.service.content;

import com.mopl.domain.fixture.TagModelFixture;
import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.repository.content.ContentTagRepository;
import com.mopl.domain.repository.tag.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContentTagService 단위 테스트")
class ContentTagServiceTest {

    @Mock
    private ContentTagRepository contentTagRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private ContentTagService contentTagService;

    @Captor
    private ArgumentCaptor<Collection<String>> tagNamesCaptor;

    @Captor
    private ArgumentCaptor<List<TagModel>> tagsCaptor;

    @Nested
    @DisplayName("getTagsByContentId()")
    class GetTagsByContentIdTest {

        @Test
        @DisplayName("Repository에 위임하여 태그 목록 반환")
        void delegatesToRepository() {
            // given
            UUID contentId = UUID.randomUUID();
            TagModel tag1 = TagModelFixture.create();
            TagModel tag2 = TagModelFixture.create();
            List<TagModel> expectedTags = List.of(tag1, tag2);

            given(contentTagRepository.findTagsByContentId(contentId)).willReturn(expectedTags);

            // when
            List<TagModel> result = contentTagService.getTagsByContentId(contentId);

            // then
            assertThat(result).isEqualTo(expectedTags);
            then(contentTagRepository).should().findTagsByContentId(contentId);
        }

        @Test
        @DisplayName("태그가 없으면 빈 목록 반환")
        void withNoTags_returnsEmptyList() {
            // given
            UUID contentId = UUID.randomUUID();

            given(contentTagRepository.findTagsByContentId(contentId)).willReturn(List.of());

            // when
            List<TagModel> result = contentTagService.getTagsByContentId(contentId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getTagsByContentIdIn()")
    class GetTagsByContentIdInTest {

        @Test
        @DisplayName("null 입력 시 빈 맵 반환")
        void withNull_returnsEmptyMap() {
            // when
            Map<UUID, List<TagModel>> result = contentTagService.getTagsByContentIdIn(null);

            // then
            assertThat(result).isEmpty();
            then(contentTagRepository).should(never()).findTagsByContentIdIn(any());
        }

        @Test
        @DisplayName("빈 목록 입력 시 빈 맵 반환")
        void withEmptyList_returnsEmptyMap() {
            // when
            Map<UUID, List<TagModel>> result = contentTagService.getTagsByContentIdIn(List.of());

            // then
            assertThat(result).isEmpty();
            then(contentTagRepository).should(never()).findTagsByContentIdIn(any());
        }

        @Test
        @DisplayName("Repository에 위임하여 콘텐츠별 태그 맵 반환")
        void delegatesToRepository() {
            // given
            UUID contentId1 = UUID.randomUUID();
            UUID contentId2 = UUID.randomUUID();
            List<UUID> contentIds = List.of(contentId1, contentId2);

            TagModel tag1 = TagModelFixture.create();
            TagModel tag2 = TagModelFixture.create();
            Map<UUID, List<TagModel>> expectedMap = Map.of(
                contentId1, List.of(tag1),
                contentId2, List.of(tag2)
            );

            given(contentTagRepository.findTagsByContentIdIn(contentIds)).willReturn(expectedMap);

            // when
            Map<UUID, List<TagModel>> result = contentTagService.getTagsByContentIdIn(contentIds);

            // then
            assertThat(result).isEqualTo(expectedMap);
            then(contentTagRepository).should().findTagsByContentIdIn(contentIds);
        }
    }

    @Nested
    @DisplayName("applyTags()")
    class ApplyTagsTest {

        @Test
        @DisplayName("null 입력 시 아무 작업도 하지 않음")
        void withNull_doesNothing() {
            // given
            UUID contentId = UUID.randomUUID();

            // when
            contentTagService.applyTags(contentId, null);

            // then
            then(tagRepository).should(never()).findByNameIn(any());
            then(tagRepository).should(never()).saveAll(any());
            then(contentTagRepository).should(never()).saveAll(any(), any());
        }

        @Test
        @DisplayName("빈 목록 입력 시 아무 작업도 하지 않음")
        void withEmptyList_doesNothing() {
            // given
            UUID contentId = UUID.randomUUID();

            // when
            contentTagService.applyTags(contentId, List.of());

            // then
            then(tagRepository).should(never()).findByNameIn(any());
            then(tagRepository).should(never()).saveAll(any());
            then(contentTagRepository).should(never()).saveAll(any(), any());
        }

        @Test
        @DisplayName("공백 문자열만 있는 경우 아무 작업도 하지 않음")
        void withOnlyBlankStrings_doesNothing() {
            // given
            UUID contentId = UUID.randomUUID();

            // when
            contentTagService.applyTags(contentId, List.of("", "  ", "   "));

            // then
            then(tagRepository).should(never()).findByNameIn(any());
            then(tagRepository).should(never()).saveAll(any());
            then(contentTagRepository).should(never()).saveAll(any(), any());
        }

        @Test
        @DisplayName("null 요소가 포함된 경우 필터링 후 처리")
        void withNullElements_filtersAndProcesses() {
            // given
            UUID contentId = UUID.randomUUID();
            TagModel existingTag = TagModelFixture.builder()
                .set("name", "액션")
                .sample();

            given(tagRepository.findByNameIn(anyCollection())).willReturn(List.of(existingTag));
            given(tagRepository.saveAll(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            contentTagService.applyTags(contentId, Arrays.asList(null, "액션", null));

            // then
            then(tagRepository).should().findByNameIn(tagNamesCaptor.capture());
            assertThat(tagNamesCaptor.getValue()).containsExactly("액션");
        }

        @Test
        @DisplayName("기존 태그가 있으면 재사용")
        void withExistingTags_reusesThem() {
            // given
            UUID contentId = UUID.randomUUID();
            TagModel existingTag = TagModelFixture.builder()
                .set("name", "액션")
                .sample();

            given(tagRepository.findByNameIn(anyCollection())).willReturn(List.of(existingTag));
            given(tagRepository.saveAll(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            contentTagService.applyTags(contentId, List.of("액션"));

            // then
            then(tagRepository).should().saveAll(tagsCaptor.capture());
            List<TagModel> savedTags = tagsCaptor.getValue();
            assertThat(savedTags).hasSize(1);
            assertThat(savedTags.getFirst().getId()).isEqualTo(existingTag.getId());
        }

        @Test
        @DisplayName("새 태그는 생성하고 기존 태그는 재사용")
        void withMixedTags_createsNewAndReusesExisting() {
            // given
            UUID contentId = UUID.randomUUID();
            TagModel existingTag = TagModelFixture.builder()
                .set("name", "액션")
                .sample();

            given(tagRepository.findByNameIn(anyCollection())).willReturn(List.of(existingTag));
            given(tagRepository.saveAll(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            contentTagService.applyTags(contentId, List.of("액션", "코미디"));

            // then
            then(tagRepository).should().saveAll(tagsCaptor.capture());
            List<TagModel> savedTags = tagsCaptor.getValue();
            assertThat(savedTags).hasSize(2);

            TagModel reusedTag = savedTags.stream()
                .filter(t -> "액션".equals(t.getName()))
                .findFirst()
                .orElseThrow();
            assertThat(reusedTag.getId()).isEqualTo(existingTag.getId());

            TagModel newTag = savedTags.stream()
                .filter(t -> "코미디".equals(t.getName()))
                .findFirst()
                .orElseThrow();
            assertThat(newTag.getId()).isNull();

            then(contentTagRepository).should().saveAll(eq(contentId), any());
        }

        @Test
        @DisplayName("중복 태그는 제거됨")
        void withDuplicateTags_removeDuplicates() {
            // given
            UUID contentId = UUID.randomUUID();

            given(tagRepository.findByNameIn(anyCollection())).willReturn(List.of());
            given(tagRepository.saveAll(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            contentTagService.applyTags(contentId, List.of("액션", "액션", "액션"));

            // then
            then(tagRepository).should().findByNameIn(tagNamesCaptor.capture());
            assertThat(tagNamesCaptor.getValue()).containsExactly("액션");
        }

        @Test
        @DisplayName("태그 이름 앞뒤 공백 제거")
        void withWhitespaceTags_trimsThem() {
            // given
            UUID contentId = UUID.randomUUID();

            given(tagRepository.findByNameIn(anyCollection())).willReturn(List.of());
            given(tagRepository.saveAll(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            contentTagService.applyTags(contentId, List.of("  액션  ", " 코미디"));

            // then
            then(tagRepository).should().findByNameIn(tagNamesCaptor.capture());
            assertThat(tagNamesCaptor.getValue()).containsExactlyInAnyOrder("액션", "코미디");
        }
    }

    @Nested
    @DisplayName("deleteAllByContentId()")
    class DeleteAllByContentIdTest {

        @Test
        @DisplayName("Repository에 위임하여 삭제")
        void delegatesToRepository() {
            // given
            UUID contentId = UUID.randomUUID();

            // when
            contentTagService.deleteByContentId(contentId);

            // then
            then(contentTagRepository).should().deleteByContentId(contentId);
        }
    }
}
