package com.mopl.api.interfaces.api.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.api.application.review.ReviewFacade;
import com.mopl.api.config.TestSecurityConfig;
import com.mopl.api.interfaces.api.ApiControllerAdvice;
import com.mopl.api.interfaces.api.user.UserSummary;
import com.mopl.domain.repository.review.ReviewQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.domain.support.cursor.SortDirection;
import com.mopl.security.userdetails.MoplUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@Import({ApiControllerAdvice.class, TestSecurityConfig.class})
@DisplayName("ReviewController 단위 테스트")
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewFacade reviewFacade;

    private MoplUserDetails mockUserDetails;
    private UUID mockUserId;

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    void setUp() {
        mockUserId = UUID.randomUUID();

        // Mock 객체 생성
        mockUserDetails = mock(MoplUserDetails.class);

        // [핵심 수정] 컨트롤러가 사용하는 메서드 이름(userId())에 맞춰 Stubbing
        given(mockUserDetails.userId()).willReturn(mockUserId);

        // 기존 Stubbing (필요하다면 유지, 컨트롤러가 안 쓴다면 없어도 무방하나 안전하게 유지)
        given(mockUserDetails.getUsername()).willReturn(mockUserId.toString());
        given(mockUserDetails.getAuthorities())
            .willReturn(
                (Collection) Collections.singleton(
                    new SimpleGrantedAuthority("ROLE_USER")
                )
            );
    }

    @Nested
    @DisplayName("createReview()")
    class CreateReviewTest {

        @Test
        @DisplayName("인증된 유저가 요청하면 201을 반환하고 결과 응답을 내려준다")
        void createReview_withAuth_returns201() throws Exception {
            // given
            UUID contentId = UUID.randomUUID();
            UUID reviewId = UUID.randomUUID();

            ReviewCreateRequest request = new ReviewCreateRequest(
                contentId,
                "테스트 리뷰",
                4.0
            );

            ReviewResponse response = new ReviewResponse(
                reviewId,
                contentId,
                new UserSummary(mockUserId, "홍길동", "profile.png"),
                "테스트 리뷰",
                4.0
            );

            given(reviewFacade.createReview(eq(mockUserId), any(ReviewCreateRequest.class)))
                .willReturn(response);

            // when & then
            mockMvc.perform(
                post("/api/reviews")
                    .with(csrf())
                    .with(user(mockUserDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(reviewId.toString()))
                .andExpect(jsonPath("$.text").value("테스트 리뷰"))
                .andExpect(jsonPath("$.author.userId").value(mockUserId.toString()));

            then(reviewFacade).should().createReview(eq(mockUserId), any(
                ReviewCreateRequest.class));
        }

        @Test
        @DisplayName("인증 정보가 없으면 403을 반환한다")
        void createReview_withoutAuth_returns403() throws Exception {
            ReviewCreateRequest request = new ReviewCreateRequest(
                UUID.randomUUID(), "내용", 5.0
            );

            mockMvc.perform(
                post("/api/reviews")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isForbidden());

            then(reviewFacade).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("getReviews()")
    class GetReviewsTest {

        @Test
        @DisplayName("유효한 요청 시 200 OK 응답과 리뷰 목록 반환")
        void withValidRequest_returns200OKWithReviewList() throws Exception {
            // given
            UUID contentId = UUID.randomUUID();
            UUID reviewId1 = UUID.randomUUID();
            UUID reviewId2 = UUID.randomUUID();

            ReviewResponse response1 = new ReviewResponse(
                reviewId1,
                contentId,
                new UserSummary(UUID.randomUUID(), "유저1", null),
                "리뷰 내용1",
                4.5
            );
            ReviewResponse response2 = new ReviewResponse(
                reviewId2,
                contentId,
                new UserSummary(UUID.randomUUID(), "유저2", null),
                "리뷰 내용2",
                3.0
            );

            CursorResponse<ReviewResponse> cursorResponse = CursorResponse.of(
                List.of(response1, response2),
                "2025-01-01T00:00:00Z",
                reviewId2,
                true,
                10,
                "createdAt",
                SortDirection.DESCENDING
            );

            given(reviewFacade.getReviews(any(ReviewQueryRequest.class)))
                .willReturn(cursorResponse);

            // when & then
            mockMvc.perform(get("/api/reviews")
                .with(user(mockUserDetails))
                .param("contentId", contentId.toString())
                .param("limit", "10")
                .param("sortDirection", "DESCENDING")
                .param("sortBy", "createdAt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].text").value("리뷰 내용1"))
                .andExpect(jsonPath("$.data[0].rating").value(4.5))
                .andExpect(jsonPath("$.data[1].text").value("리뷰 내용2"))
                .andExpect(jsonPath("$.data[1].rating").value(3.0))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.totalCount").value(10))
                .andExpect(jsonPath("$.sortBy").value("createdAt"))
                .andExpect(jsonPath("$.sortDirection").value("DESCENDING"));

            then(reviewFacade).should().getReviews(any(ReviewQueryRequest.class));
        }

        @Test
        @DisplayName("rating 정렬 요청 처리")
        void withRatingSortBy_handlesSorting() throws Exception {
            // given
            CursorResponse<ReviewResponse> cursorResponse = CursorResponse.of(
                List.of(),
                null,
                null,
                false,
                0,
                "rating",
                SortDirection.DESCENDING
            );

            given(reviewFacade.getReviews(any(ReviewQueryRequest.class)))
                .willReturn(cursorResponse);

            // when & then
            mockMvc.perform(get("/api/reviews")
                .with(user(mockUserDetails))
                .param("sortBy", "rating")
                .param("sortDirection", "DESCENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sortBy").value("rating"));
        }

        @Test
        @DisplayName("커서 기반 페이지네이션 요청 처리")
        void withCursorParams_handlesPagination() throws Exception {
            // given
            UUID idAfter = UUID.randomUUID();
            CursorResponse<ReviewResponse> cursorResponse = CursorResponse.of(
                List.of(),
                null,
                null,
                false,
                5,
                "createdAt",
                SortDirection.DESCENDING
            );

            given(reviewFacade.getReviews(any(ReviewQueryRequest.class)))
                .willReturn(cursorResponse);

            // when & then
            mockMvc.perform(get("/api/reviews")
                .with(user(mockUserDetails))
                .param("cursor", "2025-01-01T00:00:00Z")
                .param("idAfter", idAfter.toString())
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasNext").value(false));
        }

        @Test
        @DisplayName("빈 결과 시 빈 목록 반환")
        void withNoResults_returnsEmptyList() throws Exception {
            // given
            CursorResponse<ReviewResponse> emptyResponse = CursorResponse.empty(
                "createdAt",
                SortDirection.DESCENDING
            );

            given(reviewFacade.getReviews(any(ReviewQueryRequest.class)))
                .willReturn(emptyResponse);

            // when & then
            mockMvc.perform(get("/api/reviews")
                .with(user(mockUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.totalCount").value(0));
        }
    }

    @Nested
    @DisplayName("updateReview()")
    class UpdateReviewTest {

        @Test
        @DisplayName("인증된 유저가 수정 요청하면 200을 반환한다")
        void updateReview_withAuth_returns200() throws Exception {
            // given
            UUID reviewId = UUID.randomUUID();
            ReviewUpdateRequest request = new ReviewUpdateRequest(
                "수정된 내용",
                5.0
            );

            ReviewResponse response = new ReviewResponse(
                reviewId,
                UUID.randomUUID(),
                new UserSummary(mockUserId, "홍길동", null),
                "수정된 내용",
                5.0
            );

            given(reviewFacade.updateReview(eq(mockUserId), eq(reviewId), any(
                ReviewUpdateRequest.class)))
                .willReturn(response);

            // when & then
            mockMvc.perform(
                patch("/api/reviews/{reviewId}", reviewId)
                    .with(csrf())
                    .with(user(mockUserDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("수정된 내용"));

            then(reviewFacade).should().updateReview(eq(mockUserId), eq(reviewId), any(
                ReviewUpdateRequest.class));
        }
    }

    @Nested
    @DisplayName("deleteReview()")
    class DeleteReviewTest {

        @Test
        @DisplayName("인증된 유저가 삭제 요청하면 204를 반환한다")
        void deleteReview_withAuth_returns204() throws Exception {
            // given
            UUID reviewId = UUID.randomUUID();

            // when & then
            mockMvc.perform(
                delete("/api/reviews/{reviewId}", reviewId)
                    .with(csrf())
                    .with(user(mockUserDetails))
            )
                .andExpect(status().isNoContent());

            // Facade 호출 검증
            then(reviewFacade).should().deleteReview(eq(mockUserId), eq(reviewId));
        }

        @Test
        @DisplayName("인증 정보가 없으면 삭제 요청 시 403을 반환한다")
        void deleteReview_withoutAuth_returns403() throws Exception {
            UUID reviewId = UUID.randomUUID();

            mockMvc.perform(
                delete("/api/reviews/{reviewId}", reviewId)
                    .with(csrf())
            )
                .andExpect(status().isForbidden());

            then(reviewFacade).shouldHaveNoInteractions();
        }
    }
}
