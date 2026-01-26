package com.mopl.domain.repository.league;

import com.mopl.domain.model.league.LeagueModel;

import java.util.List;

public interface LeagueRepository {

    List<LeagueModel> findAll();

    long count();

    boolean existsByLeagueId(Long leagueId);

    LeagueModel save(LeagueModel model);
}
