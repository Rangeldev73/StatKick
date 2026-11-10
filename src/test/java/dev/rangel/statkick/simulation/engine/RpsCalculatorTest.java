package dev.rangel.statkick.simulation.engine;

import dev.rangel.statkick.simulation.model.MatchOutcome;
import dev.rangel.statkick.simulation.model.OutcomeProbabilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.within;

class RpsCalculatorTest {

    private RpsCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new RpsCalculator();
    }

    @Test
    @DisplayName("Should match manual calculation anchor for forecast (0.5, 0.3, 0.2) and HOME_WIN -> 0.145")
    void shouldCalculateCorrectRpsForAnchorCase() {
        OutcomeProbabilities probs = new OutcomeProbabilities(0.5, 0.3, 0.2);

        double rps = calculator.calculate(probs, MatchOutcome.HOME_WIN);

        assertThat(rps).isCloseTo(0.145, within(0.0001));
    }

    @Test
    @DisplayName("Should return 0.0 when prediction is perfect (100% certainty on actual outcome)")
    void shouldReturnZeroForPerfectPrediction() {
        OutcomeProbabilities probs = new OutcomeProbabilities(1.0, 0.0, 0.0);

        double rps = calculator.calculate(probs, MatchOutcome.HOME_WIN);

        assertThat(rps).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should return 1.0 for worst case prediction (100% away win predicted, but home won)")
    void shouldReturnOneForWorstCasePrediction() {
        OutcomeProbabilities probs = new OutcomeProbabilities(0.0, 0.0, 1.0);

        double rps = calculator.calculate(probs, MatchOutcome.HOME_WIN);

        assertThat(rps).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Should prove ordering penalty: predicting DRAW (0,1,0) when HOME_WIN happens -> 0.5 RPS")
    void shouldCalculateRpsForDrawPredictionWhenHomeWins() {
        OutcomeProbabilities probs = new OutcomeProbabilities(0.0, 1.0, 0.0);
        double rps = calculator.calculate(probs, MatchOutcome.HOME_WIN);

        assertThat(rps).isEqualTo(0.5);
    }

    @Test
    @DisplayName("Should match manual calculation anchor for forecast (0.5, 0.3, 0.2) and AWAY_WIN -> 0.445")
    void shouldCalculateCorrectRpsForAwayWinAnchor() {
        OutcomeProbabilities probs = new OutcomeProbabilities(0.5, 0.3, 0.2);
        double rps = calculator.calculate(probs, MatchOutcome.AWAY_WIN);

        assertThat(rps).isCloseTo(0.445, within(0.0001));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when inputs are null")
    void shouldThrowExceptionForNullInputs() {
        OutcomeProbabilities probs = new OutcomeProbabilities(0.5, 0.3, 0.2);

        assertThatIllegalArgumentException().isThrownBy(() -> calculator.calculate(null, MatchOutcome.HOME_WIN));
        assertThatIllegalArgumentException().isThrownBy(() -> calculator.calculate(probs, null));
    }
}