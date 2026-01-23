package com.mopl.domain.model.content;

import com.mopl.domain.exception.content.InvalidContentDataException;
import com.mopl.domain.model.base.BaseUpdatableModel;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentModel extends BaseUpdatableModel {

    public enum ContentType {
        movie,
        tvSeries,
        sport
    }

    public static final int CONTENT_TYPE_MAX_LENGTH = 20;
    public static final int TITLE_MAX_LENGTH = 255;
    public static final int THUMBNAIL_URL_MAX_LENGTH = 1024;

    private ContentType type;
    private String title;
    private String description;
    private String thumbnailUrl;

    @Builder.Default
    private List<String> tags = List.of();

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
        String title,
        String description,
        String thumbnailUrl
    ) {
        validateRequiredFields(this.type, title, description, thumbnailUrl);
        validateTitle(title);
        validateThumbnailUrl(thumbnailUrl);

        return this.toBuilder()
            .title(title)
            .description(description)
            .thumbnailUrl(thumbnailUrl)
            .build();
    }

    public ContentModel withTags(List<String> tags) {
        return this.toBuilder()
            .tags(tags)
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
        if (title.length() > TITLE_MAX_LENGTH) {
            throw InvalidContentDataException.withDetailMessage("제목은 " + TITLE_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

    private static void validateThumbnailUrl(String thumbnailUrl) {
        if (thumbnailUrl.length() > THUMBNAIL_URL_MAX_LENGTH) {
            throw InvalidContentDataException.withDetailMessage("썸네일 URL은 " + THUMBNAIL_URL_MAX_LENGTH
                + "자를 초과할 수 없습니다.");
        }
    }
}
