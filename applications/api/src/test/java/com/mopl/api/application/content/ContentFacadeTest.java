package com.mopl.api.application.content;

import com.mopl.api.interfaces.api.content.ContentCreateRequest;
import com.mopl.api.interfaces.api.content.ContentUpdateRequest;
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

            ContentCreateRequest request = new ContentCreateRequest(ContentModel.ContentType.movie,
                "인셉션", "꿈속의 꿈", tagNames);

            MultipartFile thumbnail = mock(MultipartFile.class);
            InputStream inputStream = mock(InputStream.class);

            given(thumbnail.isEmpty()).willReturn(false);
            given(thumbnail.getOriginalFilename()).willReturn("inception.png");
            given(thumbnail.getInputStream()).willReturn(inputStream);

            String storedPath = "contents/uuid_inception.png";
            String thumbnailUrl = "http://localhost/files/" + storedPath;

            given(fileStorageProvider.upload(eq(inputStream), anyString()))
                .willReturn(storedPath);
            given(fileStorageProvider.getUrl(storedPath))
                .willReturn(thumbnailUrl);

            ContentModel savedModel = ContentModel.builder()
                .id(UUID.randomUUID())
                .type(ContentModel.ContentType.movie)
                .title("인셉션")
                .description("꿈속의 꿈")
                .thumbnailUrl(thumbnailUrl)
                .tags(tagNames)
                .createdAt(Instant.now())
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
            // given
            ContentCreateRequest request = new ContentCreateRequest(ContentModel.ContentType.movie,
                "인셉션", "꿈속의 꿈", List
                    .of());

            // when & then
            assertThatThrownBy(() -> contentFacade.upload(request, null))
                .isInstanceOf(InvalidContentDataException.class);
        }

        @Test
        @DisplayName("파일 업로드 중 IOException 발생 시 RuntimeException")
        void withInputStreamError_throwsRuntimeException() throws IOException {
            // given
            ContentCreateRequest request = new ContentCreateRequest(ContentModel.ContentType.movie,
                "인셉션", "꿈속의 꿈", List
                    .of());

            MultipartFile thumbnail = mock(MultipartFile.class);
            given(thumbnail.isEmpty()).willReturn(false);
            given(thumbnail.getInputStream()).willThrow(new IOException("IO error"));

            // when & then
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
            // given
            UUID contentId = UUID.randomUUID();

            ContentModel model = ContentModel.builder()
                .id(contentId)
                .title("인셉션")
                .tags(List.of("SF", "액션"))
                .build();

            given(contentService.getById(contentId)).willReturn(model);

            // when
            ContentModel result = contentFacade.getDetail(contentId);

            // then
            assertThat(result.getId()).isEqualTo(contentId);
            assertThat(result.getTags()).containsExactly("SF", "액션");

            then(contentService).should().getById(contentId);
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTest {

        @Test
        @DisplayName("썸네일 포함 수정 시 파일 업로드 후 콘텐츠 수정 성공")
        void update_withThumbnail_uploadsAndUpdates() throws IOException {
            // given
            UUID contentId = UUID.randomUUID();
            List<String> tagNames = List.of("SF");

            ContentUpdateRequest request = new ContentUpdateRequest("새 제목", "새 설명", tagNames);

            MultipartFile thumbnail = mock(MultipartFile.class);
            InputStream inputStream = mock(InputStream.class);

            given(thumbnail.isEmpty()).willReturn(false);
            given(thumbnail.getOriginalFilename()).willReturn("new.png");
            given(thumbnail.getInputStream()).willReturn(inputStream);

            String storedPath = "contents/uuid_new.png";
            String thumbnailUrl = "http://localhost/files/" + storedPath;

            given(fileStorageProvider.upload(eq(inputStream), anyString()))
                .willReturn(storedPath);
            given(fileStorageProvider.getUrl(storedPath))
                .willReturn(thumbnailUrl);

            ContentModel updatedModel = ContentModel.builder()
                .id(contentId)
                .title("새 제목")
                .description("새 설명")
                .thumbnailUrl(thumbnailUrl)
                .tags(tagNames)
                .build();

            given(contentService.update(
                eq(contentId),
                eq("새 제목"),
                eq("새 설명"),
                eq(thumbnailUrl),
                eq(tagNames)
            )).willReturn(updatedModel);

            // when
            ContentModel result = contentFacade.update(contentId, request, thumbnail);

            // then
            assertThat(result.getTitle()).isEqualTo("새 제목");
            assertThat(result.getThumbnailUrl()).isEqualTo(thumbnailUrl);
            assertThat(result.getTags()).containsExactly("SF");

            then(contentService).should().update(
                eq(contentId),
                eq("새 제목"),
                eq("새 설명"),
                eq(thumbnailUrl),
                eq(tagNames)
            );
        }

        @Test
        @DisplayName("썸네일 없이 수정 시 업로드 없이 콘텐츠 수정")
        void update_withoutThumbnail_updatesOnly() {
            // given
            UUID contentId = UUID.randomUUID();
            List<String> tagNames = List.of("액션");

            ContentUpdateRequest request = new ContentUpdateRequest("제목", "설명", tagNames);

            ContentModel updatedModel = ContentModel.builder()
                .id(contentId)
                .title("제목")
                .description("설명")
                .tags(tagNames)
                .build();

            given(contentService.update(
                eq(contentId),
                eq("제목"),
                eq("설명"),
                eq(null),
                eq(tagNames)
            )).willReturn(updatedModel);

            // when
            ContentModel result = contentFacade.update(contentId, request, null);

            // then
            assertThat(result.getTitle()).isEqualTo("제목");
            assertThat(result.getTags()).containsExactly("액션");

            then(fileStorageProvider).shouldHaveNoInteractions();
            then(contentService).should().update(
                eq(contentId),
                eq("제목"),
                eq("설명"),
                eq(null),
                eq(tagNames)
            );
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTest {

        @Test
        @DisplayName("콘텐츠 삭제 요청 시 서비스 delete 호출")
        void delete_callsServiceDelete() {
            // given
            UUID contentId = UUID.randomUUID();

            // when
            contentFacade.delete(contentId);

            // then
            then(contentService).should().delete(contentId);
        }
    }
}
