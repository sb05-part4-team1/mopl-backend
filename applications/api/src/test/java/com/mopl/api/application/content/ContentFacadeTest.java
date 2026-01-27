package com.mopl.api.application.content;

import com.mopl.api.interfaces.api.content.dto.ContentCreateRequest;
import com.mopl.api.interfaces.api.content.dto.ContentUpdateRequest;
import com.mopl.domain.exception.content.InvalidContentDataException;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.content.ContentModel.ContentType;
import com.mopl.domain.model.tag.TagModel;
import com.mopl.domain.repository.content.query.ContentQueryRequest;
import com.mopl.domain.service.content.ContentService;
import com.mopl.domain.service.content.ContentTagService;
import com.mopl.domain.service.watchingsession.WatchingSessionService;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.mopl.domain.support.search.ContentSearchSyncPort;
import com.mopl.domain.support.transaction.AfterCommitExecutor;
import com.mopl.dto.content.ContentResponse;
import com.mopl.dto.content.ContentResponseMapper;
import com.mopl.storage.provider.StorageProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContentFacade 단위 테스트")
class ContentFacadeTest {

    @Mock
    private ContentService contentService;

    @Mock
    private ContentTagService contentTagService;

    @Mock
    private WatchingSessionService watchingSessionService;

    @Mock
    private StorageProvider storageProvider;

    @Mock
    private ContentResponseMapper contentResponseMapper;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    @SuppressWarnings("unused")
    private ContentSearchSyncPort contentSearchSyncPort;

    @Mock
    private AfterCommitExecutor afterCommitExecutor;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private ContentFacade contentFacade;

    @BeforeEach
    void setUp() {
        reset(multipartFile);
    }

    private void setupAfterCommitExecutor() {
        willAnswer(invocation -> {
            invocation.<Runnable>getArgument(0).run();
            return null;
        }).given(afterCommitExecutor).execute(any());
    }

    private static final String DEFAULT_TITLE = "시빌엄";
    private static final String DEFAULT_DESCRIPTION = "진짜 좋다.";
    private static final String DEFAULT_THUMBNAIL_PATH = "contents/thumbnail.jpg";
    private static final String DEFAULT_THUMBNAIL_URL = "https://cdn.example.com/contents/thumbnail.jpg";

    private static ContentModel createContentModelWithId(UUID id) {
        return ContentModel.builder()
            .id(id)
            .type(ContentType.movie)
            .title(DEFAULT_TITLE)
            .description(DEFAULT_DESCRIPTION)
            .thumbnailPath(DEFAULT_THUMBNAIL_PATH)
            .reviewCount(0)
            .averageRating(0.0)
            .build();
    }

    private static ContentResponse createContentResponse(UUID id, List<String> tags, long watcherCount) {
        return new ContentResponse(
            id,
            ContentType.movie,
            DEFAULT_TITLE,
            DEFAULT_DESCRIPTION,
            DEFAULT_THUMBNAIL_URL,
            tags,
            0.0,
            0,
            watcherCount
        );
    }

    @SuppressWarnings("unchecked")
    private void setupTransactionTemplate() {
        given(transactionTemplate.execute(any(TransactionCallback.class))).willAnswer(invocation -> {
            TransactionCallback<ContentResponse> callback = invocation.getArgument(0);
            return callback.doInTransaction(mock());
        });
    }

    @Nested
    @DisplayName("getContents()")
    class GetContentsTest {

        @Test
        @DisplayName("콘텐츠 목록이 비어있으면 빈 응답 반환")
        void withEmptyContents_returnsEmptyResponse() {
            // given
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null, null, null, null
            );
            CursorResponse<ContentModel> emptyResponse = CursorResponse.empty(
                "watcherCount", SortDirection.DESCENDING
            );

            given(contentService.getAll(request)).willReturn(emptyResponse);

            // when
            CursorResponse<ContentResponse> result = contentFacade.getContents(request);

            // then
            assertThat(result.data()).isEmpty();
            assertThat(result.hasNext()).isFalse();
            then(contentTagService).should(never()).getTagsByContentIdIn(anyList());
            then(watchingSessionService).should(never()).countByContentIdIn(anyList());
        }

        @Test
        @DisplayName("콘텐츠 목록이 있으면 태그와 시청자 수를 포함한 응답 반환")
        void withContents_returnsResponseWithTagsAndWatcherCount() {
            // given
            UUID contentId = UUID.randomUUID();
            ContentModel contentModel = createContentModelWithId(contentId);
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null, null, null, null
            );

            CursorResponse<ContentModel> contentResponse = CursorResponse.of(
                List.of(contentModel),
                null,
                null,
                false,
                1L,
                "watcherCount",
                SortDirection.DESCENDING
            );

            TagModel tagModel = TagModel.create("SF");
            Map<UUID, List<TagModel>> tagsByContentId = Map.of(contentId, List.of(tagModel));
            Map<UUID, Long> watcherCountByContentId = Map.of(contentId, 100L);
            ContentResponse expectedResponse = createContentResponse(contentId, List.of("SF"), 100L);

            given(contentService.getAll(request)).willReturn(contentResponse);
            given(contentTagService.getTagsByContentIdIn(List.of(contentId))).willReturn(tagsByContentId);
            given(watchingSessionService.countByContentIdIn(List.of(contentId))).willReturn(watcherCountByContentId);
            given(contentResponseMapper.toResponse(
                eq(contentModel),
                eq(List.of("SF")),
                eq(100L)
            )).willReturn(expectedResponse);

            // when
            CursorResponse<ContentResponse> result = contentFacade.getContents(request);

            // then
            assertThat(result.data()).hasSize(1);
            assertThat(result.data().getFirst()).isEqualTo(expectedResponse);
            then(contentTagService).should().getTagsByContentIdIn(List.of(contentId));
            then(watchingSessionService).should().countByContentIdIn(List.of(contentId));
        }

        @Test
        @DisplayName("태그가 없는 콘텐츠는 빈 태그 목록 반환")
        void withContentWithoutTags_returnsResponseWithEmptyTags() {
            // given
            UUID contentId = UUID.randomUUID();
            ContentModel contentModel = createContentModelWithId(contentId);
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null, null, null, null
            );

            CursorResponse<ContentModel> contentResponse = CursorResponse.of(
                List.of(contentModel),
                null,
                null,
                false,
                1L,
                "watcherCount",
                SortDirection.DESCENDING
            );

            Map<UUID, List<TagModel>> emptyTagsByContentId = Map.of();
            Map<UUID, Long> watcherCountByContentId = Map.of(contentId, 50L);
            ContentResponse expectedResponse = createContentResponse(contentId, List.of(), 50L);

            given(contentService.getAll(request)).willReturn(contentResponse);
            given(contentTagService.getTagsByContentIdIn(List.of(contentId))).willReturn(emptyTagsByContentId);
            given(watchingSessionService.countByContentIdIn(List.of(contentId))).willReturn(watcherCountByContentId);
            given(contentResponseMapper.toResponse(
                eq(contentModel),
                eq(List.of()),
                eq(50L)
            )).willReturn(expectedResponse);

            // when
            CursorResponse<ContentResponse> result = contentFacade.getContents(request);

            // then
            assertThat(result.data()).hasSize(1);
            assertThat(result.data().getFirst().tags()).isEmpty();
        }

        @Test
        @DisplayName("시청자 수가 없는 콘텐츠는 0 반환")
        void withContentWithoutWatcherCount_returnsZeroWatcherCount() {
            // given
            UUID contentId = UUID.randomUUID();
            ContentModel contentModel = createContentModelWithId(contentId);
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null, null, null, null
            );

            CursorResponse<ContentModel> contentResponse = CursorResponse.of(
                List.of(contentModel),
                null,
                null,
                false,
                1L,
                "watcherCount",
                SortDirection.DESCENDING
            );

            Map<UUID, List<TagModel>> tagsByContentId = Map.of();
            Map<UUID, Long> emptyWatcherCountByContentId = Map.of();
            ContentResponse expectedResponse = createContentResponse(contentId, List.of(), 0L);

            given(contentService.getAll(request)).willReturn(contentResponse);
            given(contentTagService.getTagsByContentIdIn(List.of(contentId))).willReturn(tagsByContentId);
            given(watchingSessionService.countByContentIdIn(List.of(contentId))).willReturn(emptyWatcherCountByContentId);
            given(contentResponseMapper.toResponse(
                eq(contentModel),
                eq(List.of()),
                eq(0L)
            )).willReturn(expectedResponse);

            // when
            CursorResponse<ContentResponse> result = contentFacade.getContents(request);

            // then
            assertThat(result.data()).hasSize(1);
            assertThat(result.data().getFirst().watcherCount()).isZero();
        }

        @Test
        @DisplayName("여러 태그가 있는 콘텐츠는 모든 태그명 반환")
        void withContentWithMultipleTags_returnsAllTagNames() {
            // given
            UUID contentId = UUID.randomUUID();
            ContentModel contentModel = createContentModelWithId(contentId);
            ContentQueryRequest request = new ContentQueryRequest(
                null, null, null, null, null, null, null, null
            );

            CursorResponse<ContentModel> contentResponse = CursorResponse.of(
                List.of(contentModel),
                null,
                null,
                false,
                1L,
                "watcherCount",
                SortDirection.DESCENDING
            );

            TagModel tag1 = TagModel.create("SF");
            TagModel tag2 = TagModel.create("Action");
            TagModel tag3 = TagModel.create("Thriller");
            Map<UUID, List<TagModel>> tagsByContentId = Map.of(contentId, List.of(tag1, tag2, tag3));
            Map<UUID, Long> watcherCountByContentId = Map.of(contentId, 200L);
            ContentResponse expectedResponse = createContentResponse(
                contentId, List.of("SF", "Action", "Thriller"), 200L
            );

            given(contentService.getAll(request)).willReturn(contentResponse);
            given(contentTagService.getTagsByContentIdIn(List.of(contentId))).willReturn(tagsByContentId);
            given(watchingSessionService.countByContentIdIn(List.of(contentId))).willReturn(watcherCountByContentId);
            given(contentResponseMapper.toResponse(
                eq(contentModel),
                eq(List.of("SF", "Action", "Thriller")),
                eq(200L)
            )).willReturn(expectedResponse);

            // when
            CursorResponse<ContentResponse> result = contentFacade.getContents(request);

            // then
            assertThat(result.data()).hasSize(1);
            assertThat(result.data().getFirst().tags()).containsExactlyInAnyOrder("SF", "Action", "Thriller");
        }
    }

    @Nested
    @DisplayName("getContent()")
    class GetContentTest {

        @Test
        @DisplayName("존재하는 콘텐츠 ID로 조회하면 ContentResponse 반환")
        void withExistingContentId_returnsContentResponse() {
            // given
            UUID contentId = UUID.randomUUID();
            ContentModel contentModel = createContentModelWithId(contentId);
            TagModel tagModel = TagModel.create("Action");
            ContentResponse expectedResponse = createContentResponse(contentId, List.of("Action"), 200L);

            given(contentService.getById(contentId)).willReturn(contentModel);
            given(contentTagService.getTagsByContentId(contentId)).willReturn(List.of(tagModel));
            given(watchingSessionService.countByContentId(contentId)).willReturn(200L);
            given(contentResponseMapper.toResponse(
                eq(contentModel),
                eq(List.of("Action")),
                eq(200L)
            )).willReturn(expectedResponse);

            // when
            ContentResponse result = contentFacade.getContent(contentId);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(contentService).should().getById(contentId);
            then(contentTagService).should().getTagsByContentId(contentId);
            then(watchingSessionService).should().countByContentId(contentId);
        }
    }

    @Nested
    @DisplayName("upload()")
    class UploadTest {

        @Test
        @DisplayName("썸네일이 null이면 예외 발생")
        void withNullThumbnail_throwsException() {
            // given
            ContentCreateRequest request = new ContentCreateRequest(
                ContentType.movie,
                DEFAULT_TITLE,
                DEFAULT_DESCRIPTION,
                List.of("SF")
            );

            // when & then
            assertThatThrownBy(() -> contentFacade.upload(request, null))
                .isInstanceOf(InvalidContentDataException.class);

            then(storageProvider).should(never()).upload(any(), anyLong(), anyString());
            then(transactionTemplate).should(never()).execute(any());
        }

        @Test
        @DisplayName("썸네일이 비어있으면 예외 발생")
        void withEmptyThumbnail_throwsException() {
            // given
            ContentCreateRequest request = new ContentCreateRequest(
                ContentType.movie,
                DEFAULT_TITLE,
                DEFAULT_DESCRIPTION,
                List.of("SF")
            );

            given(multipartFile.isEmpty()).willReturn(true);

            // when & then
            assertThatThrownBy(() -> contentFacade.upload(request, multipartFile))
                .isInstanceOf(InvalidContentDataException.class);

            then(storageProvider).should(never()).upload(any(), anyLong(), anyString());
            then(transactionTemplate).should(never()).execute(any());
        }

        @Test
        @DisplayName("유효한 요청이면 콘텐츠 생성 후 응답 반환")
        void withValidRequest_createsContentAndReturnsResponse() throws IOException {
            // given
            UUID contentId = UUID.randomUUID();
            List<String> tags = List.of("SF", "Action");
            ContentCreateRequest request = new ContentCreateRequest(
                ContentType.movie,
                DEFAULT_TITLE,
                DEFAULT_DESCRIPTION,
                tags
            );

            ContentModel savedContent = createContentModelWithId(contentId);
            ContentResponse expectedResponse = createContentResponse(contentId, tags, 0L);

            given(multipartFile.isEmpty()).willReturn(false);
            given(multipartFile.getInputStream()).willReturn(new ByteArrayInputStream(new byte[0]));
            given(multipartFile.getSize()).willReturn(1024L);
            given(multipartFile.getOriginalFilename()).willReturn("thumbnail.jpg");

            given(contentService.create(any(ContentModel.class))).willReturn(savedContent);
            given(contentTagService.getTagsByContentId(contentId)).willReturn(List.of());
            given(watchingSessionService.countByContentId(contentId)).willReturn(0L);
            given(contentResponseMapper.toResponse(any(), anyList(), anyLong())).willReturn(expectedResponse);

            setupTransactionTemplate();
            setupAfterCommitExecutor();

            // when
            ContentResponse result = contentFacade.upload(request, multipartFile);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(storageProvider).should().upload(any(), eq(1024L), anyString());
            then(contentService).should().create(any(ContentModel.class));
            then(contentTagService).should().applyTags(contentId, tags);
            then(contentSearchSyncPort).should().upsert(any(ContentModel.class));
        }

        @Test
        @DisplayName("파일 업로드 중 IOException 발생 시 UncheckedIOException 발생")
        void withIOException_throwsUncheckedIOException() throws IOException {
            // given
            ContentCreateRequest request = new ContentCreateRequest(
                ContentType.movie,
                DEFAULT_TITLE,
                DEFAULT_DESCRIPTION,
                List.of("SF")
            );

            given(multipartFile.isEmpty()).willReturn(false);
            given(multipartFile.getInputStream()).willThrow(new IOException("File read error"));

            // when & then
            assertThatThrownBy(() -> contentFacade.upload(request, multipartFile))
                .isInstanceOf(UncheckedIOException.class)
                .hasCauseInstanceOf(IOException.class);

            then(transactionTemplate).should(never()).execute(any());
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTest {

        @Test
        @DisplayName("썸네일 없이 업데이트하면 기존 썸네일 유지")
        void withoutThumbnail_keepsExistingThumbnail() {
            // given
            UUID contentId = UUID.randomUUID();
            ContentModel existingContent = createContentModelWithId(contentId);
            ContentUpdateRequest request = new ContentUpdateRequest(
                "Updated Title",
                "Updated Description",
                null
            );

            ContentResponse expectedResponse = new ContentResponse(
                contentId,
                ContentType.movie,
                "Updated Title",
                "Updated Description",
                DEFAULT_THUMBNAIL_URL,
                List.of(),
                0.0,
                0,
                0L
            );

            given(contentService.getById(contentId)).willReturn(existingContent);
            given(contentService.update(any(ContentModel.class))).willReturn(existingContent);
            given(contentTagService.getTagsByContentId(contentId)).willReturn(List.of());
            given(watchingSessionService.countByContentId(contentId)).willReturn(0L);
            given(contentResponseMapper.toResponse(any(), anyList(), anyLong())).willReturn(expectedResponse);

            setupTransactionTemplate();

            // when
            ContentResponse result = contentFacade.update(contentId, request, null);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(storageProvider).should(never()).upload(any(), anyLong(), anyString());
            then(contentTagService).should(never()).deleteByContentId(any());
            then(contentTagService).should(never()).applyTags(any(), any());
        }

        @Test
        @DisplayName("빈 썸네일로 업데이트하면 기존 썸네일 유지")
        void withEmptyThumbnail_keepsExistingThumbnail() {
            // given
            UUID contentId = UUID.randomUUID();
            ContentModel existingContent = createContentModelWithId(contentId);
            ContentUpdateRequest request = new ContentUpdateRequest(
                "Updated Title",
                null,
                null
            );

            ContentResponse expectedResponse = createContentResponse(contentId, List.of(), 0L);

            given(contentService.getById(contentId)).willReturn(existingContent);
            given(multipartFile.isEmpty()).willReturn(true);
            given(contentService.update(any(ContentModel.class))).willReturn(existingContent);
            given(contentTagService.getTagsByContentId(contentId)).willReturn(List.of());
            given(watchingSessionService.countByContentId(contentId)).willReturn(0L);
            given(contentResponseMapper.toResponse(any(), anyList(), anyLong())).willReturn(expectedResponse);

            setupTransactionTemplate();

            // when
            ContentResponse result = contentFacade.update(contentId, request, multipartFile);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(storageProvider).should(never()).upload(any(), anyLong(), anyString());
        }

        @Test
        @DisplayName("새 썸네일과 함께 업데이트")
        void withNewThumbnail_updatesWithNewThumbnail() throws IOException {
            // given
            UUID contentId = UUID.randomUUID();
            ContentModel existingContent = createContentModelWithId(contentId);
            List<String> newTags = List.of("Drama");
            ContentUpdateRequest request = new ContentUpdateRequest(
                "Updated Title",
                "Updated Description",
                newTags
            );

            ContentResponse expectedResponse = new ContentResponse(
                contentId,
                ContentType.movie,
                "Updated Title",
                "Updated Description",
                "https://cdn.example.com/new-thumbnail.jpg",
                newTags,
                0.0,
                0,
                0L
            );

            given(contentService.getById(contentId)).willReturn(existingContent);
            given(multipartFile.isEmpty()).willReturn(false);
            given(multipartFile.getInputStream()).willReturn(new ByteArrayInputStream(new byte[0]));
            given(multipartFile.getSize()).willReturn(2048L);
            given(multipartFile.getOriginalFilename()).willReturn("new-thumbnail.jpg");
            given(contentService.update(any(ContentModel.class))).willReturn(existingContent);
            given(contentTagService.getTagsByContentId(contentId)).willReturn(List.of());
            given(watchingSessionService.countByContentId(contentId)).willReturn(0L);
            given(contentResponseMapper.toResponse(any(), anyList(), anyLong())).willReturn(expectedResponse);

            setupTransactionTemplate();
            setupAfterCommitExecutor();

            // when
            ContentResponse result = contentFacade.update(contentId, request, multipartFile);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(storageProvider).should().upload(any(), eq(2048L), anyString());
            then(contentTagService).should().deleteByContentId(contentId);
            then(contentTagService).should().applyTags(contentId, newTags);
            then(contentSearchSyncPort).should().upsert(any(ContentModel.class));
        }

        @Test
        @DisplayName("태그가 null이면 태그 업데이트 하지 않음")
        void withNullTags_doesNotUpdateTags() {
            // given
            UUID contentId = UUID.randomUUID();
            ContentModel existingContent = createContentModelWithId(contentId);
            ContentUpdateRequest request = new ContentUpdateRequest(
                "Updated Title",
                null,
                null
            );

            TagModel existingTag = TagModel.create("SF");
            ContentResponse expectedResponse = createContentResponse(contentId, List.of("SF"), 0L);

            given(contentService.getById(contentId)).willReturn(existingContent);
            given(contentService.update(any(ContentModel.class))).willReturn(existingContent);
            given(contentTagService.getTagsByContentId(contentId)).willReturn(List.of(existingTag));
            given(watchingSessionService.countByContentId(contentId)).willReturn(0L);
            given(contentResponseMapper.toResponse(any(), eq(List.of("SF")), anyLong()))
                .willReturn(expectedResponse);

            setupTransactionTemplate();

            // when
            ContentResponse result = contentFacade.update(contentId, request, null);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(contentTagService).should(never()).deleteByContentId(any());
            then(contentTagService).should(never()).applyTags(any(), any());
        }

        @Test
        @DisplayName("태그가 있으면 기존 태그 삭제 후 새 태그 적용")
        void withTags_deletesAndAppliesNewTags() {
            // given
            UUID contentId = UUID.randomUUID();
            ContentModel existingContent = createContentModelWithId(contentId);
            List<String> newTags = List.of("Drama", "Romance");
            ContentUpdateRequest request = new ContentUpdateRequest(
                null,
                null,
                newTags
            );

            TagModel newTag1 = TagModel.create("Drama");
            TagModel newTag2 = TagModel.create("Romance");
            ContentResponse expectedResponse = createContentResponse(contentId, newTags, 0L);

            given(contentService.getById(contentId)).willReturn(existingContent);
            given(contentService.update(any(ContentModel.class))).willReturn(existingContent);
            given(contentTagService.getTagsByContentId(contentId)).willReturn(List.of(newTag1, newTag2));
            given(watchingSessionService.countByContentId(contentId)).willReturn(0L);
            given(contentResponseMapper.toResponse(any(), eq(List.of("Drama", "Romance")), anyLong()))
                .willReturn(expectedResponse);

            setupTransactionTemplate();

            // when
            ContentResponse result = contentFacade.update(contentId, request, null);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            then(contentTagService).should().deleteByContentId(contentId);
            then(contentTagService).should().applyTags(contentId, newTags);
        }

        @Test
        @DisplayName("파일 업로드 중 IOException 발생 시 UncheckedIOException 발생")
        void withIOException_throwsUncheckedIOException() throws IOException {
            // given
            UUID contentId = UUID.randomUUID();
            ContentModel existingContent = createContentModelWithId(contentId);
            ContentUpdateRequest request = new ContentUpdateRequest(
                "Updated Title",
                null,
                null
            );

            given(contentService.getById(contentId)).willReturn(existingContent);
            given(multipartFile.isEmpty()).willReturn(false);
            given(multipartFile.getInputStream()).willThrow(new IOException("File read error"));

            // when & then
            assertThatThrownBy(() -> contentFacade.update(contentId, request, multipartFile))
                .isInstanceOf(UncheckedIOException.class)
                .hasCauseInstanceOf(IOException.class);

            then(transactionTemplate).should(never()).execute(any());
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTest {

        @Test
        @DisplayName("존재하는 콘텐츠 삭제 성공")
        void withExistingContent_deletesSuccessfully() {
            // given
            UUID contentId = UUID.randomUUID();
            ContentModel contentModel = createContentModelWithId(contentId);

            willAnswer(invocation -> {
                invocation.<Consumer<Object>>getArgument(0).accept(null);
                return null;
            }).given(transactionTemplate).executeWithoutResult(any());

            setupAfterCommitExecutor();
            given(contentService.getById(contentId)).willReturn(contentModel);

            // when
            contentFacade.delete(contentId);

            // then
            then(contentService).should().getById(contentId);
            then(contentService).should().delete(any(ContentModel.class));
        }
    }
}
