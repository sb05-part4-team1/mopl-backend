package com.mopl.domain.model.review;

import com.mopl.domain.exception.review.InvalidReviewDataException;
import com.mopl.domain.model.base.BaseUpdatableModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 오직 create() 메서드를 통해서만 유효한 객체를 만들도록 강제
public class ReviewModel extends BaseUpdatableModel {

    public static final int TEXT_MAX_LENGTH = 10_000;

    private UUID contentId;
    private UUID authorId; // 리뷰를 쓴 작성자 객체
    private String text;
    private BigDecimal rating;

    // 생성자 대신에 스태틱을 이용해 객체를 만듦, 유효성 검사
    public static ReviewModel create(
        UUID contentId,
        UUID authorId,
        String text,
        BigDecimal rating
    ) {
        if (contentId == null) {
            throw new InvalidReviewDataException("콘텐츠 ID는 null일 수 없습니다.");
        }
        if (authorId == null) {
            throw new InvalidReviewDataException("작성자 ID는 null일 수 없습니다.");
        }
        if (rating == null) {
            throw new InvalidReviewDataException("평점은 null일 수 없습니다.");
        }

        // 비즈니스 로직 검사 (아래에 메서드로 구현함)
        validateText(text);
        validateRating(rating);

        // 검사 통과시 객체 생성 및 반환
        return ReviewModel.builder()
            .contentId(contentId)
            .authorId(authorId)
            .text(text)
            .rating(rating)
            .build();
    }

    //    // 수정 메서드인데 뭔가 오류가 많아서 우선 주석처리 추후 리팩토링 예정
    //    public ReviewModel update(
    //            String newText,
    //            BigDecimal newRating
    //    ) {
    //        if (newText != null) {
    //            validateText(newText);
    //            this.text = newText;
    //        }
    //        if (newRating != null) {
    //            validateRating(newRating);
    //            this.rating = newRating;
    //        }
    //        return this;
    //    }

    // 리뷰 내용 검증 로직 구현
    private static void validateText(String text) {
        if (text == null) {
            return;
        }
        if (text.length() > TEXT_MAX_LENGTH) {
            throw new InvalidReviewDataException("리뷰 내용은 " + TEXT_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

    // 평점 검증 로직 구현
    private static void validateRating(BigDecimal rating) {
        if (rating.compareTo(BigDecimal.ZERO) < 0 || rating.compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new InvalidReviewDataException("평점은 0.0 이상 5.0 이하만 가능합니다.");
        }

        BigDecimal normalized = rating.stripTrailingZeros();

        if (normalized.scale() > 0) {
            throw new InvalidReviewDataException("평점은 정수만 가능합니다. (0.5 단위는 허용되지 않습니다.)");
        }
    }

}
