package com.mopl.api.application.content;

import com.mopl.api.interfaces.api.content.ContentCreateRequest;
import com.mopl.domain.exception.content.InvalidContentDataException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.service.content.ContentService;
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
            List<String> tagNames = List.of("SF", "액션");

            ContentCreateRequest request = new ContentCreateRequest(
                "영화", "인셉션", "꿈속의 꿈", tagNames
            );

            MultipartFile thumbnail = mock(MultipartFile.class);
            InputStream inputStream = mock(InputStream.class);

            given(thumbnail.isEmpty()).willReturn(false);
            given(thumbnail.getOriginalFilename()).willReturn("inception.png");
            given(thumbnail.getInputStream()).willReturn(inputStream);

            String storedPath = "contents/uuid_inception.png";
            String thumbnailUrl = "http://localhost:8080/files/" + storedPath;

            given(fileStorageProvider.upload(eq(inputStream), anyString()))
                .willReturn(storedPath);
            given(fileStorageProvider.getUrl(storedPath))
                .willReturn(thumbnailUrl);

            Instant now = Instant.now();

            ContentModel savedModel = ContentModel.builder()
                .id(UUID.randomUUID())
                .type("영화")
                .title("인셉션")
                .description("꿈속의 꿈")
                .thumbnailUrl(thumbnailUrl)
                .tags(tagNames)
                .createdAt(now)
                .build();

            given(contentService.create(any(ContentModel.class), eq(tagNames)))
                .willReturn(savedModel);

            // when
            ContentModel result = contentFacade.upload(request, thumbnail);

            // then
            assertThat(result.getTitle()).isEqualTo("인셉션");
            assertThat(result.getThumbnailUrl()).isEqualTo(thumbnailUrl);
            assertThat(result.getTags()).containsExactly("SF", "액션");

            then(fileStorageProvider).should().upload(eq(inputStream), anyString());
            then(contentService).should().create(any(ContentModel.class), eq(tagNames));
        }

        @Test
        @DisplayName("썸네일이 null이면 예외 발생")
        void withNullThumbnail_throwsException() {
            ContentCreateRequest request = new ContentCreateRequest("영화", "인셉션", "꿈속의 꿈", List
                .of());

            assertThatThrownBy(() -> contentFacade.upload(request, null))
                .isInstanceOf(InvalidContentDataException.class)
                .satisfies(e -> {
                    InvalidContentDataException ex = (InvalidContentDataException) e;
                    assertThat(ex.getDetails().get("detailMessage"))
                        .isEqualTo("썸네일 파일은 필수입니다.");
                });
        }

        @Test
        @DisplayName("썸네일 InputStream 처리 중 IOException 발생 시 RuntimeException")
        void withInputStreamError_throwsRuntimeException() throws IOException {
            ContentCreateRequest request = new ContentCreateRequest("영화", "인셉션", "꿈속의 꿈", List
                .of());

            MultipartFile thumbnail = mock(MultipartFile.class);
            given(thumbnail.isEmpty()).willReturn(false);
            given(thumbnail.getInputStream()).willThrow(new IOException("IO error"));

            assertThatThrownBy(() -> contentFacade.upload(request, thumbnail))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("파일 저장 중 오류");
        }
    }

    @Nested
    @DisplayName("getDetail()")
    class GetDetailTest {

        @Test
        @DisplayName("콘텐츠 ID로 상세 조회 성공")
        void withContentId_returnsContent() {
            UUID contentId = UUID.randomUUID();

            ContentModel model = ContentModel.builder()
                .id(contentId)
                .title("인셉션")
                .tags(List.of("SF", "액션"))
                .build();

            given(contentService.getById(contentId)).willReturn(model);

            ContentModel result = contentFacade.getDetail(contentId);

            assertThat(result.getId()).isEqualTo(contentId);
            assertThat(result.getTags()).containsExactly("SF", "액션");

            then(contentService).should().getById(contentId);
        }
    }
}
