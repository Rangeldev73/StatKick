package dev.rangel.statkick.integration.footballapi.dto;

import java.time.Instant;

public record MatchDto(
        Long id,
        Instant utcDate,
        String status,
        Integer matchday,
        TeamDto homeTeam,
        TeamDto awayTeam,
        ScoreDto score
) {}