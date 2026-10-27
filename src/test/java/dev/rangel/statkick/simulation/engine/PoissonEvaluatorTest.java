package dev.rangel.statkick.simulation.engine;

import dev.rangel.statkick.simulation.model.MatchProbabilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.within;

class PoissonEvaluatorTest {

    private PoissonEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new PoissonEvaluator();
    }

    @Test
    @DisplayName("Should calculate correct probabilities for lambda = 1.0 anchor values")
    void shouldCalculateCorrectProbabilitiesForLambdaOne() {
        MatchProbabilities probs = evaluator.evaluate(1.0, 1.0);

        assertThat(probs.getProbability(0, 0)).isCloseTo(0.1353, within(0.001));
        assertThat(probs.draw()).isCloseTo(0.3085, within(0.001));
        assertThat(probs.homeWin()).isCloseTo(0.3458, within(0.001));
        assertThat(probs.awayWin()).isCloseTo(0.3458, within(0.001));
    }

    @Test
    @DisplayName("Should verify that normalized outcomes sum strictly to 1.0")
    void shouldVerifyNormalizedSumEqualsOne() {
        MatchProbabilities probs = evaluator.evaluate(3.5, 3.5);

        double sum = probs.homeWin() + probs.draw() + probs.awayWin();
        assertThat(sum).isCloseTo(1.0, within(1e-12));
    }

    @Test
    @DisplayName("Should verify that residual for lambda = 3.5 is below strict tolerance (1e-4) with MAX_GOALS = 14")
    void shouldVerifyResidualTolerance() {
        MatchProbabilities probs = evaluator.evaluate(3.5, 3.5);

        assertThat(probs.residual()).isLessThan(1e-4);
    }

    @Test
    @DisplayName("Should produce symmetric outcomes when home and away lambdas are equal")
    void shouldBeSymmetricWhenLambdasAreEqual() {
        MatchProbabilities probs = evaluator.evaluate(1.8, 1.8);

        assertThat(probs.homeWin()).isCloseTo(probs.awayWin(), within(1e-14));
    }

    @Test
    @DisplayName("Should strictly increase homeWin probability when lambdaHome increases")
    void shouldRespectMonotonicityWhenLambdaHomeIncreases() {
        MatchProbabilities base = evaluator.evaluate(1.2, 1.2);
        MatchProbabilities higherHome = evaluator.evaluate(2.0, 1.2);

        assertThat(higherHome.homeWin()).isGreaterThan(base.homeWin());
        assertThat(higherHome.awayWin()).isLessThan(base.awayWin());
    }

    @Test
    @DisplayName("Should handle degenerate case lambda = 0 gracefully")
    void shouldHandleDegenerateLambdaZero() {
        MatchProbabilities probs = evaluator.evaluate(0.0, 0.0);

        assertThat(probs.getProbability(0, 0)).isEqualTo(1.0);
        assertThat(probs.draw()).isEqualTo(1.0);
        assertThat(probs.homeWin()).isEqualTo(0.0);
        assertThat(probs.awayWin()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for negative lambdas, NaN, and Infinity")
    void shouldThrowExceptionForInvalidLambdas() {
        assertThatIllegalArgumentException().isThrownBy(() -> evaluator.evaluate(-1.0, 1.0));
        assertThatIllegalArgumentException().isThrownBy(() -> evaluator.evaluate(Double.NaN, 1.0));
        assertThatIllegalArgumentException().isThrownBy(() -> evaluator.evaluate(Double.POSITIVE_INFINITY, 1.0));
    }
}