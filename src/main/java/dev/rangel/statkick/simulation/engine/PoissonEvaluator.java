package dev.rangel.statkick.simulation.engine;

import dev.rangel.statkick.simulation.model.MatchProbabilities;

public class PoissonEvaluator {

    private static final int MAX_GOALS = 14;

    public MatchProbabilities evaluate(double lambdaHome, double lambdaAway) {
        if (!Double.isFinite(lambdaHome) || !Double.isFinite(lambdaAway) || lambdaHome < 0 || lambdaAway < 0) {
            throw new IllegalArgumentException("Lambdas must be finite non-negative numbers");
        }

        double[] homeProbs = calculatePoissonArray(lambdaHome);
        double[] awayProbs = calculatePoissonArray(lambdaAway);

        double[][] rawMatrix = new double[MAX_GOALS + 1][MAX_GOALS + 1];
        double totalMatrixProbability = 0.0;

        for (int h = 0; h <= MAX_GOALS; h++) {
            for (int a = 0; a <= MAX_GOALS; a++) {
                double prob = homeProbs[h] * awayProbs[a];
                rawMatrix[h][a] = prob;
                totalMatrixProbability += prob;
            }
        }

        double residual = Math.max(0.0, 1.0 - totalMatrixProbability);

        double normFactor = totalMatrixProbability > 0 ? 1.0 / totalMatrixProbability : 1.0;

        double[][] normalizedMatrix = new double[MAX_GOALS + 1][MAX_GOALS + 1];
        double homeWin = 0.0;
        double draw = 0.0;
        double awayWin = 0.0;

        for (int h = 0; h <= MAX_GOALS; h++) {
            for (int a = 0; a <= MAX_GOALS; a++) {
                double normProb = rawMatrix[h][a] * normFactor;
                normalizedMatrix[h][a] = normProb;

                if (h > a) {
                    homeWin += normProb;
                } else if (h == a) {
                    draw += normProb;
                } else {
                    awayWin += normProb;
                }
            }
        }

        return new MatchProbabilities(homeWin, draw, awayWin, residual, normalizedMatrix);
    }

    private double[] calculatePoissonArray(double lambda) {
        double[] probs = new double[MAX_GOALS + 1];
        if (lambda == 0.0) {
            probs[0] = 1.0;
            return probs;
        }

        probs[0] = Math.exp(-lambda);
        for (int k = 1; k <= MAX_GOALS; k++) {
            probs[k] = probs[k - 1] * (lambda / k);
        }
        return probs;
    }
}