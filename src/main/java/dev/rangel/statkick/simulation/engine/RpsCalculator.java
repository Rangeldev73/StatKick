package dev.rangel.statkick.simulation.engine;

import dev.rangel.statkick.simulation.model.MatchOutcome;
import dev.rangel.statkick.simulation.model.MatchProbabilities;

public class RpsCalculator {

    public double calculate(MatchProbabilities probs, MatchOutcome outcome) {
        if (probs == null || outcome == null) {
            throw new IllegalArgumentException("Probabilities and outcome cannot be null");
        }

        double pHome = probs.homeWin();
        double pDraw = probs.draw();

        double e1 = (outcome == MatchOutcome.HOME_WIN) ? 1.0 : 0.0;
        double e2 = (outcome == MatchOutcome.HOME_WIN || outcome == MatchOutcome.DRAW) ? 1.0 : 0.0;

        double diff1 = pHome - e1;
        double diff2 = (pHome + pDraw) - e2;

        return 0.5 * (diff1 * diff1 + diff2 * diff2);
    }
}