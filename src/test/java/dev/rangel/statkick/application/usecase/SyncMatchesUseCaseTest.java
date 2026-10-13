package dev.rangel.statkick.application.usecase;

import dev.rangel.statkick.application.service.MatchPersistenceService;
import dev.rangel.statkick.domain.model.Match;
import dev.rangel.statkick.domain.model.MatchStatus;
import dev.rangel.statkick.domain.model.Team;
import dev.rangel.statkick.domain.port.FootballMatchPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncMatchesUseCaseTest {

    @Mock
    private FootballMatchPort footballMatchPort;

    @Mock
    private MatchPersistenceService matchPersistenceService;

    @InjectMocks
    private SyncMatchesUseCase syncMatchesUseCase;

    @Captor
    private ArgumentCaptor<Set<Team>> teamsCaptor;

    @Captor
    private ArgumentCaptor<List<Match>> matchesCaptor;

    @Test
    @DisplayName("Should deduplicate teams across multiple matches before persisting")
    void shouldDeduplicateTeamsAndPersist() {
        Team arsenal1 = new Team(57L, "Arsenal FC");
        Team chelsea = new Team(61L, "Chelsea FC");

        Team arsenal2 = new Team(57L, "Arsenal FC");
        Team spurs = new Team(73L, "Tottenham Hotspur FC");

        Match match1 = new Match(101L, Instant.now(), arsenal1, chelsea, null, MatchStatus.SCHEDULED);
        Match match2 = new Match(102L, Instant.now(), arsenal2, spurs, null, MatchStatus.SCHEDULED);

        when(footballMatchPort.fetchMatches("PL")).thenReturn(List.of(match1, match2));

        syncMatchesUseCase.sync("PL");

        verify(matchPersistenceService).saveAtomically(teamsCaptor.capture(), matchesCaptor.capture());

        Set<Team> persistedTeams = teamsCaptor.getValue();
        List<Match> persistedMatches = matchesCaptor.getValue();

        assertThat(persistedTeams).hasSize(3);
        assertThat(persistedMatches).hasSize(2);
    }
}