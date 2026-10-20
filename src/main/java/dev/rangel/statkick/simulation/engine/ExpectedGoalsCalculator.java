package dev.rangel.statkick.simulation.engine;

import dev.rangel.statkick.simulation.model.LeagueMetrics;
import dev.rangel.statkick.simulation.model.TeamStrength;

public class ExpectedGoalsCalculator {

    public record ExpectedGoals(double lambdaHome, double lambdaAway) {}

    public ExpectedGoals calculate(LeagueMetrics metrics, TeamStrength homeTeam, TeamStrength awayTeam) {
        double lambdaHome = metrics.muHome() * homeTeam.homeAttack() * awayTeam.awayDefense();
        double lambdaAway = metrics.muAway() * awayTeam.awayAttack() * homeTeam.homeDefense();

        return new ExpectedGoals(lambdaHome, lambdaAway);
    }
}