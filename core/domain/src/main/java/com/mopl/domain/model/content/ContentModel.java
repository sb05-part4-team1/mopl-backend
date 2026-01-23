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
    public static final int THUMBNAIL_URL_MAX_LENGTH = 1024;

    // TODO: FE 대문자 snake case로 변경
    public enum ContentType {
        movie,
        tvSeries,
        sport
    }

    private ContentType type;
    private String title;
    private String description;
    private String thumbnailUrl;

    private double averageRating;
    private int reviewCount;

    public static ContentModel create(
        ContentType type,
        String title,
        String description,
        String thumbnailUrl
    ) {
        validateRequiredFields(type, title, description, thumbnailUrl);
        validateTitle(title);
        validateThumbnailUrl(thumbnailUrl);

        return ContentModel.builder()
            .type(type)
            .title(title)
            .description(description)
            .thumbnailUrl(thumbnailUrl)
            .reviewCount(0)
            .averageRating(0.0)
            .build();
    }

    public ContentModel update(
        String newTitle,
        String newDescription,
        String newThumbnailUrl
    ) {
        String updatedTitle = this.title;
        String updatedDescription = this.description;
        String updatedThumbnailUrl = this.thumbnailUrl;

        if (newTitle != null) {
            validateTitle(newTitle);
            updatedTitle = newTitle;
        }

        if (newDescription != null) {
            validateDescription(newDescription);
            updatedDescription = newDescription;
        }

        if (newThumbnailUrl != null) {
            validateThumbnailUrl(newThumbnailUrl);
            updatedThumbnailUrl = newThumbnailUrl;
        }

        return this.toBuilder()
            .title(updatedTitle)
            .description(updatedDescription)
            .thumbnailUrl(updatedThumbnailUrl)
            .build();
    }

    public ContentModel applyReview(double rating) {
        int newCount = this.reviewCount + 1;

        double newAverage = ((this.averageRating * this.reviewCount) + rating) / newCount;

        return this.toBuilder()
            .reviewCount(newCount)
            .averageRating(newAverage)
            .build();
    }

    public ContentModel updateReview(double oldRating, double newRating) {
        if (this.reviewCount == 0) {
            return this;
        }

        double total = this.averageRating * this.reviewCount;
        double newAverage = (total - oldRating + newRating) / this.reviewCount;

        return this.toBuilder()
            .averageRating(newAverage)
            .build();
    }

    public ContentModel removeReview(double rating) {
        int newCount = this.reviewCount - 1;

        if (newCount <= 0) {
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

    private static void validateRequiredFields(
        ContentType type,
        String title,
        String description,
        String thumbnailUrl
    ) {
        if (type == null) {
            throw InvalidContentDataException.withDetailMessage("컨텐츠 타입은 필수입니다.");
        }
        if (title == null || title.isBlank()) {
            throw InvalidContentDataException.withDetailMessage("제목은 비어있을 수 없습니다.");
        }
        if (description == null || description.isBlank()) {
            throw InvalidContentDataException.withDetailMessage("설명은 비어있을 수 없습니다.");
        }
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
            throw InvalidContentDataException.withDetailMessage("썸네일 URL은 비어있을 수 없습니다.");
        }
    }

    private static void validateTitle(String title) {
        if (title.isBlank()) {
            throw InvalidContentDataException.withDetailMessage("제목은 비어있을 수 없습니다.");
        }
        if (title.length() > TITLE_MAX_LENGTH) {
            throw InvalidContentDataException.withDetailMessage("제목은 " + TITLE_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

    private static void validateDescription(String description) {
        if (description.isBlank()) {
            throw InvalidContentDataException.withDetailMessage("설명은 비어있을 수 없습니다.");
        }
    }

    private static void validateThumbnailUrl(String thumbnailUrl) {
        if (thumbnailUrl.isBlank()) {
            throw InvalidContentDataException.withDetailMessage("썸네일 URL은 비어있을 수 없습니다.");
        }
        if (thumbnailUrl.length() > THUMBNAIL_URL_MAX_LENGTH) {
            throw InvalidContentDataException.withDetailMessage("썸네일 URL은 " + THUMBNAIL_URL_MAX_LENGTH
                + "자를 초과할 수 없습니다.");
        }
    }
}
