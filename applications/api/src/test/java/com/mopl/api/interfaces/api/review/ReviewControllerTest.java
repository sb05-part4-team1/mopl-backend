package com.mopl.api.interfaces.api.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.api.application.review.ReviewFacade;
import com.mopl.api.interfaces.api.ApiControllerAdvice;
import com.mopl.api.interfaces.api.user.UserSummary;
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

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// addFilters = false를 제거해야 @AuthenticationPrincipal이 동작합니다.
@WebMvcTest(ReviewController.class)
@Import(ApiControllerAdvice.class)
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
    void setUp() {
        mockUserId = UUID.randomUUID();

        // Mock 객체 생성
        mockUserDetails = mock(MoplUserDetails.class);

        // [핵심 수정] 컨트롤러가 사용하는 메서드 이름(userId())에 맞춰 Stubbing
        given(mockUserDetails.userId()).willReturn(mockUserId);

        // 기존 Stubbing (필요하다면 유지, 컨트롤러가 안 쓴다면 없어도 무방하나 안전하게 유지)
        given(mockUserDetails.getUsername()).willReturn(mockUserId.toString());
        given(mockUserDetails.getAuthorities())
            .willReturn((Collection) Collections.singleton(new SimpleGrantedAuthority(
                "ROLE_USER")));
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
                new BigDecimal("4.0")
            );

            ReviewResponse response = new ReviewResponse(
                reviewId,
                contentId,
                new UserSummary(mockUserId, "홍길동", "profile.png"),
                "테스트 리뷰",
                new BigDecimal("4.0")
            );

            given(reviewFacade.createReview(eq(mockUserId), refEq(request))).willReturn(response);

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

            then(reviewFacade).should().createReview(eq(mockUserId), refEq(request));
        }

        @Test
        @DisplayName("인증 정보가 없으면 401을 반환한다")
        void createReview_withoutAuth_returns401() throws Exception {
            ReviewCreateRequest request = new ReviewCreateRequest(
                UUID.randomUUID(), "내용", BigDecimal.TEN
            );

            mockMvc.perform(
                post("/api/reviews")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isUnauthorized());

            then(reviewFacade).shouldHaveNoInteractions();
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
                new BigDecimal("5.0")
            );

            ReviewResponse response = new ReviewResponse(
                reviewId,
                UUID.randomUUID(),
                new UserSummary(mockUserId, "홍길동", null),
                "수정된 내용",
                new BigDecimal("5.0")
            );

            given(reviewFacade.updateReview(eq(mockUserId), eq(reviewId), refEq(request)))
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

            then(reviewFacade).should().updateReview(eq(mockUserId), eq(reviewId), refEq(request));
        }
    }

    @Nested
    @DisplayName("deleteReview()")
    class DeleteReviewTest {

        @Test
        @DisplayName("인증된 유저가 삭제 요청하면 200을 반환한다")
        void deleteReview_withAuth_returns200() throws Exception {
            // given
            UUID reviewId = UUID.randomUUID();

            // when & then
            mockMvc.perform(
                delete("/api/reviews/{reviewId}", reviewId)
                    .with(csrf())
                    .with(user(mockUserDetails))
            )
                .andExpect(status().isOk());

            // Facade 호출 검증
            then(reviewFacade).should().deleteReview(eq(mockUserId), eq(reviewId));
        }

        @Test
        @DisplayName("인증 정보가 없으면 삭제 요청 시 401을 반환한다")
        void deleteReview_withoutAuth_returns401() throws Exception {
            UUID reviewId = UUID.randomUUID();

            mockMvc.perform(
                delete("/api/reviews/{reviewId}", reviewId)
                    .with(csrf())
            )
                .andExpect(status().isUnauthorized());

            then(reviewFacade).shouldHaveNoInteractions();
        }
    }
}
