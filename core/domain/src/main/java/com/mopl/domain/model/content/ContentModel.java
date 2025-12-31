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
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentModel extends BaseUpdatableModel {

    public static final int TYPE_MAX_LENGTH = 20;
    public static final int TITLE_MAX_LENGTH = 255;
    public static final int THUMBNAIL_URL_MAX_LENGTH = 1024;

    private String type;
    private String title;
    private String description;
    private String thumbnailUrl;

    /**
     * 연결된 태그 이름 목록 (NPE 방지를 위해 null 대신 빈 리스트(List.of)로 초기화)
     */
    @Builder.Default
    private List<String> tags = List.of();

    public static ContentModel create(
        String type,
        String title,
        String description,
        String thumbnailUrl
    ) {
        if (type == null || type.isBlank()) {
            throw new InvalidContentDataException("컨텐츠 타입은 비어있을 수 없습니다.");
        }
        if (title == null || title.isBlank()) {
            throw new InvalidContentDataException("제목은 비어있을 수 없습니다.");
        }
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
            throw new InvalidContentDataException("썸네일 URL은 비어있을 수 없습니다.");
        }

        validateType(type);
        validateTitle(title);
        validateThumbnailUrl(thumbnailUrl);

        return ContentModel.builder()
            .type(type)
            .title(title)
            .description(description)
            .thumbnailUrl(thumbnailUrl)
            .build();
    }

    private static void validateType(String type) {
        if (type.length() > TYPE_MAX_LENGTH) {
            throw new InvalidContentDataException("타입은 " + TYPE_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

    private static void validateTitle(String title) {
        if (title.length() > TITLE_MAX_LENGTH) {
            throw new InvalidContentDataException("제목은 " + TITLE_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

    private static void validateThumbnailUrl(String thumbnailUrl) {
        if (thumbnailUrl.length() > THUMBNAIL_URL_MAX_LENGTH) {
            throw new InvalidContentDataException("썸네일 URL은 " + THUMBNAIL_URL_MAX_LENGTH
                + "자를 초과할 수 없습니다.");
        }
    }
}
