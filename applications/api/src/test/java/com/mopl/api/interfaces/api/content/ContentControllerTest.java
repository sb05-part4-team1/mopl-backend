package com.mopl.api.interfaces.api.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.api.application.content.ContentFacade;
import com.mopl.api.config.TestSecurityConfig;
import com.mopl.api.interfaces.api.ApiControllerAdvice;
import com.mopl.api.interfaces.api.content.dto.ContentCreateRequest;
import com.mopl.dto.content.ContentResponse;
import com.mopl.api.interfaces.api.content.dto.ContentUpdateRequest;
import com.mopl.dto.content.ContentResponseMapper;
import com.mopl.domain.exception.content.ContentNotFoundException;
import com.mopl.domain.exception.content.InvalidContentDataException;
import com.mopl.domain.model.content.ContentModel.ContentType;
import com.mopl.domain.repository.content.query.ContentQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.mopl.security.userdetails.MoplUserDetails;
import com.mopl.storage.provider.StorageProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ContentController.class)
@Import({
    ApiControllerAdvice.class,
    ContentResponseMapper.class,
    TestSecurityConfig.class
})
@DisplayName("ContentController 슬라이스 테스트")
class ContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContentFacade contentFacade;

    @MockBean
    @SuppressWarnings("unused")
    private StorageProvider storageProvider;

    private MoplUserDetails mockAdminDetails;
    private MoplUserDetails mockUserDetails;

    private static final String DEFAULT_TITLE = "인셉션";
    private static final String DEFAULT_DESCRIPTION = "꿈속의 꿈을 다룬 SF 영화";
    private static final String DEFAULT_THUMBNAIL_URL = "https://cdn.example.com/contents/thumbnail.jpg";

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    void setUp() {
        UUID adminId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockAdminDetails = mock(MoplUserDetails.class);
        given(mockAdminDetails.userId()).willReturn(adminId);
        given(mockAdminDetails.getUsername()).willReturn(adminId.toString());
        given(mockAdminDetails.getAuthorities()).willReturn(
            (Collection) List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        mockUserDetails = mock(MoplUserDetails.class);
        given(mockUserDetails.userId()).willReturn(userId);
        given(mockUserDetails.getUsername()).willReturn(userId.toString());
        given(mockUserDetails.getAuthorities()).willReturn(
            (Collection) List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    private static ContentResponse createContentResponse(UUID id, List<String> tags) {
        return new ContentResponse(
            id,
            ContentType.movie,
            DEFAULT_TITLE,
            DEFAULT_DESCRIPTION,
            DEFAULT_THUMBNAIL_URL,
            tags,
            4.5,
            100,
            500L
        );
    }

    @Nested
    @DisplayName("GET /api/contents - 콘텐츠 목록 조회")
    class GetContentsTest {

        @Test
        @DisplayName("유효한 요청 시 200 OK와 콘텐츠 목록 반환")
        void withValidRequest_returns200OKWithContentList() throws Exception {
            // given
            UUID contentId1 = UUID.randomUUID();
            UUID contentId2 = UUID.randomUUID();
            ContentResponse content1 = createContentResponse(contentId1, List.of("SF", "Action"));
            ContentResponse content2 = createContentResponse(contentId2, List.of("Drama"));

            CursorResponse<ContentResponse> cursorResponse = CursorResponse.of(
                List.of(content1, content2),
                "500",
                contentId2,
                true,
                100,
                "watcherCount",
                SortDirection.DESCENDING
            );

            given(contentFacade.getContents(any(ContentQueryRequest.class))).willReturn(cursorResponse);

            // when & then
            mockMvc.perform(get("/api/contents")
                .with(user(mockUserDetails))
                .param("limit", "20")
                .param("sortDirection", "DESCENDING")
                .param("sortBy", "watcherCount"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(contentId1.toString()))
                .andExpect(jsonPath("$.data[0].title").value(DEFAULT_TITLE))
                .andExpect(jsonPath("$.data[0].tags").isArray())
                .andExpect(jsonPath("$.data[0].tags.length()").value(2))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.totalCount").value(100))
                .andExpect(jsonPath("$.sortBy").value("watcherCount"))
                .andExpect(jsonPath("$.sortDirection").value("DESCENDING"));

            then(contentFacade).should().getContents(any(ContentQueryRequest.class));
        }

        @Test
        @DisplayName("필터 파라미터 정상 적용")
        void withFilterParams_appliesFilters() throws Exception {
            // given
            CursorResponse<ContentResponse> emptyResponse = CursorResponse.empty(
                "watcherCount", SortDirection.DESCENDING
            );

            given(contentFacade.getContents(any(ContentQueryRequest.class))).willReturn(emptyResponse);

            // when & then
            mockMvc.perform(get("/api/contents")
                .with(user(mockUserDetails))
                .param("typeEqual", "movie")
                .param("keywordLike", "인셉션")
                .param("tagsIn", "SF,Action"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.hasNext").value(false));

            then(contentFacade).should().getContents(any(ContentQueryRequest.class));
        }

        @Test
        @DisplayName("커서 기반 페이지네이션 처리")
        void withCursorParams_handlesPagination() throws Exception {
            // given
            UUID idAfter = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();
            ContentResponse content = createContentResponse(contentId, List.of("SF"));

            CursorResponse<ContentResponse> cursorResponse = CursorResponse.of(
                List.of(content),
                null,
                null,
                false,
                50,
                "watcherCount",
                SortDirection.DESCENDING
            );

            given(contentFacade.getContents(any(ContentQueryRequest.class))).willReturn(cursorResponse);

            // when & then
            mockMvc.perform(get("/api/contents")
                .with(user(mockUserDetails))
                .param("cursor", "100")
                .param("idAfter", idAfter.toString())
                .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.nextCursor").doesNotExist());

            then(contentFacade).should().getContents(any(ContentQueryRequest.class));
        }

        @Test
        @DisplayName("결과 없을 시 빈 목록 반환")
        void withNoResults_returnsEmptyList() throws Exception {
            // given
            CursorResponse<ContentResponse> emptyResponse = CursorResponse.empty(
                "watcherCount", SortDirection.DESCENDING
            );

            given(contentFacade.getContents(any(ContentQueryRequest.class))).willReturn(emptyResponse);

            // when & then
            mockMvc.perform(get("/api/contents")
                .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.totalCount").value(0));

            then(contentFacade).should().getContents(any(ContentQueryRequest.class));
        }
    }

    @Nested
    @DisplayName("GET /api/contents/{contentId} - 콘텐츠 상세 조회")
    class GetContentTest {

        @Test
        @DisplayName("유효한 콘텐츠 ID로 조회 시 200 OK 반환")
        void withValidContentId_returns200OK() throws Exception {
            // given
            UUID contentId = UUID.randomUUID();
            ContentResponse response = createContentResponse(contentId, List.of("SF", "Action"));

            given(contentFacade.getContent(contentId)).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/contents/{contentId}", contentId)
                .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(contentId.toString()))
                .andExpect(jsonPath("$.type").value("movie"))
                .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
                .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
                .andExpect(jsonPath("$.thumbnailUrl").value(DEFAULT_THUMBNAIL_URL))
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags.length()").value(2))
                .andExpect(jsonPath("$.averageRating").value(4.5))
                .andExpect(jsonPath("$.reviewCount").value(100))
                .andExpect(jsonPath("$.watcherCount").value(500));

            then(contentFacade).should().getContent(contentId);
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠 ID로 요청 시 404 Not Found 반환")
        void withNonExistingContentId_returns404NotFound() throws Exception {
            // given
            UUID nonExistingContentId = UUID.randomUUID();

            given(contentFacade.getContent(nonExistingContentId))
                .willThrow(ContentNotFoundException.withId(nonExistingContentId));

            // when & then
            mockMvc.perform(get("/api/contents/{contentId}", nonExistingContentId)
                .with(user(mockUserDetails)))
                .andExpect(status().isNotFound());

            then(contentFacade).should().getContent(nonExistingContentId);
        }
    }

    @Nested
    @DisplayName("POST /api/contents - 콘텐츠 업로드")
    class UploadTest {

        @Test
        @DisplayName("ADMIN 권한과 유효한 요청 시 201 Created 반환")
        void withAdminAndValidRequest_returns201Created() throws Exception {
            // given
            UUID contentId = UUID.randomUUID();
            ContentCreateRequest request = new ContentCreateRequest(
                ContentType.movie,
                DEFAULT_TITLE,
                DEFAULT_DESCRIPTION,
                List.of("SF", "Action")
            );

            MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
            );

            MockMultipartFile thumbnail = new MockMultipartFile(
                "thumbnail",
                "thumbnail.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "thumbnail image content".getBytes()
            );

            ContentResponse response = createContentResponse(contentId, List.of("SF", "Action"));

            given(contentFacade.upload(any(ContentCreateRequest.class), any(MultipartFile.class)))
                .willReturn(response);

            // when & then
            mockMvc.perform(multipart(HttpMethod.POST, "/api/contents")
                .file(requestPart)
                .file(thumbnail)
                .with(user(mockAdminDetails)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(contentId.toString()))
                .andExpect(jsonPath("$.type").value("movie"))
                .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags.length()").value(2));

            then(contentFacade).should().upload(any(ContentCreateRequest.class), any(MultipartFile.class));
        }

        @Test
        @DisplayName("USER 권한 시 403 Forbidden 반환")
        void withUserRole_returns403Forbidden() throws Exception {
            // given
            ContentCreateRequest request = new ContentCreateRequest(
                ContentType.movie,
                DEFAULT_TITLE,
                DEFAULT_DESCRIPTION,
                List.of("SF")
            );

            MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
            );

            MockMultipartFile thumbnail = new MockMultipartFile(
                "thumbnail",
                "thumbnail.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "thumbnail image content".getBytes()
            );

            // when & then
            mockMvc.perform(multipart(HttpMethod.POST, "/api/contents")
                .file(requestPart)
                .file(thumbnail)
                .with(user(mockUserDetails)))
                .andExpect(status().isForbidden());

            then(contentFacade).should(never()).upload(any(), any());
        }

        @Test
        @DisplayName("인증 없이 요청 시 403 Forbidden 반환")
        void withoutAuthentication_returns403Forbidden() throws Exception {
            // given
            ContentCreateRequest request = new ContentCreateRequest(
                ContentType.movie,
                DEFAULT_TITLE,
                DEFAULT_DESCRIPTION,
                List.of("SF")
            );

            MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
            );

            MockMultipartFile thumbnail = new MockMultipartFile(
                "thumbnail",
                "thumbnail.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "thumbnail image content".getBytes()
            );

            // when & then
            mockMvc.perform(multipart(HttpMethod.POST, "/api/contents")
                .file(requestPart)
                .file(thumbnail))
                .andExpect(status().isForbidden());

            then(contentFacade).should(never()).upload(any(), any());
        }

        @Test
        @DisplayName("유효하지 않은 콘텐츠 데이터로 요청 시 400 Bad Request 반환")
        void withInvalidContentData_returns400BadRequest() throws Exception {
            // given
            ContentCreateRequest request = new ContentCreateRequest(
                ContentType.movie,
                DEFAULT_TITLE,
                DEFAULT_DESCRIPTION,
                List.of("SF")
            );

            MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
            );

            MockMultipartFile thumbnail = new MockMultipartFile(
                "thumbnail",
                "thumbnail.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "thumbnail image content".getBytes()
            );

            given(contentFacade.upload(any(ContentCreateRequest.class), any(MultipartFile.class)))
                .willThrow(InvalidContentDataException.withDetailMessage("Thumbnail file is required."));

            // when & then
            mockMvc.perform(multipart(HttpMethod.POST, "/api/contents")
                .file(requestPart)
                .file(thumbnail)
                .with(user(mockAdminDetails)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PATCH /api/contents/{contentId} - 콘텐츠 수정")
    class UpdateTest {

        @Test
        @DisplayName("ADMIN 권한과 유효한 요청 시 200 OK 반환")
        void withAdminAndValidRequest_returns200OK() throws Exception {
            // given
            UUID contentId = UUID.randomUUID();
            ContentUpdateRequest request = new ContentUpdateRequest(
                "Updated Title",
                "Updated Description",
                List.of("Drama", "Romance")
            );

            MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
            );

            MockMultipartFile thumbnail = new MockMultipartFile(
                "thumbnail",
                "new-thumbnail.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "new thumbnail content".getBytes()
            );

            ContentResponse response = new ContentResponse(
                contentId,
                ContentType.movie,
                "Updated Title",
                "Updated Description",
                "https://cdn.example.com/new-thumbnail.jpg",
                List.of("Drama", "Romance"),
                4.5,
                100,
                500L
            );

            given(contentFacade.update(eq(contentId), any(ContentUpdateRequest.class), any(MultipartFile.class)))
                .willReturn(response);

            // when & then
            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/contents/{contentId}", contentId)
                .file(requestPart)
                .file(thumbnail)
                .with(user(mockAdminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(contentId.toString()))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.tags.length()").value(2));

            then(contentFacade).should().update(eq(contentId), any(ContentUpdateRequest.class), any(MultipartFile.class));
        }

        @Test
        @DisplayName("썸네일 없이 수정 시 200 OK 반환")
        void withoutThumbnail_returns200OK() throws Exception {
            // given
            UUID contentId = UUID.randomUUID();
            ContentUpdateRequest request = new ContentUpdateRequest(
                "Updated Title",
                null,
                null
            );

            MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
            );

            ContentResponse response = new ContentResponse(
                contentId,
                ContentType.movie,
                "Updated Title",
                DEFAULT_DESCRIPTION,
                DEFAULT_THUMBNAIL_URL,
                List.of("SF"),
                4.5,
                100,
                500L
            );

            given(contentFacade.update(eq(contentId), any(ContentUpdateRequest.class), isNull()))
                .willReturn(response);

            // when & then
            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/contents/{contentId}", contentId)
                .file(requestPart)
                .with(user(mockAdminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(contentId.toString()))
                .andExpect(jsonPath("$.title").value("Updated Title"));

            then(contentFacade).should().update(eq(contentId), any(ContentUpdateRequest.class), isNull());
        }

        @Test
        @DisplayName("USER 권한 시 403 Forbidden 반환")
        void withUserRole_returns403Forbidden() throws Exception {
            // given
            UUID contentId = UUID.randomUUID();
            ContentUpdateRequest request = new ContentUpdateRequest(
                "Updated Title",
                null,
                null
            );

            MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
            );

            // when & then
            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/contents/{contentId}", contentId)
                .file(requestPart)
                .with(user(mockUserDetails)))
                .andExpect(status().isForbidden());

            then(contentFacade).should(never()).update(any(), any(), any());
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠 ID로 요청 시 404 Not Found 반환")
        void withNonExistingContentId_returns404NotFound() throws Exception {
            // given
            UUID nonExistingContentId = UUID.randomUUID();
            ContentUpdateRequest request = new ContentUpdateRequest(
                "Updated Title",
                null,
                null
            );

            MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
            );

            given(contentFacade.update(eq(nonExistingContentId), any(ContentUpdateRequest.class), isNull()))
                .willThrow(ContentNotFoundException.withId(nonExistingContentId));

            // when & then
            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/contents/{contentId}", nonExistingContentId)
                .file(requestPart)
                .with(user(mockAdminDetails)))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/contents/{contentId} - 콘텐츠 삭제")
    class DeleteTest {

        @Test
        @DisplayName("ADMIN 권한과 유효한 요청 시 204 No Content 반환")
        void withAdminAndValidRequest_returns204NoContent() throws Exception {
            // given
            UUID contentId = UUID.randomUUID();

            willDoNothing().given(contentFacade).delete(contentId);

            // when & then
            mockMvc.perform(delete("/api/contents/{contentId}", contentId)
                .with(user(mockAdminDetails)))
                .andExpect(status().isNoContent());

            then(contentFacade).should().delete(contentId);
        }

        @Test
        @DisplayName("USER 권한 시 403 Forbidden 반환")
        void withUserRole_returns403Forbidden() throws Exception {
            // given
            UUID contentId = UUID.randomUUID();

            // when & then
            mockMvc.perform(delete("/api/contents/{contentId}", contentId)
                .with(user(mockUserDetails)))
                .andExpect(status().isForbidden());

            then(contentFacade).should(never()).delete(any());
        }

        @Test
        @DisplayName("인증 없이 요청 시 403 Forbidden 반환")
        void withoutAuthentication_returns403Forbidden() throws Exception {
            // given
            UUID contentId = UUID.randomUUID();

            // when & then
            mockMvc.perform(delete("/api/contents/{contentId}", contentId))
                .andExpect(status().isForbidden());

            then(contentFacade).should(never()).delete(any());
        }

        @Test
        @DisplayName("존재하지 않는 콘텐츠 ID로 요청 시 404 Not Found 반환")
        void withNonExistingContentId_returns404NotFound() throws Exception {
            // given
            UUID nonExistingContentId = UUID.randomUUID();

            willThrow(ContentNotFoundException.withId(nonExistingContentId))
                .given(contentFacade).delete(nonExistingContentId);

            // when & then
            mockMvc.perform(delete("/api/contents/{contentId}", nonExistingContentId)
                .with(user(mockAdminDetails)))
                .andExpect(status().isNotFound());

            then(contentFacade).should().delete(nonExistingContentId);
        }
    }
}
