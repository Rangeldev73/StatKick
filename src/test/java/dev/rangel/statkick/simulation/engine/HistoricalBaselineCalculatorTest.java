package dev.rangel.statkick.simulation.engine;

import dev.rangel.statkick.simulation.model.OutcomeProbabilities;
import dev.rangel.statkick.simulation.model.SimulationMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

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

        Optional<OutcomeProbabilities> probsOpt = calculator.calculate(matches, asOf);

        assertThat(probsOpt).isPresent();
        assertThat(probsOpt.get().homeWin()).isCloseTo(0.3333, within(0.0001));
        assertThat(probsOpt.get().draw()).isCloseTo(0.3333, within(0.0001));
        assertThat(probsOpt.get().awayWin()).isCloseTo(0.3333, within(0.0001));
    }

    @Test
    @DisplayName("Should return Optional.empty when no matches exist strictly before asOf")
    void shouldReturnEmptyWhenNoPastMatches() {
        List<SimulationMatch> matches = List.of(
                new SimulationMatch(1L, 2L, 2, 0, asOf),
                new SimulationMatch(3L, 4L, 1, 0, asOf.plus(1, ChronoUnit.DAYS))
        );

        Optional<OutcomeProbabilities> probsOpt = calculator.calculate(matches, asOf);

        assertThat(probsOpt).isEmpty();
    }

    @Test
    @DisplayName("Should ignore future matches and exact asOf matches when mixed with past matches")
    void shouldIgnoreFutureAndExactAsOfMatches() {
        Instant past = asOf.minus(2, ChronoUnit.DAYS);
        List<SimulationMatch> matches = List.of(
                new SimulationMatch(1L, 2L, 3, 0, past), // Past (Home Win)
                new SimulationMatch(3L, 4L, 2, 0, asOf), // Exact asOf (Ignored)
                new SimulationMatch(5L, 6L, 0, 2, asOf.plus(1, ChronoUnit.DAYS)) // Future (Ignored)
        );

        Optional<OutcomeProbabilities> probsOpt = calculator.calculate(matches, asOf);

        assertThat(probsOpt).isPresent();
        assertThat(probsOpt.get().homeWin()).isEqualTo(1.0);
        assertThat(probsOpt.get().draw()).isEqualTo(0.0);
        assertThat(probsOpt.get().awayWin()).isEqualTo(0.0);
    }
}