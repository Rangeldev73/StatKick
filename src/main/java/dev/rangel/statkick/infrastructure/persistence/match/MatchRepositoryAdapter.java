package dev.rangel.statkick.infrastructure.persistence.match;

import dev.rangel.statkick.domain.model.Match;
import dev.rangel.statkick.domain.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MatchRepositoryAdapter implements MatchRepository {

    private final MatchJpaRepository matchJpaRepository;
    private final MatchEntityMapper matchEntityMapper;

    @Override
    public void save(Match match) {
        MatchEntity entity = matchEntityMapper.toEntity(match);
        matchJpaRepository.save(entity);
    }

    @Override
    public void saveAll(List<Match> matches) {
        List<MatchEntity> entities = matches.stream()
                .map(matchEntityMapper::toEntity)
                .toList();
        matchJpaRepository.saveAll(entities);
    }

    @Override
    public Optional<Match> findById(Long id) {
        return matchJpaRepository.findByIdWithTeams(id)
                .map(matchEntityMapper::toDomain);
    }
}