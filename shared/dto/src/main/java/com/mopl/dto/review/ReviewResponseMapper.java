package com.mopl.dto.review;

import com.mopl.domain.model.review.ReviewModel;
import com.mopl.dto.user.UserSummaryMapper;
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
