package dev.rangel.statkick.integration.footballapi.mapper;

import dev.rangel.statkick.domain.model.Match;
import dev.rangel.statkick.domain.model.MatchStatus;
import dev.rangel.statkick.integration.footballapi.dto.FullTimeScoreDto;
import dev.rangel.statkick.integration.footballapi.dto.MatchDto;
import dev.rangel.statkick.integration.footballapi.dto.ScoreDto;
import dev.rangel.statkick.integration.footballapi.dto.TeamDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class MatchMapperTest {

    private MatchMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new MatchMapper();
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when MatchDto is null (Fail-Fast)")
    void shouldThrowExceptionWhenDtoIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> mapper.toDomain(null)
        );

        assertEquals("MatchDto cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should map status TIMED to SCHEDULED")
    void shouldMapTimedStatusToScheduled() {
        MatchDto dto = createMatchDtoWithStatus("TIMED");

        Match match = mapper.toDomain(dto);

        assertNotNull(match);
        assertEquals(MatchStatus.SCHEDULED, match.getStatus());
    }

    @Test
    @DisplayName("Should map unknown status like POSTPONED to OTHER")
    void shouldMapUnknownStatusToOther() {
        MatchDto dto = createMatchDtoWithStatus("POSTPONED");

        Match match = mapper.toDomain(dto);

        assertNotNull(match);
        assertEquals(MatchStatus.OTHER, match.getStatus());
    }

    @Test
    @DisplayName("Should map null status to OTHER")
    void shouldMapNullStatusToOther() {
        MatchDto dto = createMatchDtoWithStatus(null);

        Match match = mapper.toDomain(dto);

        assertNotNull(match);
        assertEquals(MatchStatus.OTHER, match.getStatus());
    }

    @Test
    @DisplayName("Should map Score to Score(null, null) without NullPointerException when score or fullTime is null")
    void shouldHandleNullFullTimeScoreWithoutNpe() {
        MatchDto dtoWithNullScore = new MatchDto(
                1L,
                Instant.now(),
                "SCHEDULED",
                1,
                new TeamDto(10L, "Arsenal"),
                new TeamDto(20L, "Chelsea"),
                null
        );

        MatchDto dtoWithNullFullTime = new MatchDto(
                2L,
                Instant.now(),
                "SCHEDULED",
                1,
                new TeamDto(10L, "Arsenal"),
                new TeamDto(20L, "Chelsea"),
                new ScoreDto(null)
        );

        Match match1 = mapper.toDomain(dtoWithNullScore);
        Match match2 = mapper.toDomain(dtoWithNullFullTime);

        assertNotNull(match1.getScore());
        assertNull(match1.getScore().homeGoals());
        assertNull(match1.getScore().awayGoals());

        assertNotNull(match2.getScore());
        assertNull(match2.getScore().homeGoals());
        assertNull(match2.getScore().awayGoals());
    }

    @Test
    @DisplayName("Should correctly map a finished match with valid fullTime score")
    void shouldMapFinishedMatchWithValidScore() {
        MatchDto dto = new MatchDto(
                100L,
                Instant.parse("2026-09-25T12:00:00Z"),
                "FINISHED",
                5,
                new TeamDto(1L, "Real Madrid"),
                new TeamDto(2L, "Barcelona"),
                new ScoreDto(new FullTimeScoreDto(3, 1))
        );

        Match match = mapper.toDomain(dto);

        assertNotNull(match);
        assertEquals(100L, match.getId());
        assertEquals(MatchStatus.FINISHED, match.getStatus());
        assertTrue(match.isFinished());
        assertEquals("Real Madrid", match.getHomeTeam().getName());
        assertEquals("Barcelona", match.getAwayTeam().getName());
        assertEquals(3, match.getScore().homeGoals());
        assertEquals(1, match.getScore().awayGoals());
    }

    private MatchDto createMatchDtoWithStatus(String status) {
        return new MatchDto(
                1L,
                Instant.now(),
                status,
                1,
                new TeamDto(10L, "Home"),
                new TeamDto(20L, "Away"),
                new ScoreDto(new FullTimeScoreDto(1, 0))
        );
    }
}