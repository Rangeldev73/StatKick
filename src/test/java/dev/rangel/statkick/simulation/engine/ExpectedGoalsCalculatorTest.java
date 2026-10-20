package dev.rangel.statkick.simulation.engine;

import dev.rangel.statkick.simulation.model.LeagueMetrics;
import dev.rangel.statkick.simulation.model.TeamStrength;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ExpectedGoalsCalculatorTest {

    private final ExpectedGoalsCalculator calculator = new ExpectedGoalsCalculator();

    @Test
    @DisplayName("Should calculate expected goals multiplying league average by attack and opponent defense")
    void shouldCalculateLambdasCorrectly() {
        LeagueMetrics metrics = new LeagueMetrics(1.5, 1.2, Map.of());
        TeamStrength home = new TeamStrength(1.2, 0.8, 1.0, 1.0);
        TeamStrength away = new TeamStrength(1.0, 1.0, 1.1, 1.3);

        ExpectedGoalsCalculator.ExpectedGoals result = calculator.calculate(metrics, home, away);

        // lambdaHome = 1.5 (muHome) * 1.2 (HAS) * 1.3 (ADS) = 2.34
        assertThat(result.lambdaHome()).isCloseTo(2.34, within(0.001));

        // lambdaAway = 1.2 (muAway) * 1.1 (AAS) * 0.8 (HDS) = 1.056
        assertThat(result.lambdaAway()).isCloseTo(1.056, within(0.001));
    }

    @Test
    @DisplayName("Should return league average (mu) when both teams have neutral strengths (1.0)")
    void shouldReturnMuWhenStrengthsAreNeutral() {
        LeagueMetrics metrics = new LeagueMetrics(1.6, 1.1, Map.of());
        TeamStrength home = new TeamStrength(1.0, 1.0, 1.0, 1.0);
        TeamStrength away = new TeamStrength(1.0, 1.0, 1.0, 1.0);

        ExpectedGoalsCalculator.ExpectedGoals result = calculator.calculate(metrics, home, away);

        assertThat(result.lambdaHome()).isCloseTo(1.6, within(0.001));
        assertThat(result.lambdaAway()).isCloseTo(1.1, within(0.001));
    }
}