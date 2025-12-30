package com.mopl.api.application.content;

import com.mopl.api.interfaces.api.content.ContentCreateRequest;
import com.mopl.domain.exception.content.InvalidContentDataException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.tag.TagService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContentFacade 단위 테스트")
class ContentFacadeTest {

    @Mock
    private ContentService contentService;

    @Mock
    private TagService tagService;

    @InjectMocks
    private ContentFacade contentFacade;

    @Nested
    @DisplayName("upload()")
    class UploadTest {

        @Test
        @DisplayName("유효한 요청과 썸네일이 있으면 콘텐츠 생성 성공")
        void withValidRequest_uploadSuccess() {
            // given
            ContentCreateRequest request = new ContentCreateRequest(
                "영화", "인셉션", "꿈속의 꿈", List.of("SF", "액션")
            );
            MultipartFile thumbnail = mock(MultipartFile.class);
            given(thumbnail.isEmpty()).willReturn(false);
            given(thumbnail.getOriginalFilename()).willReturn("inception.png");

            Instant now = Instant.now();

            List<TagModel> tags = List.of(
                TagModel.builder().id(UUID.randomUUID()).name("SF").createdAt(now).build(),
                TagModel.builder().id(UUID.randomUUID()).name("액션").createdAt(now).build()
            );

            UUID id = UUID.randomUUID();

            ContentModel savedModel = ContentModel.builder()
                .id(id)
                .type("영화")
                .title("인셉션")
                .description("꿈속의 꿈")
                .thumbnailUrl("https://mopl.com/inception.png")
                .tags(tags.stream().map(TagModel::getName).toList())
                .createdAt(now)
                .updatedAt(now)
                .deletedAt(null)
                .build();

            given(tagService.findOrCreateTags(anyList())).willReturn(tags);
            given(contentService.create(any(ContentModel.class), eq(tags))).willReturn(savedModel);

            // when
            ContentModel result = contentFacade.upload(request, thumbnail);

            // then
            assertThat(result.getId()).isEqualTo(savedModel.getId());
            assertThat(result.getType()).isEqualTo("영화");
            assertThat(result.getTitle()).isEqualTo("인셉션");
            assertThat(result.getDescription()).isEqualTo("꿈속의 꿈");
            assertThat(result.getThumbnailUrl()).isEqualTo("https://mopl.com/inception.png");
            assertThat(result.getTags()).containsExactly("SF", "액션");
            assertThat(result.getCreatedAt()).isEqualTo(now);
            assertThat(result.getUpdatedAt()).isNotNull();
            assertThat(result.getDeletedAt()).isNull();

            then(tagService).should().findOrCreateTags(request.tags());
            then(contentService).should().create(any(ContentModel.class), eq(tags));
        }

        @Test
        @DisplayName("썸네일이 없으면 예외 발생")
        void withNullThumbnail_throwsException() {
            // given
            ContentCreateRequest request = new ContentCreateRequest("영화", "인셉션", "꿈속의 꿈", List
                .of());

            // when & then
            assertThatThrownBy(() -> contentFacade.upload(request, null))
                .isInstanceOf(InvalidContentDataException.class)
                .satisfies(e -> {
                    InvalidContentDataException ex = (InvalidContentDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("썸네일 파일은 필수입니다.");
                });
        }

        @Test
        @DisplayName("빈 썸네일 파일이면 예외 발생")
        void withEmptyThumbnail_throwsException() {
            // given
            ContentCreateRequest request = new ContentCreateRequest("영화", "인셉션", "꿈속의 꿈", List
                .of());
            MultipartFile emptyFile = mock(MultipartFile.class);
            given(emptyFile.isEmpty()).willReturn(true);

            // when & then
            assertThatThrownBy(() -> contentFacade.upload(request, emptyFile))
                .isInstanceOf(InvalidContentDataException.class)
                .satisfies(e -> {
                    InvalidContentDataException ex = (InvalidContentDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("썸네일 파일은 필수입니다.");
                });
        }
    }
}
