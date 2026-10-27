package dev.rangel.statkick.simulation.model;

import java.util.Arrays;

public record MatchProbabilities(
        double homeWin,
        double draw,
        double awayWin,
        double residual,
        double[][] scoreMatrix
) {
    public MatchProbabilities {
        if (scoreMatrix != null) {
            double[][] copy = new double[scoreMatrix.length][];
            for (int i = 0; i < scoreMatrix.length; i++) {
                copy[i] = Arrays.copyOf(scoreMatrix[i], scoreMatrix[i].length);
            }
            scoreMatrix = copy;
        }
    }

    @Override
    public double[][] scoreMatrix() {
        if (scoreMatrix == null) return null;
        double[][] copy = new double[scoreMatrix.length][];
        for (int i = 0; i < scoreMatrix.length; i++) {
            copy[i] = Arrays.copyOf(scoreMatrix[i], scoreMatrix[i].length);
        }
        return copy;
    }

    public double getProbability(int homeGoals, int awayGoals) {
        if (homeGoals < 0 || homeGoals >= scoreMatrix.length || awayGoals < 0 || awayGoals >= scoreMatrix[0].length) {
            return 0.0;
        }
        return scoreMatrix[homeGoals][awayGoals];
    }
}