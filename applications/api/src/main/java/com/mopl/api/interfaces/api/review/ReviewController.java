package com.mopl.api.interfaces.api.review;

import com.mopl.api.application.review.ReviewFacade;
import com.mopl.domain.model.review.ReviewModel;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private static final String REQUESTER_ID_HEADER = "X-USER-ID";

    private final ReviewFacade reviewFacade;
    private final ReviewResponseMapper reviewResponseMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse createReview(
        @RequestHeader(REQUESTER_ID_HEADER) UUID requesterId,
        @RequestBody @Valid ReviewCreateRequest request
    ) {
        ReviewModel reviewModel = reviewFacade.createReview(
            requesterId,
            request
        );
        return reviewResponseMapper.toResponse(reviewModel);
    }
}
