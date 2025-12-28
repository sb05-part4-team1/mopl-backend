package com.mopl.api.interfaces.api.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mopl.api.application.review.ReviewFacade;
import com.mopl.api.interfaces.api.user.UserSummary;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@DisplayName("ReviewController 단위 테스트")
class ReviewControllerTest {

    private static final String REQUESTER_ID_HEADER = "X-USER-ID";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewFacade reviewFacade;

    @MockBean
    private ReviewResponseMapper reviewResponseMapper;

    @Test
    @DisplayName("리뷰 생성 요청이 오면 201을 반환하고, ReviewResponse를 그대로 응답한다 (UserSummary 포함)")
    void createReview_withValidRequest_returns201AndResponse() throws Exception {
        // given
        UUID requesterId = UUID.randomUUID();
        UUID contentId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        ReviewCreateRequest request = new ReviewCreateRequest(
            contentId,
            "H2 테스트 리뷰",
            BigDecimal.valueOf(4)
        );

        ReviewModel reviewModel = ReviewModel.builder()
            .id(reviewId)
            .contentId(contentId)
            .author(UserModel.builder()
                .id(requesterId)
                .name("홍길동")
                .profileImageUrl("https://example.com/profile.png")
                .build())
            .text("H2 테스트 리뷰")
            .rating(BigDecimal.valueOf(4))
            .build();

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
            BigDecimal.valueOf(4)
        );

        given(reviewFacade.createReview(requesterId, request)).willReturn(reviewModel);
        given(reviewResponseMapper.toResponse(reviewModel)).willReturn(response);

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
            .andExpect(jsonPath("$.rating").value(4))
            .andExpect(jsonPath("$.author.userId").value(requesterId.toString()))
            .andExpect(jsonPath("$.author.name").value("홍길동"))
            .andExpect(jsonPath("$.author.profileImageUrl").value(
                "https://example.com/profile.png"));

        then(reviewFacade).should().createReview(requesterId, request);
        then(reviewResponseMapper).should().toResponse(reviewModel);
    }

    @Test
    @DisplayName("X-USER-ID 헤더가 없으면 400을 반환한다")
    void createReview_withoutRequesterHeader_returns400() throws Exception {
        // given
        ReviewCreateRequest request = new ReviewCreateRequest(
            UUID.randomUUID(),
            "리뷰",
            BigDecimal.valueOf(4)
        );

        // when & then
        mockMvc.perform(
            post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest());

        then(reviewFacade).shouldHaveNoInteractions();
        then(reviewResponseMapper).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("X-USER-ID가 UUID 형식이 아니면 400을 반환한다")
    void createReview_withInvalidRequesterHeader_returns400() throws Exception {
        // given
        ReviewCreateRequest request = new ReviewCreateRequest(
            UUID.randomUUID(),
            "리뷰",
            BigDecimal.valueOf(4)
        );

        // when & then
        mockMvc.perform(
            post("/api/reviews")
                .header(REQUESTER_ID_HEADER, "not-a-uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest());

        then(reviewFacade).shouldHaveNoInteractions();
        then(reviewResponseMapper).shouldHaveNoInteractions();
    }
}
