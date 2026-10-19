package dev.rangel.statkick.simulation.model;

import java.time.Instant;

public record SimulationMatch(
        long homeTeamId,
        long awayTeamId,
        int homeGoals,
        int awayGoals,
        Instant date
) {}