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
        @DisplayName("존재하는 태그는 조회하고, 없는 태그는 생성한다")
        void withMixedTags_returnsAllTags() {
            // given
            String existingName = "SF";
            String newName = "액션";

            TagModel existingTag = TagModel.builder().id(UUID.randomUUID()).name(existingName)
                .build();
            TagModel newTag = TagModel.builder().id(UUID.randomUUID()).name(newName).build();

            // 기존 태그 조회 결과 설정
            given(tagRepository.findByName(existingName)).willReturn(Optional.of(existingTag));
            // 신규 태그 조회 결과(empty) 및 저장 결과 설정
            given(tagRepository.findByName(newName)).willReturn(Optional.empty());
            given(tagRepository.save(any(TagModel.class))).willReturn(newTag);

            // when
            List<TagModel> result = tagService.findOrCreateTags(List.of(existingName, newName));

            // then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(existingTag, newTag);

            // 검증: 각 태그이름으로 조회가 일어났는지 확인
            then(tagRepository).should().findByName(existingName);
            then(tagRepository).should().findByName(newName);
            // 검증: 존재하지 않는 태그 하나에 대해서만 저장이 호출되었는지 확인
            then(tagRepository).should(times(1)).save(any(TagModel.class));
        }
    }
}
