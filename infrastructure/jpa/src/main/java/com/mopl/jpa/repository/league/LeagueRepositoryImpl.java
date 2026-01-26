package com.mopl.jpa.repository.league;

import com.mopl.domain.model.league.LeagueModel;
import com.mopl.domain.repository.league.LeagueRepository;
import com.mopl.jpa.entity.league.LeagueEntity;
import com.mopl.jpa.entity.league.LeagueEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LeagueRepositoryImpl implements LeagueRepository {

    private final JpaLeagueRepository jpaLeagueRepository;
    private final LeagueEntityMapper leagueEntityMapper;

    @Override
    public List<LeagueModel> findAll() {
        return jpaLeagueRepository.findAll().stream()
            .map(leagueEntityMapper::toModel)
            .toList();
    }

    @Override
    public long count() {
        return jpaLeagueRepository.count();
    }

    @Override
    public boolean existsByLeagueId(Long leagueId) {
        return jpaLeagueRepository.existsByLeagueId(leagueId);
    }

    @Override
    public LeagueModel save(LeagueModel model) {
        LeagueEntity leagueEntity = leagueEntityMapper.toEntity(model);
        LeagueEntity savedLeagueEntity = jpaLeagueRepository.save(leagueEntity);
        return leagueEntityMapper.toModel(savedLeagueEntity);
    }
}
