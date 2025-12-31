package com.mopl.domain.service.tag;

import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.repository.tag.TagRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagService 단위 테스트")
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;
    @InjectMocks
    private TagService tagService;

    @Nested
    @DisplayName("findOrCreateTags()")
    class FindOrCreateTagsTest {

        @Test
        @DisplayName("존재하는 태그는 조회하고, 없는 태그는 생성용 객체를 만들어 saveAll로 일괄 저장한다")
        void withMixedTags_returnsAllTags() {
            // given
            String existingName = "SF";
            String newName = "액션";
            java.time.Instant now = java.time.Instant.now();

            TagModel existingTag = TagModel.builder()
                .id(UUID.randomUUID()).name(existingName).createdAt(now).build();

            given(tagRepository.findByName(existingName)).willReturn(Optional.of(existingTag));
            given(tagRepository.findByName(newName)).willReturn(Optional.empty());

            given(tagRepository.saveAll(anyList())).willAnswer(inv -> {
                List<TagModel> tags = inv.getArgument(0);
                return tags.stream().map(t -> t.getId() == null ? TagModel.builder().id(UUID
                    .randomUUID()).name(t.getName()).createdAt(now).build() : t
                ).toList();
            });

            // when
            List<TagModel> result = tagService.findOrCreateTags(List.of(existingName, newName));

            // then
            assertThat(result).hasSize(2);
            assertBaseFields(result);
            then(tagRepository).should().saveAll(anyList());
            then(tagRepository).should(times(0)).save(any());
        }

        @Test
        @DisplayName("이미 삭제된 태그가 있으면 복구(restore) 상태로 만들어 saveAll로 저장한다")
        void withDeletedTag_restoresExistingTag() {
            // given
            String tagName = "SF";
            TagModel deletedTag = TagModel.builder()
                .id(UUID.randomUUID()).name(tagName).createdAt(java.time.Instant.now())
                .deletedAt(java.time.Instant.now()).build();

            given(tagRepository.findByName(tagName)).willReturn(Optional.of(deletedTag));
            given(tagRepository.saveAll(anyList())).willAnswer(inv -> inv.getArgument(0));

            // when
            List<TagModel> result = tagService.findOrCreateTags(List.of(tagName));

            // then
            assertThat(result.get(0).getDeletedAt()).isNull();
            assertBaseFields(result);
            then(tagRepository).should().saveAll(anyList());
        }

        @Test
        @DisplayName("중복되거나 공백이 섞인 태그명도 정제하여 중복 없이 저장한다")
        void withDuplicateAndMessyNames_cleansAndDistincts() {
            // given
            List<String> input = List.of(" SF ", "SF", "  SF  ");
            TagModel sfTag = TagModel.builder()
                .id(UUID.randomUUID()).name("SF").createdAt(java.time.Instant.now()).build();

            given(tagRepository.findByName("SF")).willReturn(Optional.empty());
            given(tagRepository.saveAll(anyList())).willReturn(List.of(sfTag));

            // when
            List<TagModel> result = tagService.findOrCreateTags(input);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("SF");
            assertBaseFields(result);
            then(tagRepository).should(times(1)).findByName("SF");
            then(tagRepository).should().saveAll(anyList());
        }

        private void assertBaseFields(List<TagModel> tags) {
            assertThat(tags).allSatisfy(tag -> {
                assertThat(tag.getId()).isNotNull();
                assertThat(tag.getCreatedAt()).isNotNull();
                assertThat(tag.getDeletedAt()).isNull();
                assertThat(tag.isDeleted()).isFalse();
            });
        }
    }
}
