package dev.rangel.statkick.simulation.model;

public record TeamStrength(
        double homeAttack,
        double homeDefense,
        double awayAttack,
        double awayDefense
) {
    public static TeamStrength neutral() {
        return new TeamStrength(1.0, 1.0, 1.0, 1.0);
    }
}