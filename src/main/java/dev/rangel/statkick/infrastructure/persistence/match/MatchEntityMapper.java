package dev.rangel.statkick.infrastructure.persistence.match;

import dev.rangel.statkick.domain.model.Match;
import dev.rangel.statkick.domain.model.Score;
import dev.rangel.statkick.infrastructure.persistence.team.TeamEntityMapper;
import org.springframework.stereotype.Component;

@Component
public class MatchEntityMapper {

    private final TeamEntityMapper teamEntityMapper;

    public MatchEntityMapper(TeamEntityMapper teamEntityMapper) {
        this.teamEntityMapper = teamEntityMapper;
    }

    public MatchEntity toEntity(Match domain) {
        if (domain == null) {
            return null;
        }

        ScoreEmbeddable scoreEmbeddable = null;
        if (domain.getScore() != null) {
            scoreEmbeddable = new ScoreEmbeddable(
                    domain.getScore().homeGoals(),
                    domain.getScore().awayGoals()
            );
        }

        return new MatchEntity(
                domain.getId(),
                domain.getDate(),
                domain.getStatus(),
                teamEntityMapper.toEntity(domain.getHomeTeam()),
                teamEntityMapper.toEntity(domain.getAwayTeam()),
                scoreEmbeddable
        );
    }

    public Match toDomain(MatchEntity entity) {
        if (entity == null) {
            return null;
        }

        Score score = null;
        if (entity.getScore() != null) {
            score = new Score(
                    entity.getScore().getHomeGoals(),
                    entity.getScore().getAwayGoals()
            );
        }

        return new Match(
                entity.getId(),
                entity.getUtcDate(),
                teamEntityMapper.toDomain(entity.getHomeTeam()),
                teamEntityMapper.toDomain(entity.getAwayTeam()),
                score,
                entity.getStatus()
        );
    }
}