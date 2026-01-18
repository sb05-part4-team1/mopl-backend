package com.mopl.domain.repository.league;

import com.mopl.domain.model.league.LeagueModel;
import java.util.List;

public interface LeagueRepository {

    LeagueModel save(LeagueModel model);

    boolean existsByLeagueId(Long leagueId);

    List<LeagueModel> findAll();

    long count();
}
