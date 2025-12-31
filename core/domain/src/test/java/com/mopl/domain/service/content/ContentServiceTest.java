package com.mopl.domain.service.content;

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

import static org.assertj.core.api.Assertions.assertThat;
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
        @DisplayName("콘텐츠를 저장하고 태그가 있으면 태그 저장을 위임한다")
        void withModelAndTags_returnsSavedModel() {
            // given
            ContentModel contentModel = ContentModel.builder().title("인셉션").build();

            List<TagModel> tags = List.of(TagModel.builder().name("SF").build());

            given(contentRepository.save(contentModel))
                .willReturn(contentModel);

            // when
            ContentModel result = contentService.create(contentModel, tags);

            // then
            assertThat(result).isEqualTo(contentModel);

            then(contentRepository).should().save(contentModel);
            then(contentTagRepository).should()
                .saveAll(contentModel.getId(), tags);
        }

        @Test
        @DisplayName("태그가 없으면 태그 저장을 호출하지 않는다")
        void withoutTags_onlySaveContent() {
            // given
            ContentModel contentModel = ContentModel.builder().title("인셉션").build();

            given(contentRepository.save(contentModel))
                .willReturn(contentModel);

            // when
            ContentModel result = contentService.create(contentModel, List.of());

            // then
            assertThat(result).isEqualTo(contentModel);

            then(contentRepository).should().save(contentModel);
            then(contentTagRepository).shouldHaveNoInteractions();
        }
    }
}
