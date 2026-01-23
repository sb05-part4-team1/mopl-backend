// package com.mopl.api.interfaces.api.content;
//
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.mopl.api.application.content.ContentFacade;
// import com.mopl.api.interfaces.api.ApiControllerAdvice;
// import com.mopl.domain.model.content.ContentModel;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.context.annotation.Import;
// import org.springframework.http.MediaType;
// import org.springframework.mock.web.MockMultipartFile;
// import org.springframework.test.web.servlet.MockMvc;
//
// import java.util.List;
// import java.util.UUID;
//
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.BDDMockito.given;
// import static org.mockito.BDDMockito.then;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
// @WebMvcTest(controllers = ContentController.class)
// @AutoConfigureMockMvc(addFilters = false)
// @Import(ApiControllerAdvice.class)
// @DisplayName("ContentController 슬라이스 테스트")
// class ContentControllerTest {
//
//     @Autowired
//     private MockMvc mockMvc;
//
//     @Autowired
//     private ObjectMapper objectMapper;
//
//     @MockBean
//     private ContentFacade contentFacade;
//
//     @MockBean
//     private ContentResponseMapper contentResponseMapper;
//
//     @Nested
//     @DisplayName("POST /api/contents - 콘텐츠 업로드")
//     class UploadTest {
//
//         @Test
//         @DisplayName("유효한 요청 시 201 Created 응답")
//         void withValidRequest_returns201Created() throws Exception {
//             // given
//             UUID contentId = UUID.randomUUID();
//
//             ContentCreateRequest request = new ContentCreateRequest(
//                 ContentModel.ContentType.movie, "인셉션", "꿈속의 꿈", List.of("SF", "액션")
//             );
//
//             MockMultipartFile requestPart = new MockMultipartFile(
//                 "request",
//                 "",
//                 MediaType.APPLICATION_JSON_VALUE,
//                 objectMapper.writeValueAsBytes(request)
//             );
//
//             MockMultipartFile thumbnailPart = new MockMultipartFile(
//                 "thumbnail",
//                 "inception.png",
//                 MediaType.IMAGE_PNG_VALUE,
//                 "image".getBytes()
//             );
//
//             ContentModel contentModel = ContentModel.builder()
//                 .id(contentId)
//                 .build();
//
//             ContentResponse response = new ContentResponse(
//                 contentId, ContentModel.ContentType.movie, "인셉션", "꿈속의 꿈",
//                 "https://mopl.com/inception.png",
//                 List.of("SF", "액션"), 0.0, 0, 0
//             );
//
//             given(contentFacade.upload(any(), any())).willReturn(contentModel);
//             given(contentResponseMapper.toResponse(contentModel)).willReturn(response);
//
//             // when & then
//             mockMvc.perform(
//                 multipart("/api/contents")
//                     .file(requestPart)
//                     .file(thumbnailPart)
//                     .accept(MediaType.APPLICATION_JSON)
//             )
//                 .andExpect(status().isCreated())
//                 .andExpect(jsonPath("$.id").value(contentId.toString()));
//
//             then(contentFacade).should().upload(any(), any());
//         }
//     }
//
//     @Nested
//     @DisplayName("GET /api/contents/{contentId} - 콘텐츠 조회")
//     class GetDetailTest {
//
//         @Test
//         @DisplayName("정상 조회 시 200 OK")
//         void withExistingId_returns200() throws Exception {
//             // given
//             UUID contentId = UUID.randomUUID();
//
//             ContentModel model = ContentModel.builder()
//                 .id(contentId)
//                 .build();
//
//             ContentResponse response = new ContentResponse(
//                 contentId, ContentModel.ContentType.movie, "인셉션", "꿈속의 꿈",
//                 "https://mopl.com/inception.png",
//                 List.of("SF"), 0.0, 0, 0
//             );
//
//             given(contentFacade.getDetail(contentId)).willReturn(model);
//             given(contentResponseMapper.toResponse(model)).willReturn(response);
//
//             // when & then
//             mockMvc.perform(get("/api/contents/{id}", contentId))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.id").value(contentId.toString()));
//         }
//     }
//
//     @Nested
//     @DisplayName("PATCH /api/contents/{contentId} - 콘텐츠 수정")
//     class UpdateTest {
//
//         @Test
//         @DisplayName("썸네일 없이 수정 시 200 OK")
//         void updateWithoutThumbnail_returns200() throws Exception {
//             // given
//             UUID contentId = UUID.randomUUID();
//
//             ContentUpdateRequest request = new ContentUpdateRequest(
//                 "수정된 제목", "수정된 설명", List.of("SF2")
//             );
//
//             MockMultipartFile requestPart = new MockMultipartFile(
//                 "request",
//                 "",
//                 MediaType.APPLICATION_JSON_VALUE,
//                 objectMapper.writeValueAsBytes(request)
//             );
//
//             ContentModel updatedModel = ContentModel.builder()
//                 .id(contentId)
//                 .build();
//
//             ContentResponse response = new ContentResponse(
//                 contentId, ContentModel.ContentType.movie, "수정된 제목", "수정된 설명",
//                 "https://mopl.com/inception.png",
//                 List.of("SF2"), 0.0, 0, 0
//             );
//
//             given(contentFacade.update(eq(contentId), any(), eq(null)))
//                 .willReturn(updatedModel);
//             given(contentResponseMapper.toResponse(updatedModel))
//                 .willReturn(response);
//
//             // when & then
//             mockMvc.perform(
//                 multipart("/api/contents/{id}", contentId)
//                     .file(requestPart)
//                     .with(req -> {
//                         req.setMethod("PATCH");
//                         return req;
//                     })
//                     .accept(MediaType.APPLICATION_JSON)
//             )
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.title").value("수정된 제목"));
//
//             then(contentFacade).should().update(eq(contentId), any(), eq(null));
//         }
//
//         @Test
//         @DisplayName("썸네일 포함 수정 시 200 OK")
//         void updateWithThumbnail_returns200() throws Exception {
//             // given
//             UUID contentId = UUID.randomUUID();
//
//             ContentUpdateRequest request = new ContentUpdateRequest(
//                 "수정된 제목", "수정된 설명", List.of("SF2")
//             );
//
//             MockMultipartFile requestPart = new MockMultipartFile(
//                 "request",
//                 "",
//                 MediaType.APPLICATION_JSON_VALUE,
//                 objectMapper.writeValueAsBytes(request)
//             );
//
//             MockMultipartFile thumbnailPart = new MockMultipartFile(
//                 "thumbnail",
//                 "new.png",
//                 MediaType.IMAGE_PNG_VALUE,
//                 "image".getBytes()
//             );
//
//             ContentModel updatedModel = ContentModel.builder()
//                 .id(contentId)
//                 .build();
//
//             ContentResponse response = new ContentResponse(
//                 contentId, ContentModel.ContentType.movie, "수정된 제목", "수정된 설명",
//                 "https://mopl.com/new.png",
//                 List.of("SF2"), 0.0, 0, 0
//             );
//
//             given(contentFacade.update(eq(contentId), any(), any()))
//                 .willReturn(updatedModel);
//             given(contentResponseMapper.toResponse(updatedModel))
//                 .willReturn(response);
//
//             // when & then
//             mockMvc.perform(
//                 multipart("/api/contents/{id}", contentId)
//                     .file(requestPart)
//                     .file(thumbnailPart)
//                     .with(req -> {
//                         req.setMethod("PATCH");
//                         return req;
//                     })
//                     .accept(MediaType.APPLICATION_JSON)
//             )
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.title").value("수정된 제목"));
//
//             then(contentFacade).should().update(eq(contentId), any(), any());
//         }
//     }
//
//     @Nested
//     @DisplayName("DELETE /api/contents/{contentId} - 콘텐츠 삭제")
//     class DeleteTest {
//
//         @Test
//         @DisplayName("삭제 요청 시 204 No Content 응답")
//         void delete_returns204NoContent() throws Exception {
//             // given
//             UUID contentId = UUID.randomUUID();
//
//             // when & then
//             mockMvc.perform(
//                 delete("/api/contents/{id}", contentId)
//                     .accept(MediaType.APPLICATION_JSON)
//             )
//                 .andExpect(status().isNoContent());
//
//             then(contentFacade).should().delete(contentId);
//         }
//     }
// }
