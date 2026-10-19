package dev.rangel.statkick.simulation.model;

import java.util.Map;

public record LeagueMetrics(
        double muHome,
        double muAway,
        Map<Long, TeamStrength> teamStrengths
) {
    public TeamStrength getStrengthFor(long teamId) {
        return teamStrengths.getOrDefault(teamId, TeamStrength.neutral());
    }
}