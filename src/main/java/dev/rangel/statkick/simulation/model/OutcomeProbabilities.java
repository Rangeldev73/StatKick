package dev.rangel.statkick.simulation.model;

public record OutcomeProbabilities(
        double homeWin,
        double draw,
        double awayWin
) {
    public OutcomeProbabilities {
        if (!Double.isFinite(homeWin) || !Double.isFinite(draw) || !Double.isFinite(awayWin)) {
            throw new IllegalArgumentException("Probabilities must be finite numbers");
        }
    }
}