package dev.rangel.statkick.simulation.engine;

import dev.rangel.statkick.simulation.model.MatchOutcome;
import dev.rangel.statkick.simulation.model.MatchProbabilities;
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
        MatchProbabilities probs = new MatchProbabilities(0.5, 0.3, 0.2, 0.0, new double[0][0]);

        double rps = calculator.calculate(probs, MatchOutcome.HOME_WIN);

        assertThat(rps).isCloseTo(0.145, within(0.0001));
    }

    @Test
    @DisplayName("Should return 0.0 when prediction is perfect (100% certainty on actual outcome)")
    void shouldReturnZeroForPerfectPrediction() {
        MatchProbabilities probs = new MatchProbabilities(1.0, 0.0, 0.0, 0.0, new double[0][0]);

        double rps = calculator.calculate(probs, MatchOutcome.HOME_WIN);

        assertThat(rps).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should return 1.0 for worst case prediction (100% away win predicted, but home won)")
    void shouldReturnOneForWorstCasePrediction() {
        MatchProbabilities probs = new MatchProbabilities(0.0, 0.0, 1.0, 0.0, new double[0][0]);

        double rps = calculator.calculate(probs, MatchOutcome.HOME_WIN);

        assertThat(rps).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when inputs are null")
    void shouldThrowExceptionForNullInputs() {
        MatchProbabilities probs = new MatchProbabilities(0.5, 0.3, 0.2, 0.0, new double[0][0]);

        assertThatIllegalArgumentException().isThrownBy(() -> calculator.calculate(null, MatchOutcome.HOME_WIN));
        assertThatIllegalArgumentException().isThrownBy(() -> calculator.calculate(probs, null));
    }
}