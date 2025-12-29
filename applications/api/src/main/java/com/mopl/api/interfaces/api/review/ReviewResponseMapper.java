package com.mopl.api.interfaces.api.review;

import com.mopl.api.interfaces.api.user.UserSummary;
import com.mopl.api.interfaces.api.user.UserSummaryMapper;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewResponseMapper {

    private final UserSummaryMapper userSummaryMapper;

    public ReviewResponse toResponse(ReviewModel reviewModel, UserModel author) {
        return new ReviewResponse(
            reviewModel.getId(),
            reviewModel.getContentId(),
            userSummaryMapper.toSummary(author),
            reviewModel.getText(),
            reviewModel.getRating()
        );
    }
}
