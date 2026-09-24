package dev.rangel.statkick.integration.footballapi.mapper;

import dev.rangel.statkick.domain.model.Match;
import dev.rangel.statkick.domain.model.MatchStatus;
import dev.rangel.statkick.domain.model.Score;
import dev.rangel.statkick.domain.model.Team;
import dev.rangel.statkick.integration.footballapi.dto.FullTimeScoreDto;
import dev.rangel.statkick.integration.footballapi.dto.MatchDto;
import dev.rangel.statkick.integration.footballapi.dto.ScoreDto;
import dev.rangel.statkick.integration.footballapi.dto.TeamDto;
import org.springframework.stereotype.Component;

@Component
public class MatchMapper {

    public Match toDomain(MatchDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("MatchDto cannot be null");
        }

        Team homeTeam = toTeamDomain(dto.homeTeam());
        Team awayTeam = toTeamDomain(dto.awayTeam());
        Score score = toScoreDomain(dto.score());
        MatchStatus status = toStatusDomain(dto.status());

        return new Match(
                dto.id(),
                dto.utcDate(),
                homeTeam,
                awayTeam,
                score,
                status
        );
    }

    private Team toTeamDomain(TeamDto dto) {
        if (dto == null) {
            return null;
        }
        return new Team(dto.id(), dto.name());
    }

    private Score toScoreDomain(ScoreDto dto) {
        if (dto == null || dto.fullTime() == null) {
            return new Score(null, null);
        }

        FullTimeScoreDto fullTime = dto.fullTime();
        return new Score(fullTime.home(), fullTime.away());
    }

    private MatchStatus toStatusDomain(String statusRaw) {
        if (statusRaw == null) {
            return MatchStatus.OTHER;
        }

        return switch (statusRaw.toUpperCase()) {
            case "FINISHED" -> MatchStatus.FINISHED;
            case "SCHEDULED", "TIMED" -> MatchStatus.SCHEDULED;
            default -> MatchStatus.OTHER;
        };
    }
}