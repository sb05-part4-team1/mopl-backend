package com.mopl.domain.model.league;

import com.mopl.domain.model.base.BaseModel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LeagueModel extends BaseModel {

    private Long leagueId;
    private String name;
    private String sport;

    public static LeagueModel create(Long leagueId, String name, String sport) {
        return LeagueModel.builder()
            .leagueId(leagueId)
            .name(name.strip())
            .sport(sport.strip())
            .build();
    }
}
