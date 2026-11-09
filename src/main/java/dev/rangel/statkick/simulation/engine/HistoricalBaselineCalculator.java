package dev.rangel.statkick.simulation.engine;

import dev.rangel.statkick.simulation.model.MatchProbabilities;
import dev.rangel.statkick.simulation.model.SimulationMatch;

import java.time.Instant;
import java.util.List;

public class HistoricalBaselineCalculator {

    private static final double MU_HOME_WIN = 0.45;
    private static final double MU_DRAW = 0.28;
    private static final double MU_AWAY_WIN = 0.27;

    public MatchProbabilities calculate(List<SimulationMatch> matches, Instant asOf) {
        if (matches == null || asOf == null) {
            throw new IllegalArgumentException("Matches list and asOf date cannot be null");
        }

        long homeWins = 0;
        long draws = 0;
        long awayWins = 0;
        long validMatchesCount = 0;

        for (SimulationMatch match : matches) {
            if (match.date().isBefore(asOf)) {
                validMatchesCount++;
                if (match.homeGoals() > match.awayGoals()) {
                    homeWins++;
                } else if (match.homeGoals() == match.awayGoals()) {
                    draws++;
                } else {
                    awayWins++;
                }
            }
        }

        if (validMatchesCount == 0) {
            return new MatchProbabilities(MU_HOME_WIN, MU_DRAW, MU_AWAY_WIN, 0.0, null);
        }

        double freqHome = (double) homeWins / validMatchesCount;
        double freqDraw = (double) draws / validMatchesCount;
        double freqAway = (double) awayWins / validMatchesCount;

        return new MatchProbabilities(freqHome, freqDraw, freqAway, 0.0, null);
    }
}