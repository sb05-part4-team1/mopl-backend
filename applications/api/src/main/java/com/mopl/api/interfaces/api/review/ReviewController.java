package com.mopl.api.interfaces.api.review;

import com.mopl.api.application.review.ReviewFacade;
import com.mopl.security.userdetails.MoplUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewFacade reviewFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse createReview(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @RequestBody @Valid ReviewCreateRequest request
    ) {
        // 인증된 객체에서 id만 가져옴
        UUID requesterId = userDetails.userId();

        // 파사드가 이미 Response를 반환하므로 바로 리턴하면 끝!
        return reviewFacade.createReview(
            requesterId,
            request
        );
    }

    @PatchMapping("/{reviewId}")
    @ResponseStatus(HttpStatus.OK)
    public ReviewResponse updateReview(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @PathVariable UUID reviewId,
        @RequestBody @Valid ReviewUpdateRequest request
    ) {

        // 인증된 객체에서 id만 가져옴
        UUID requesterId = userDetails.userId();

        return reviewFacade.updateReview(
            requesterId,
            reviewId,
            request
        );
    }

    @DeleteMapping("/{reviewId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteReview(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @PathVariable UUID reviewId
    ) {
        // 인증된 객체에서 id만 가져옴
        UUID requesterId = userDetails.userId();

        reviewFacade.deleteReview(requesterId, reviewId);
    }
}
