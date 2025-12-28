package com.mopl.api.interfaces.api.review;

import com.mopl.api.interfaces.api.user.UserSummary;
import com.mopl.domain.model.review.ReviewModel;
import com.mopl.domain.model.user.UserModel;
import org.springframework.stereotype.Component;

@Component
public class ReviewResponseMapper {

    public ReviewResponse toResponse(ReviewModel reviewModel) {
        return new ReviewResponse(
            reviewModel.getId(),
            reviewModel.getContentId(),
            toUserSummary(reviewModel.getAuthor()),
            reviewModel.getText(),
            reviewModel.getRating()
        );
    }

    private UserSummary toUserSummary(UserModel author) {
        if (author == null) {
            return null;
        }

        return new UserSummary(
            author.getId(),
            author.getName(),
            author.getProfileImageUrl()
        );
    }
}
