package com.mopl.api.interfaces.api.review;

import com.mopl.api.application.review.ReviewFacade;
import com.mopl.api.interfaces.api.review.dto.ReviewCreateRequest;
import com.mopl.dto.review.ReviewResponse;
import com.mopl.api.interfaces.api.review.dto.ReviewUpdateRequest;
import com.mopl.domain.repository.review.ReviewQueryRequest;
import com.mopl.domain.support.cursor.CursorResponse;
import com.mopl.security.userdetails.MoplUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController implements ReviewApiSpec {

    private final ReviewFacade reviewFacade;

    @GetMapping
    public CursorResponse<ReviewResponse> getReviews(
        @ModelAttribute ReviewQueryRequest request
    ) {
        return reviewFacade.getReviews(request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse createReview(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @RequestBody @Valid ReviewCreateRequest request
    ) {
        return reviewFacade.createReview(
            userDetails.userId(),
            request
        );
    }

    @PatchMapping("/{reviewId}")
    public ReviewResponse updateReview(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @PathVariable UUID reviewId,
        @RequestBody @Valid ReviewUpdateRequest request
    ) {
        return reviewFacade.updateReview(
            userDetails.userId(),
            reviewId,
            request
        );
    }

    @DeleteMapping("/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(
        @AuthenticationPrincipal MoplUserDetails userDetails,
        @PathVariable UUID reviewId
    ) {
        reviewFacade.deleteReview(
            userDetails.userId(),
            reviewId
        );
    }
}
