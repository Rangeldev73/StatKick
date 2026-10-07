package dev.rangel.statkick.infrastructure.persistence.team;

import dev.rangel.statkick.domain.model.Team;
import dev.rangel.statkick.domain.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TeamRepositoryAdapter implements TeamRepository {

    private final TeamJpaRepository teamJpaRepository;
    private final TeamEntityMapper teamEntityMapper;

    @Override
    public void save(Team team) {
        TeamEntity entity = teamEntityMapper.toEntity(team);
        teamJpaRepository.save(entity);
    }

    @Override
    public void saveAll(List<Team> teams) {
        List<TeamEntity> entities = teams.stream()
                .map(teamEntityMapper::toEntity)
                .toList();
        teamJpaRepository.saveAll(entities);
    }

    @Override
    public Optional<Team> findById(Long id) {
        return teamJpaRepository.findById(id)
                .map(teamEntityMapper::toDomain);
    }
}