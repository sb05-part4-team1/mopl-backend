package com.mopl.api.interfaces.api.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.api.application.content.ContentFacade;
import com.mopl.api.interfaces.api.ApiControllerAdvice;
import com.mopl.domain.model.content.ContentModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ContentController.class)
@Import({ApiControllerAdvice.class, ContentControllerAdvice.class})
@DisplayName("ContentController 슬라이스 테스트")
class ContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContentFacade contentFacade;

    @MockBean
    private ContentResponseMapper contentResponseMapper;

    @Nested
    @DisplayName("POST /api/contents - 콘텐츠 업로드")
    class UploadTest {

        @Test
        @DisplayName("유효한 요청 시 201 Created 응답")
        void withValidRequest_returns201Created() throws Exception {
            // given
            UUID contentId = UUID.randomUUID();
            ContentCreateRequest request = new ContentCreateRequest(
                "영화", "인셉션", "꿈속의 꿈", List.of("SF", "액션")
            );

            MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
            );

            MockMultipartFile thumbnailPart = new MockMultipartFile(
                "thumbnail",
                "inception.png",
                MediaType.IMAGE_PNG_VALUE,
                "inception-image".getBytes()
            );

            ContentModel contentModel = ContentModel.builder()
                .id(contentId)
                .title("인셉션")
                .build();

            ContentResponse response = new ContentResponse(
                contentId, "영화", "인셉션", "꿈속의 꿈", "https://image.com/inception.png",
                List.of("SF", "액션"), 0.0, 0, 0L
            );

            given(contentFacade.upload(any(ContentCreateRequest.class), any(MultipartFile.class)))
                .willReturn(contentModel);
            given(contentResponseMapper.toResponse(eq(contentModel)))
                .willReturn(response);

            // when & then
            mockMvc.perform(multipart("/api/contents")
                .file(requestPart)
                .file(thumbnailPart)
                .accept(MediaType.APPLICATION_JSON)) // Boundary 자동생성을 위해 contentType 설정 제거
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(contentId.toString()))
                .andExpect(jsonPath("$.title").value("인셉션"))
                .andExpect(jsonPath("$.tags").isArray());

            then(contentFacade).should().upload(any(ContentCreateRequest.class), any(
                MultipartFile.class));
        }

        @Test
        @DisplayName("필수 파트(request)가 누락되면 400 Bad Request 응답")
        void withMissingRequestPart_returns400BadRequest() throws Exception {
            // given
            MockMultipartFile thumbnailPart = new MockMultipartFile(
                "thumbnail", "inception.png", MediaType.IMAGE_PNG_VALUE, "inception-image"
                    .getBytes()
            );

            // when & then
            mockMvc.perform(multipart("/api/contents")
                .file(thumbnailPart))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exceptionName").value("MissingServletRequestPartException"));

            then(contentFacade).should(never()).upload(any(), any());
        }

        @Test
        @DisplayName("유효하지 않은 데이터(제목 공백) 시 400 Bad Request 응답")
        void withInvalidData_returns400BadRequest() throws Exception {
            // given
            ContentCreateRequest invalidRequest = new ContentCreateRequest("영화", "인셉션", "꿈속의 꿈",
                List.of());

            MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(invalidRequest)
            );

            MockMultipartFile thumbnailPart = new MockMultipartFile(
                "thumbnail", "incpetion.png", MediaType.IMAGE_PNG_VALUE, "inception-image"
                    .getBytes()
            );

            // when & then
            mockMvc.perform(multipart("/api/contents")
                .file(requestPart)
                .file(thumbnailPart))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.exceptionName").value("MethodArgumentNotValidException"));

            then(contentFacade).should(never()).upload(any(), any());
        }
    }
}
