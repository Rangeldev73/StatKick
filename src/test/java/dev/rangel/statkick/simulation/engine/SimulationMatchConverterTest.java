package dev.rangel.statkick.simulation.engine;

import dev.rangel.statkick.domain.model.Match;
import dev.rangel.statkick.domain.model.MatchStatus;
import dev.rangel.statkick.domain.model.Score;
import dev.rangel.statkick.domain.model.Team;
import dev.rangel.statkick.simulation.model.SimulationMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SimulationMatchConverterTest {

    private SimulationMatchConverter converter;
    private Instant now;
    private Team teamHome;
    private Team teamAway;

    @BeforeEach
    void setUp() {
        converter = new SimulationMatchConverter();
        now = Instant.now();
        teamHome = new Team(1L, "Home Team");
        teamAway = new Team(2L, "Away Team");
    }

    @Test
    @DisplayName("Should discard invalid matches and return clean matches with discard count")
    void shouldDiscardInvalidMatches() {
        Match valid = createMatch(teamHome, teamAway, new Score(2, 1), true, now);
        Match scheduled = createMatch(teamHome, teamAway, new Score(0, 0), false, now);
        Match noScore = createMatch(teamHome, teamAway, null, true, now);
        Match nullScoreValues = createMatch(teamHome, teamAway, new Score(null, null), true, now);
        Match nullTeam = createMatch(null, teamAway, new Score(1, 1), true, now);
        Match nullDate = createMatch(teamHome, teamAway, new Score(1, 1), true, null);
        Match nullElement = null;

        List<Match> input = Arrays.asList(valid, scheduled, noScore, nullScoreValues, nullTeam, nullDate, nullElement);

        SimulationMatchConverter.ConversionResult result = converter.convert(input);

        assertThat(result.validMatches()).hasSize(1);
        assertThat(result.discardedCount()).isEqualTo(6);

        SimulationMatch simMatch = result.validMatches().get(0);
        assertThat(simMatch.homeTeamId()).isEqualTo(1L);
        assertThat(simMatch.awayTeamId()).isEqualTo(2L);
        assertThat(simMatch.homeGoals()).isEqualTo(2);
        assertThat(simMatch.awayGoals()).isEqualTo(1);
    }

    private Match createMatch(Team home, Team away, Score score, boolean isFinished, Instant date) {
        MatchStatus status = isFinished ? MatchStatus.FINISHED : MatchStatus.SCHEDULED;
        return new Match(100L, date, home, away, score, status);
    }
}