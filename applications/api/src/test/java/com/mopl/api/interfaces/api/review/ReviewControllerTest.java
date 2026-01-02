package com.mopl.api.interfaces.api.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.api.application.review.ReviewFacade;
import com.mopl.api.interfaces.api.ApiControllerAdvice;
import com.mopl.api.interfaces.api.user.UserSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiControllerAdvice.class)
@DisplayName("ReviewController 단위 테스트")
class ReviewControllerTest {

    private static final String REQUESTER_ID_HEADER = "X-USER-ID";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewFacade reviewFacade;

    @Nested
    @DisplayName("createReview()")
    class CreateReviewTest {

        @Test
        @DisplayName("리뷰 생성 요청이 오면 201을 반환하고, Facade가 반환한 ReviewResponse를 응답한다")
        void createReview_withValidRequest_returns201AndResponse() throws Exception {
            // given
            UUID requesterId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();
            UUID reviewId = UUID.randomUUID();

            ReviewCreateRequest request = new ReviewCreateRequest(
                contentId,
                "H2 테스트 리뷰",
                new BigDecimal("4.0")
            );

            // 결과로 내려줄 응답 객체 생성
            UserSummary authorSummary = new UserSummary(
                requesterId,
                "홍길동",
                "https://example.com/profile.png"
            );

            ReviewResponse response = new ReviewResponse(
                reviewId,
                contentId,
                authorSummary,
                "H2 테스트 리뷰",
                new BigDecimal("4.0")
            );

            // Facade가 바로 Response를 반환하도록 스터빙
            given(reviewFacade.createReview(eq(requesterId), refEq(request))).willReturn(response);

            // when & then
            mockMvc.perform(
                post("/api/reviews")
                    .header(REQUESTER_ID_HEADER, requesterId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(reviewId.toString()))
                .andExpect(jsonPath("$.contentId").value(contentId.toString()))
                .andExpect(jsonPath("$.text").value("H2 테스트 리뷰"))
                .andExpect(jsonPath("$.rating").value(4.0))
                .andExpect(jsonPath("$.author.userId").value(requesterId.toString()))
                .andExpect(jsonPath("$.author.name").value("홍길동"));

            then(reviewFacade).should().createReview(eq(requesterId), refEq(request));
        }
    }

    @Nested
    @DisplayName("updateReview()")
    class UpdateReviewTest {

        @Test
        @DisplayName("리뷰 수정 요청이 오면 200을 반환하고, 수정된 ReviewResponse를 응답한다")
        void updateReview_withValidRequest_returns200AndResponse() throws Exception {
            // given
            UUID requesterId = UUID.randomUUID();
            UUID contentId = UUID.randomUUID();
            UUID reviewId = UUID.randomUUID();

            // 수정 요청 데이터
            ReviewUpdateRequest request = new ReviewUpdateRequest(
                "수정된 리뷰 내용",
                new BigDecimal("5.0")
            );

            // 예상 응답 데이터
            UserSummary authorSummary = new UserSummary(
                requesterId,
                "홍길동",
                "https://example.com/profile.png"
            );

            ReviewResponse response = new ReviewResponse(
                reviewId,
                contentId,
                authorSummary,
                "수정된 리뷰 내용",
                new BigDecimal("5.0")
            );

            // Mocking
            given(reviewFacade.updateReview(eq(requesterId), eq(reviewId), refEq(request)))
                .willReturn(response);

            // when & then
            mockMvc.perform(
                patch("/api/reviews/{reviewId}", reviewId) // PATCH 요청
                    .header(REQUESTER_ID_HEADER, requesterId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId.toString()))
                .andExpect(jsonPath("$.text").value("수정된 리뷰 내용"))
                .andExpect(jsonPath("$.rating").value(5.0));

            // Facade 호출 검증
            then(reviewFacade).should().updateReview(eq(requesterId), eq(reviewId), refEq(request));
        }
    }
}
