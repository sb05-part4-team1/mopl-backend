package com.mopl.api.application.content;

import com.mopl.api.interfaces.api.content.ContentCreateRequest;
import com.mopl.domain.exception.content.InvalidContentDataException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.tag.TagService;
import com.mopl.storage.provider.FileStorageProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
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

    @Mock
    private FileStorageProvider fileStorageProvider;

    @InjectMocks
    private ContentFacade contentFacade;

    @Nested
    @DisplayName("upload()")
    class UploadTest {

        @Test
        @DisplayName("유효한 요청과 썸네일이 있으면 파일 업로드 후 콘텐츠 생성 성공")
        void withValidRequest_uploadSuccess() throws IOException {
            // given
            ContentCreateRequest request = new ContentCreateRequest(
                "영화", "인셉션", "꿈속의 꿈", List.of("SF", "액션")
            );
            MultipartFile thumbnail = mock(MultipartFile.class);
            InputStream inputStream = mock(InputStream.class);

            given(thumbnail.isEmpty()).willReturn(false);
            given(thumbnail.getOriginalFilename()).willReturn("inception.png");
            given(thumbnail.getInputStream()).willReturn(inputStream);

            String mockStoredPath = "contents/uuid_inception.png";
            String mockThumbnailUrl = "http://localhost:8080/api/v1/files/display?path=" + mockStoredPath;

            given(fileStorageProvider.upload(eq(inputStream), anyString())).willReturn(mockStoredPath);
            given(fileStorageProvider.getUrl(mockStoredPath)).willReturn(mockThumbnailUrl);

            Instant now = Instant.now();
            List<TagModel> tags = List.of(
                TagModel.builder().id(UUID.randomUUID()).name("SF").createdAt(now).build(),
                TagModel.builder().id(UUID.randomUUID()).name("액션").createdAt(now).build()
            );

            ContentModel savedModel = ContentModel.builder()
                .id(UUID.randomUUID())
                .type(request.type())
                .title(request.title())
                .description(request.description())
                .thumbnailUrl(mockThumbnailUrl)
                .tags(List.of("SF", "액션"))
                .createdAt(now)
                .updatedAt(now)
                .build();

            given(tagService.findOrCreateTags(anyList())).willReturn(tags);
            given(contentService.create(any(ContentModel.class), eq(tags))).willReturn(savedModel);

            // when
            ContentModel result = contentFacade.upload(request, thumbnail);

            // then
            assertThat(result.getThumbnailUrl()).isEqualTo(mockThumbnailUrl);
            assertThat(result.getTitle()).isEqualTo("인셉션");

            then(fileStorageProvider).should().upload(eq(inputStream), anyString());
            then(fileStorageProvider).should().getUrl(mockStoredPath);
            then(tagService).should().findOrCreateTags(request.tags());
            then(contentService).should().create(any(ContentModel.class), eq(tags));
        }

        @Test
        @DisplayName("파일 스트림 읽기 실패 시 RuntimeException 발생")
        void withInputStreamError_throwsException() throws IOException {
            // given
            ContentCreateRequest request = new ContentCreateRequest("영화", "인셉션", "꿈속의 꿈", List.of());
            MultipartFile thumbnail = mock(MultipartFile.class);

            given(thumbnail.isEmpty()).willReturn(false);
            given(thumbnail.getInputStream()).willThrow(new IOException("Stream error"));

            // when & then
            assertThatThrownBy(() -> contentFacade.upload(request, thumbnail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("파일 스트림 읽기 실패");
        }

        @Test
        @DisplayName("썸네일이 없으면 예외 발생")
        void withNullThumbnail_throwsException() {
            // given
            ContentCreateRequest request = new ContentCreateRequest("영화", "인셉션", "꿈속의 꿈", List.of());

            // when & then
            assertThatThrownBy(() -> contentFacade.upload(request, null))
                .isInstanceOf(InvalidContentDataException.class);
        }

        @Test
        @DisplayName("빈 썸네일 파일이면 예외 발생")
        void withEmptyThumbnail_throwsException() {
            // given
            ContentCreateRequest request = new ContentCreateRequest("영화", "인셉션", "꿈속의 꿈", List.of());
            MultipartFile emptyFile = mock(MultipartFile.class);
            given(emptyFile.isEmpty()).willReturn(true);

            // when & then
            assertThatThrownBy(() -> contentFacade.upload(request, emptyFile))
                .isInstanceOf(InvalidContentDataException.class);
        }
    }
}