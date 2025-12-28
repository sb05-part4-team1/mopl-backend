package com.mopl.api.application.review;

import com.mopl.api.interfaces.api.review.ReviewCreateRequest;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.review.ReviewService;
import com.mopl.domain.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReviewFacade {

    private final ReviewService reviewService;
    private final UserService userService;

    @Transactional
    public ReviewModel createReview(
        UUID requesterId,
        ReviewCreateRequest reviewCreateRequest
    ) {
        // UserModel author = userService.getById(requesterId); Merge가 다 되고 추후변경
        UserModel author = UserModel.builder().id(requesterId).build();

        UUID contentId = reviewCreateRequest.contentId();
        String text = reviewCreateRequest.text();
        BigDecimal rating = reviewCreateRequest.rating();

        return reviewService.create(
            contentId,
            author,
            text,
            rating
        );

    }
}
