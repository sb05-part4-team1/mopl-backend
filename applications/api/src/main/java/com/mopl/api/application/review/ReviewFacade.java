package com.mopl.api.application.review;

import com.mopl.api.interfaces.api.review.ReviewCreateRequest;
import com.mopl.api.interfaces.api.review.ReviewResponse;
import com.mopl.api.interfaces.api.review.ReviewResponseMapper;
import com.mopl.domain.exception.review.InvalidReviewDataException;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import com.mopl.domain.service.content.ContentService;
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
    private final ContentService contentService;
    private final ReviewResponseMapper reviewResponseMapper;

    @Transactional
    public ReviewResponse createReview(
        UUID requesterId,
        ReviewCreateRequest request
    ) {
        UserModel author = userService.getById(requesterId);

        // validate 메서드임 (ReviewService에서 옮김)
        if (!contentService.exists(request.contentId())) {
            // TODO: 임시로 이렇게 두고 나중에 ContentNotFoundException(contentId);로 바꾸기
            throw new InvalidReviewDataException(
                    "존재하지 않는 콘텐츠입니다. contentId=" + request.contentId()
            );
        }
                // 코드 스타일 변경전임 팀장님과 상의 후 다시 살릴지 고민
                //        UUID contentId = request.contentId();
                //        String text = request.text();
                //        BigDecimal rating = request.rating();

        ReviewModel savedReview = reviewService.create(
            request.contentId(),
            author,
            request.text(),
            request.rating()
        );

        return reviewResponseMapper.toResponse(savedReview, author);

    }
}
