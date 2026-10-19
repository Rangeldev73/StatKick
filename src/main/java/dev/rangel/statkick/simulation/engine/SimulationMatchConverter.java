package dev.rangel.statkick.simulation.engine;

import dev.rangel.statkick.domain.model.Match;
import dev.rangel.statkick.simulation.model.SimulationMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class SimulationMatchConverter {

    private static final Logger log = LoggerFactory.getLogger(SimulationMatchConverter.class);

    public record ConversionResult(List<SimulationMatch> validMatches, int discardedCount) {}

    public ConversionResult convert(List<Match> matches) {
        if (matches == null) return new ConversionResult(List.of(), 0);

        List<SimulationMatch> validMatches = matches.stream()
                .filter(this::hasValidResult)
                .map(m -> new SimulationMatch(
                        m.getHomeTeam().getId(),
                        m.getAwayTeam().getId(),
                        m.getScore().homeGoals(),
                        m.getScore().awayGoals(),
                        m.getDate()
                ))
                .collect(Collectors.toList());

        int discarded = matches.size() - validMatches.size();
        if (discarded > 0) {
            log.warn("Discarded {} matches without a usable result for simulation.", discarded);
        }

        return new ConversionResult(validMatches, discarded);
    }

    private boolean hasValidResult(Match match) {
        return match != null &&
                match.isFinished() &&
                match.getDate() != null &&
                match.getHomeTeam() != null && match.getHomeTeam().getId() != null &&
                match.getAwayTeam() != null && match.getAwayTeam().getId() != null &&
                match.getScore() != null &&
                match.getScore().homeGoals() != null &&
                match.getScore().awayGoals() != null;
    }
}