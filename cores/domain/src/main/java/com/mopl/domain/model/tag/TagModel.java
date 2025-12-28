package com.mopl.domain.model.tag;

import com.mopl.domain.exception.tag.InvalidTagDataException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TagModel {

    public static final int NAME_MAX_LENGTH = 20;

    private UUID id;
    private String name;

    public static TagModel create(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidTagDataException("태그 이름은 비어있을 수 없습니다.");
        }

        validateName(name);

        return TagModel.builder()
            .name(name.strip())
            .build();
    }

    private static void validateName(String name) {
        if (name.length() > NAME_MAX_LENGTH) {
            throw new InvalidTagDataException("태그 이름은 " + NAME_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
    }
}
