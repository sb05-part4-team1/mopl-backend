package com.mopl.jpa.repository.league;

import com.mopl.jpa.entity.league.LeagueEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaLeagueRepository extends JpaRepository<LeagueEntity, UUID> {

    boolean existsByLeagueId(Long leagueId);
}
