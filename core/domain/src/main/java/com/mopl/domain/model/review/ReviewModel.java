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
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 오직 create() 메서드를 통해서만 유효한 객체를 만들도록 강제
public class ReviewModel extends BaseUpdatableModel {

    public static final int TEXT_MAX_LENGTH = 10_000;

    private ContentModel content;
    private UserModel author; // 리뷰를 쓴 작성자 객체
    private String text;
    private double rating;

    // 생성자 대신에 스태틱을 이용해 객체를 만듦, 유효성 검사
    public static ReviewModel create(
        ContentModel content,
        UserModel author,
        String text,
        double rating
    ) {
        if (content == null || content.getId() == null) {
            throw new InvalidReviewDataException("콘텐츠 정보는 null일 수 없습니다.");
        }
        if (author == null || author.getId() == null) {
            throw new InvalidReviewDataException("작성자 정보는 null일 수 없습니다.");
        }

        // 비즈니스 로직 검사 (아래에 메서드로 구현함)
        validateText(text);
        validateRating(rating);

        // 검사 통과시 객체 생성 및 반환
        return ReviewModel.builder()
            .content(content)
            .author(author)
            .text(text)
            .rating(rating)
            .build();
    }

    public ReviewModel update(
        String newText,
        double newRating
    ) {
        if (newText == null) {
            throw new InvalidReviewDataException("리뷰 내용은 null일 수 없습니다.");
        }

        validateText(newText);
        validateRating(newRating);

        this.text = newText;
        this.rating = newRating;

        return this;
    }

    // 리뷰 내용 검증 로직 구현
    private static void validateText(String text) {
        if (text == null) {
            return;
        }
        if (text.length() > TEXT_MAX_LENGTH) {
            throw new InvalidReviewDataException(
                "리뷰 내용은 " + TEXT_MAX_LENGTH + "자를 초과할 수 없습니다."
            );
        }
    }

    private static void validateRating(double rating) {
        if (rating < 0.0 || rating > 5.0) {
            throw new InvalidReviewDataException("평점은 0.0 이상 5.0 이하만 가능합니다.");
        }
    }
}
