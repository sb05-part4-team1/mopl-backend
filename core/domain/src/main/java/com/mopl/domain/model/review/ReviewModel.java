package com.mopl.domain.model.review;

import com.mopl.domain.exception.review.InvalidReviewDataException;
import com.mopl.domain.model.base.BaseUpdatableModel;
import com.mopl.domain.model.content.ContentModel;
import com.mopl.domain.model.user.UserModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
public class ReviewModel extends BaseUpdatableModel {

    public static final int TEXT_MAX_LENGTH = 10_000;

    private ContentModel content;
    private UserModel author;
    private String text;
    private double rating;

    public static ReviewModel create(
        ContentModel content,
        UserModel author,
        String text,
        double rating
    ) {
        if (content == null || content.getId() == null) {
            throw InvalidReviewDataException.withDetailMessage("콘텐츠 정보는 null일 수 없습니다.");
        }
        if (author == null || author.getId() == null) {
            throw InvalidReviewDataException.withDetailMessage("작성자 정보는 null일 수 없습니다.");
        }

        validateText(text);
        validateRating(rating);

        return ReviewModel.builder()
            .content(content)
            .author(author)
            .text(text)
            .rating(rating)
            .build();
    }

    public ReviewModel update(
        String newText,
        Double newRating
    ) {
        String updatedText = this.text;
        double updatedRating = this.rating;

        if (newText != null) {
            validateText(newText);
            updatedText = newText;
        }

        if (newRating != null) {
            validateRating(newRating);
            updatedRating = newRating;
        }

        return this.toBuilder()
            .text(updatedText)
            .rating(updatedRating)
            .build();
    }

    private static void validateText(String text) {
        if (text.length() > TEXT_MAX_LENGTH) {
            throw InvalidReviewDataException.withDetailMessage(
                "리뷰 내용은 " + TEXT_MAX_LENGTH + "자를 초과할 수 없습니다."
            );
        }
    }

    private static void validateRating(double rating) {
        if (rating < 0.0 || rating > 5.0) {
            throw InvalidReviewDataException.withDetailMessage("평점은 0.0 이상 5.0 이하만 가능합니다.");
        }
    }
}
