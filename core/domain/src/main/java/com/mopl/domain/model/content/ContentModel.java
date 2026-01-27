package com.mopl.domain.model.content;

import com.mopl.domain.exception.content.InvalidContentDataException;
import com.mopl.domain.model.base.BaseUpdatableModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
public class ContentModel extends BaseUpdatableModel {

    public static final int CONTENT_TYPE_MAX_LENGTH = 20;
    public static final int TITLE_MAX_LENGTH = 255;
    public static final int DESCRIPTION_MAX_LENGTH = 10_000;
    public static final int THUMBNAIL_PATH_MAX_LENGTH = 1024;

    public enum ContentType {
        movie,
        tvSeries,
        sport
    }

    private ContentType type;
    private String title;
    private String description;
    private String thumbnailPath;

    private int reviewCount;
    private double averageRating;
    private double popularityScore;

    public static ContentModel create(
        ContentType type,
        String title,
        String description,
        String thumbnailPath
    ) {
        if (type == null) {
            throw InvalidContentDataException.withDetailMessage("콘텐츠 타입은 null일 수 없습니다.");
        }
        if (title == null) {
            throw InvalidContentDataException.withDetailMessage("제목은 null일 수 없습니다.");
        }
        if (description == null) {
            throw InvalidContentDataException.withDetailMessage("설명은 null일 수 없습니다.");
        }
        if (thumbnailPath == null) {
            throw InvalidContentDataException.withDetailMessage("썸네일 경로는 null일 수 없습니다.");
        }

        validateTitle(title);
        validateDescription(description);
        validateThumbnailPath(thumbnailPath);

        return ContentModel.builder()
            .type(type)
            .title(title)
            .description(description)
            .thumbnailPath(thumbnailPath)
            .reviewCount(0)
            .averageRating(0.0)
            .popularityScore(0.0)
            .build();
    }

    public ContentModel update(
        String newTitle,
        String newDescription,
        String newThumbnailPath
    ) {
        String updatedTitle = this.title;
        String updatedDescription = this.description;
        String updatedThumbnailPath = this.thumbnailPath;

        if (newTitle != null) {
            validateTitle(newTitle);
            updatedTitle = newTitle;
        }

        if (newDescription != null) {
            validateDescription(newDescription);
            updatedDescription = newDescription;
        }

        if (newThumbnailPath != null) {
            validateThumbnailPath(newThumbnailPath);
            updatedThumbnailPath = newThumbnailPath;
        }

        return this.toBuilder()
            .title(updatedTitle)
            .description(updatedDescription)
            .thumbnailPath(updatedThumbnailPath)
            .build();
    }

    public ContentModel addReview(double rating) {
        int newCount = this.reviewCount + 1;
        double total = this.averageRating * this.reviewCount;
        double newAverage = (total + rating) / newCount;

        return this.toBuilder()
            .reviewCount(newCount)
            .averageRating(newAverage)
            .build();
    }

    public ContentModel updateReview(double oldRating, double newRating) {
        if (this.reviewCount == 0) {
            throw InvalidContentDataException.withDetailMessage(
                "리뷰가 없는 콘텐츠의 리뷰를 수정할 수 없습니다."
            );
        }

        double total = this.averageRating * this.reviewCount;
        double newAverage = (total - oldRating + newRating) / this.reviewCount;

        return this.toBuilder()
            .averageRating(newAverage)
            .build();
    }

    public ContentModel removeReview(double rating) {
        if (this.reviewCount <= 0) {
            throw InvalidContentDataException.withDetailMessage("삭제할 리뷰가 없습니다.");
        }

        int newCount = this.reviewCount - 1;

        if (newCount == 0) {
            return this.toBuilder()
                .reviewCount(0)
                .averageRating(0.0)
                .build();
        }

        double total = this.averageRating * this.reviewCount;
        double newAverage = (total - rating) / newCount;

        return this.toBuilder()
            .reviewCount(newCount)
            .averageRating(newAverage)
            .build();
    }

    public ContentModel recalculatePopularity(double globalAvgRating, int minReviewCount) {
        double reviewCountAsDouble = this.reviewCount;
        double effectiveMinReviewCount = Math.max(minReviewCount, 1);
        double contentAverageRating = this.averageRating;
        double globalAverageRating = globalAvgRating;

        double popularityScore =
            ((reviewCountAsDouble / (reviewCountAsDouble + effectiveMinReviewCount)) * contentAverageRating)
                + ((effectiveMinReviewCount / (reviewCountAsDouble + effectiveMinReviewCount)) * globalAverageRating);

        return this.toBuilder()
            .popularityScore(popularityScore)
            .build();
    }

    private static void validateTitle(String title) {
        if (title.isBlank()) {
            throw InvalidContentDataException.withDetailMessage("제목은 비어있을 수 없습니다.");
        }
        if (title.length() > TITLE_MAX_LENGTH) {
            throw InvalidContentDataException.withDetailMessage(
                "제목은 " + TITLE_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

    private static void validateDescription(String description) {
        if (description.isBlank()) {
            throw InvalidContentDataException.withDetailMessage("설명은 비어있을 수 없습니다.");
        }
        if (description.length() > DESCRIPTION_MAX_LENGTH) {
            throw InvalidContentDataException.withDetailMessage(
                "설명은 " + DESCRIPTION_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

    private static void validateThumbnailPath(String thumbnailPath) {
        if (thumbnailPath.isBlank()) {
            throw InvalidContentDataException.withDetailMessage("썸네일 경로는 비어있을 수 없습니다.");
        }
        if (thumbnailPath.length() > THUMBNAIL_PATH_MAX_LENGTH) {
            throw InvalidContentDataException.withDetailMessage(
                "썸네일 경로는 " + THUMBNAIL_PATH_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }
}
