package dev.rangel.statkick.simulation.engine;

import dev.rangel.statkick.simulation.model.OutcomeProbabilities;
import dev.rangel.statkick.simulation.model.SimulationMatch;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class HistoricalBaselineCalculator {

    public Optional<OutcomeProbabilities> calculate(List<SimulationMatch> matches, Instant asOf) {
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
            return Optional.empty();
        }

        double freqHome = (double) homeWins / validMatchesCount;
        double freqDraw = (double) draws / validMatchesCount;
        double freqAway = (double) awayWins / validMatchesCount;

        return Optional.of(new OutcomeProbabilities(freqHome, freqDraw, freqAway));
    }
}