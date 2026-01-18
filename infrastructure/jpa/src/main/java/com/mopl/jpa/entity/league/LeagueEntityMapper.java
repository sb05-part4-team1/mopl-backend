package com.mopl.jpa.entity.league;

import com.mopl.domain.model.league.LeagueModel;
import org.springframework.stereotype.Component;

@Component
public class LeagueEntityMapper {

    public LeagueModel toModel(LeagueEntity entity) {
        if (entity == null) {
            return null;
        }

        return LeagueModel.builder()
            .id(entity.getId())
            .leagueId(entity.getLeagueId())
            .name(entity.getName())
            .sport(entity.getSport())
            .createdAt(entity.getCreatedAt())
            .deletedAt(entity.getDeletedAt())
            .build();
    }

    public LeagueEntity toEntity(LeagueModel model) {
        if (model == null) {
            return null;
        }

        return LeagueEntity.builder()
            .id(model.getId())
            .leagueId(model.getLeagueId())
            .name(model.getName())
            .sport(model.getSport())
            .createdAt(model.getCreatedAt())
            .deletedAt(model.getDeletedAt())
            .build();
    }
}
