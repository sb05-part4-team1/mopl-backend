package com.mopl.domain.model.tag;

import com.mopl.domain.model.base.BaseModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
public class GenreModel extends BaseModel {

    private Long tmdbId;
    private String name;

    public static GenreModel create(Long tmdbId, String name) {
        return GenreModel.builder()
            .tmdbId(tmdbId)
            .name(name.strip())
            .build();
    }
}
