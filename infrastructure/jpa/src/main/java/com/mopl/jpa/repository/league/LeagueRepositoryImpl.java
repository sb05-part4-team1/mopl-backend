package com.mopl.jpa.repository.league;

import com.mopl.domain.model.league.LeagueModel;
import com.mopl.domain.repository.league.LeagueRepository;
import com.mopl.jpa.entity.league.LeagueEntity;
import com.mopl.jpa.entity.league.LeagueEntityMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LeagueRepositoryImpl implements LeagueRepository {

    private final JpaLeagueRepository jpaLeagueRepository;
    private final LeagueEntityMapper mapper;

    @Override
    public LeagueModel save(LeagueModel model) {
        LeagueEntity entity = mapper.toEntity(model);
        return mapper.toModel(jpaLeagueRepository.save(entity));
    }

    @Override
    public boolean existsByLeagueId(Long leagueId) {
        return jpaLeagueRepository.existsByLeagueId(leagueId);
    }

    @Override
    public List<LeagueModel> findAll() {
        return jpaLeagueRepository.findAll().stream()
            .map(mapper::toModel)
            .toList();
    }

    @Override
    public long count() {
        return jpaLeagueRepository.count();
    }
}
