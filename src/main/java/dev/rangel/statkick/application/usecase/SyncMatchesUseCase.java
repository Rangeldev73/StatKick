package dev.rangel.statkick.application.usecase;

import dev.rangel.statkick.application.service.MatchPersistenceService;
import dev.rangel.statkick.domain.model.Match;
import dev.rangel.statkick.domain.model.Team;
import dev.rangel.statkick.domain.port.FootballMatchPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SyncMatchesUseCase {

    private final FootballMatchPort footballMatchPort;
    private final MatchPersistenceService matchPersistenceService;

    public void sync(String competitionCode) {
        List<Match> matches = footballMatchPort.fetchMatches(competitionCode);

        if (matches == null || matches.isEmpty()) {
            return;
        }

        Set<Team> uniqueTeams = matches.stream()
                .flatMap(match -> Stream.of(match.getHomeTeam(), match.getAwayTeam()))
                .collect(Collectors.toSet());

        matchPersistenceService.saveAtomically(uniqueTeams, matches);
    }
}