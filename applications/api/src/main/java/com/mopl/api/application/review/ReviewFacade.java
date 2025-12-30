package com.mopl.api.application.review;

import com.mopl.api.interfaces.api.review.ReviewCreateRequest;
import com.mopl.api.interfaces.api.review.ReviewResponse;
import com.mopl.api.interfaces.api.review.ReviewResponseMapper;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.review.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReviewFacade {

    private final ReviewService reviewService;
    // TODO: 추후 실제 조회 시 필요
    //  private final UserService userService;
    private final ReviewResponseMapper reviewResponseMapper;

    @Transactional
    public ReviewResponse createReview(
        UUID requesterId,
        ReviewCreateRequest reviewCreateRequest
    ) {
        // TODO: UserModel author = userService.getById(requesterId); Merge가 다 되고 추후변경
        UserModel author = UserModel.builder().id(requesterId).build();

        UUID contentId = reviewCreateRequest.contentId();
        String text = reviewCreateRequest.text();
        BigDecimal rating = reviewCreateRequest.rating();

        ReviewModel savedReview = reviewService.create(
            contentId,
            author,
            text,
            rating
        );

        return reviewResponseMapper.toResponse(savedReview, author);

    }
}
