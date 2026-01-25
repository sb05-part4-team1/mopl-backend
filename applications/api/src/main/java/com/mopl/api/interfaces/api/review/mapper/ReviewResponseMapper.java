package com.mopl.api.interfaces.api.review.mapper;

import com.mopl.api.interfaces.api.review.dto.ReviewResponse;
import com.mopl.dto.user.UserSummaryMapper;
import com.mopl.domain.model.review.ReviewModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewResponseMapper {

    private final UserSummaryMapper userSummaryMapper;

    public ReviewResponse toResponse(ReviewModel reviewModel) {
        return new ReviewResponse(
            reviewModel.getId(),
            reviewModel.getContent().getId(),
            userSummaryMapper.toSummary(reviewModel.getAuthor()),
            reviewModel.getText(),
            reviewModel.getRating()
        );
    }
}
