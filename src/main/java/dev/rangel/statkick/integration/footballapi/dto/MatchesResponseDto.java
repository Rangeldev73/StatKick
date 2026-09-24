package dev.rangel.statkick.integration.footballapi.dto;

import java.util.List;

public record MatchesResponseDto(
        List<MatchDto> matches
) {}