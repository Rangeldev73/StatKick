package dev.rangel.statkick.simulation.engine;

import dev.rangel.statkick.simulation.model.MatchProbabilities;
import dev.rangel.statkick.simulation.model.SimulationMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class HistoricalBaselineCalculatorTest {

    private HistoricalBaselineCalculator calculator;
    private Instant asOf;

    @BeforeEach
    void setUp() {
        calculator = new HistoricalBaselineCalculator();
        asOf = Instant.parse("2024-05-10T15:00:00Z");
    }

    @Test
    @DisplayName("Should calculate exactly 1/3 frequencies for 3 distributed past matches")
    void shouldCalculateExactThirdFrequencies() {
        Instant past = asOf.minus(1, ChronoUnit.DAYS);
        List<SimulationMatch> matches = List.of(
                new SimulationMatch(1L, 2L, 2, 0, past),
                new SimulationMatch(3L, 4L, 1, 1, past),
                new SimulationMatch(5L, 6L, 0, 1, past)
        );

        MatchProbabilities probs = calculator.calculate(matches, asOf);

        assertThat(probs.homeWin()).isCloseTo(0.3333, within(0.0001));
        assertThat(probs.draw()).isCloseTo(0.3333, within(0.0001));
        assertThat(probs.awayWin()).isCloseTo(0.3333, within(0.0001));
    }

    @Test
    @DisplayName("Should return league mean prior (mu) when no matches exist strictly before asOf")
    void shouldReturnPriorWhenNoPastMatches() {
        List<SimulationMatch> matches = List.of(
                new SimulationMatch(1L, 2L, 2, 0, asOf),
                new SimulationMatch(3L, 4L, 1, 0, asOf.plus(1, ChronoUnit.DAYS))
        );

        MatchProbabilities probs = calculator.calculate(matches, asOf);

        assertThat(probs.homeWin()).isEqualTo(0.45);
        assertThat(probs.draw()).isEqualTo(0.28);
        assertThat(probs.awayWin()).isEqualTo(0.27);
    }
}