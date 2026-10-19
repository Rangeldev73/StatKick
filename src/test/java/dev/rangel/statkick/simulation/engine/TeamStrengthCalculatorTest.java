package dev.rangel.statkick.simulation.engine;

import dev.rangel.statkick.simulation.model.LeagueMetrics;
import dev.rangel.statkick.simulation.model.SimulationMatch;
import dev.rangel.statkick.simulation.model.TeamStrength;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.within;

class TeamStrengthCalculatorTest {

    private TeamStrengthCalculator calculator;
    private Instant asOf;

    @BeforeEach
    void setUp() {
        calculator = new TeamStrengthCalculator();
        asOf = Instant.parse("2026-10-15T00:00:00Z");
    }

    @Test
    @DisplayName("Should ignore matches played exactly on or after asOf date")
    void shouldIgnoreMatchesOnOrAfterAsOf() {
        SimulationMatch exactAsOfMatch = new SimulationMatch(1L, 2L, 2, 0, asOf);
        SimulationMatch futureMatch = new SimulationMatch(1L, 2L, 2, 0, asOf.plusSeconds(3600));

        Optional<LeagueMetrics> result = calculator.calculate(List.of(exactAsOfMatch, futureMatch), asOf, 30.0, 5.0);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should process fractional days correctly using Duration")
    void shouldProcessFractionalDays() {
        SimulationMatch matchA = new SimulationMatch(1L, 2L, 3, 0, asOf.minus(12, ChronoUnit.HOURS));
        SimulationMatch matchB = new SimulationMatch(3L, 4L, 0, 0, asOf.minus(24, ChronoUnit.HOURS));

        Optional<LeagueMetrics> metricsOpt = calculator.calculate(List.of(matchA, matchB), asOf, 1.0, 0.0);
        assertThat(metricsOpt).isPresent();

        double muHome = metricsOpt.get().muHome();
        assertThat(muHome).isCloseTo(1.757, within(0.001));
    }

    @Test
    @DisplayName("Should calculate intermediate shrinkage correctly when k > 0 and weight is comparable")
    void shouldCalculateIntermediateShrinkage() {
        SimulationMatch matchA = new SimulationMatch(1L, 3L, 3, 0, asOf.minusSeconds(1));
        SimulationMatch matchB = new SimulationMatch(2L, 4L, 1, 0, asOf.minusSeconds(1));

        Optional<LeagueMetrics> metricsOpt = calculator.calculate(List.of(matchA, matchB), asOf, 30.0, 1.0);
        assertThat(metricsOpt).isPresent();

        TeamStrength strength1 = metricsOpt.get().getStrengthFor(1L);
        TeamStrength strength2 = metricsOpt.get().getStrengthFor(2L);

        assertThat(strength1.homeAttack()).isCloseTo(1.25, within(0.001));
        assertThat(strength2.homeAttack()).isCloseTo(0.75, within(0.001));
    }

    @Test
    @DisplayName("Should calculate home and away strengths separately")
    void shouldCalculateHomeAndAwaySeparately() {
        SimulationMatch m1 = new SimulationMatch(1L, 2L, 3, 1, asOf.minusSeconds(1));
        SimulationMatch m2 = new SimulationMatch(2L, 1L, 0, 2, asOf.minusSeconds(1));

        Optional<LeagueMetrics> metricsOpt = calculator.calculate(List.of(m1, m2), asOf, 30.0, 0.0);
        assertThat(metricsOpt).isPresent();

        TeamStrength t1 = metricsOpt.get().getStrengthFor(1L);

        assertThat(t1.homeAttack()).isCloseTo(2.0, within(0.001));
        assertThat(t1.awayAttack()).isCloseTo(1.333, within(0.001));
        assertThat(t1.homeDefense()).isCloseTo(0.667, within(0.001));
        assertThat(t1.awayDefense()).isCloseTo(0.0, within(0.001));
    }

    @Test
    @DisplayName("Should respect shrinkage limit when weight approaches infinity (W -> ∞)")
    void shouldCalculateCorrectShrinkageWhenWApproachesInfinity() {
        SimulationMatch matchA = new SimulationMatch(1L, 3L, 3, 0, asOf.minusSeconds(1));
        SimulationMatch matchB = new SimulationMatch(2L, 4L, 1, 0, asOf.minusSeconds(1));

        Optional<LeagueMetrics> metricsOpt = calculator.calculate(List.of(matchA, matchB), asOf, 30.0, 0.0);
        assertThat(metricsOpt).isPresent();

        TeamStrength strength1 = metricsOpt.get().getStrengthFor(1L);
        assertThat(strength1.homeAttack()).isCloseTo(1.5, within(0.001));

        TeamStrength strength2 = metricsOpt.get().getStrengthFor(2L);
        assertThat(strength2.homeAttack()).isCloseTo(0.5, within(0.001));
    }

    @Test
    @DisplayName("Should respect shrinkage limit when weight approaches zero (W -> 0)")
    void shouldShrinkToNeutralWhenWApproachesZero() {
        SimulationMatch oldMatch = new SimulationMatch(1L, 3L, 5, 0, asOf.minus(3000, ChronoUnit.DAYS));
        SimulationMatch recentMatch = new SimulationMatch(2L, 4L, 1, 0, asOf.minusSeconds(1));

        Optional<LeagueMetrics> metricsOpt = calculator.calculate(List.of(oldMatch, recentMatch), asOf, 30.0, 5.0);
        assertThat(metricsOpt).isPresent();

        TeamStrength strength1 = metricsOpt.get().getStrengthFor(1L);
        assertThat(strength1.homeAttack()).isCloseTo(1.0, within(0.01));
    }

    @Test
    @DisplayName("Should never output NaN and output finite strengths even in extreme edge cases (k=0, W=0, mu=0)")
    void shouldOutputFiniteValuesInExtremeConditions() {
        SimulationMatch match = new SimulationMatch(1L, 2L, 0, 0, asOf.minusSeconds(1));

        Optional<LeagueMetrics> metricsOpt = calculator.calculate(List.of(match), asOf, 30.0, 0.0);
        assertThat(metricsOpt).isPresent();

        TeamStrength strength1 = metricsOpt.get().getStrengthFor(1L);

        assertThat(Double.isFinite(strength1.homeAttack())).isTrue();
        assertThat(Double.isFinite(strength1.homeDefense())).isTrue();
        assertThat(Double.isFinite(strength1.awayAttack())).isTrue();
        assertThat(Double.isFinite(strength1.awayDefense())).isTrue();
    }

    @Test
    @DisplayName("Should throw exceptions for invalid parameters")
    void shouldValidateParameters() {
        assertThatIllegalArgumentException().isThrownBy(() -> calculator.calculate(List.of(), null, 30.0, 5.0));
        assertThatIllegalArgumentException().isThrownBy(() -> calculator.calculate(List.of(), asOf, 0.0, 5.0));
        assertThatIllegalArgumentException().isThrownBy(() -> calculator.calculate(List.of(), asOf, -10.0, 5.0));
        assertThatIllegalArgumentException().isThrownBy(() -> calculator.calculate(List.of(), asOf, 30.0, -1.0));
    }
}