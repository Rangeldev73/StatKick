package dev.rangel.statkick.simulation.engine;

import dev.rangel.statkick.simulation.model.LeagueMetrics;
import dev.rangel.statkick.simulation.model.SimulationMatch;
import dev.rangel.statkick.simulation.model.TeamStrength;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TeamStrengthCalculator {

    public Optional<LeagueMetrics> calculate(List<SimulationMatch> matches, Instant asOf, double halfLifeDays, double k) {
        if (asOf == null) throw new IllegalArgumentException("asOf cannot be null");
        if (halfLifeDays <= 0) throw new IllegalArgumentException("halfLifeDays must be > 0");
        if (k < 0) throw new IllegalArgumentException("k must be >= 0");
        if (matches == null || matches.isEmpty()) return Optional.empty();

        double totalWeight = 0.0;
        double weightedHomeGoals = 0.0;
        double weightedAwayGoals = 0.0;

        Map<Long, Double> homeWeight = new HashMap<>();
        Map<Long, Double> awayWeight = new HashMap<>();

        Map<Long, Double> homeGoalsScored = new HashMap<>();
        Map<Long, Double> homeGoalsConceded = new HashMap<>();
        Map<Long, Double> awayGoalsScored = new HashMap<>();
        Map<Long, Double> awayGoalsConceded = new HashMap<>();

        for (SimulationMatch match : matches) {
            if (!match.date().isBefore(asOf)) {
                continue;
            }

            double daysAgo = Duration.between(match.date(), asOf).toMillis() / 86400000.0;
            double weight = Math.pow(2.0, -daysAgo / halfLifeDays);

            totalWeight += weight;
            weightedHomeGoals += match.homeGoals() * weight;
            weightedAwayGoals += match.awayGoals() * weight;

            long home = match.homeTeamId();
            long away = match.awayTeamId();

            homeWeight.merge(home, weight, Double::sum);
            awayWeight.merge(away, weight, Double::sum);

            homeGoalsScored.merge(home, match.homeGoals() * weight, Double::sum);
            homeGoalsConceded.merge(home, match.awayGoals() * weight, Double::sum);

            awayGoalsScored.merge(away, match.awayGoals() * weight, Double::sum);
            awayGoalsConceded.merge(away, match.homeGoals() * weight, Double::sum);
        }

        if (totalWeight == 0.0) {
            return Optional.empty();
        }

        double muHome = weightedHomeGoals / totalWeight;
        double muAway = weightedAwayGoals / totalWeight;

        Map<Long, TeamStrength> strengths = new HashMap<>();

        for (Long teamId : extractAllTeams(homeWeight, awayWeight)) {
            double wHome = homeWeight.getOrDefault(teamId, 0.0);
            double wAway = awayWeight.getOrDefault(teamId, 0.0);

            double rawHAS = (wHome > 0 && muHome > 0) ? (homeGoalsScored.get(teamId) / muHome) / wHome : 1.0;
            double rawHDS = (wHome > 0 && muAway > 0) ? (homeGoalsConceded.get(teamId) / muAway) / wHome : 1.0;

            double rawAAS = (wAway > 0 && muAway > 0) ? (awayGoalsScored.get(teamId) / muAway) / wAway : 1.0;
            double rawADS = (wAway > 0 && muHome > 0) ? (awayGoalsConceded.get(teamId) / muHome) / wAway : 1.0;

            double has = applyShrinkage(rawHAS, wHome, k);
            double hds = applyShrinkage(rawHDS, wHome, k);
            double aas = applyShrinkage(rawAAS, wAway, k);
            double ads = applyShrinkage(rawADS, wAway, k);

            strengths.put(teamId, new TeamStrength(has, hds, aas, ads));
        }

        return Optional.of(new LeagueMetrics(muHome, muAway, strengths));
    }

    private double applyShrinkage(double rawValue, double weight, double k) {
        if (weight + k == 0.0) return 1.0;
        return (weight * rawValue + k * 1.0) / (weight + k);
    }

    private Iterable<Long> extractAllTeams(Map<Long, Double> home, Map<Long, Double> away) {
        java.util.Set<Long> teams = new java.util.HashSet<>(home.keySet());
        teams.addAll(away.keySet());
        return teams;
    }
}