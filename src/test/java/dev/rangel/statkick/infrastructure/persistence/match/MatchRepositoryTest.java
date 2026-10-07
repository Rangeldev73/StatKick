package dev.rangel.statkick.infrastructure.persistence.match;

import dev.rangel.statkick.domain.model.MatchStatus;
import dev.rangel.statkick.infrastructure.persistence.team.TeamEntity;
import dev.rangel.statkick.infrastructure.persistence.team.TeamJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MatchRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private MatchJpaRepository matchJpaRepository;

    @Autowired
    private TeamJpaRepository teamJpaRepository;

    @Test
    @DisplayName("Should save and retrieve SCHEDULED match with null score and handle JOIN FETCH")
    void shouldSaveAndRetrieveScheduledMatchWithNullScore() {
        TeamEntity homeTeam = new TeamEntity(57L, "Arsenal FC");
        TeamEntity awayTeam = new TeamEntity(65L, "Manchester City FC");
        teamJpaRepository.save(homeTeam);
        teamJpaRepository.save(awayTeam);

        MatchEntity match = new MatchEntity(
                1001L,
                Instant.now(),
                MatchStatus.SCHEDULED,
                homeTeam,
                awayTeam,
                null
        );
        matchJpaRepository.save(match);

        Optional<MatchEntity> found = matchJpaRepository.findByIdWithTeams(1001L);

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(MatchStatus.SCHEDULED);

        assertThat(found.get().getScore()).isNull();

        assertThat(found.get().getHomeTeam().getName()).isEqualTo("Arsenal FC");
        assertThat(found.get().getAwayTeam().getName()).isEqualTo("Manchester City FC");
    }

    @Test
    @DisplayName("Should save and retrieve FINISHED match with populated score")
    void shouldSaveAndRetrieveFinishedMatchWithPopulatedScore() {
        TeamEntity homeTeam = new TeamEntity(57L, "Arsenal FC");
        TeamEntity awayTeam = new TeamEntity(65L, "Manchester City FC");
        teamJpaRepository.save(homeTeam);
        teamJpaRepository.save(awayTeam);

        ScoreEmbeddable score = new ScoreEmbeddable(2, 1);

        MatchEntity match = new MatchEntity(
                1002L,
                Instant.now(),
                MatchStatus.FINISHED,
                homeTeam,
                awayTeam,
                score
        );
        matchJpaRepository.save(match);

        Optional<MatchEntity> found = matchJpaRepository.findByIdWithTeams(1002L);

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(MatchStatus.FINISHED);
        assertThat(found.get().getScore()).isNotNull();
        assertThat(found.get().getScore().getHomeGoals()).isEqualTo(2);
        assertThat(found.get().getScore().getAwayGoals()).isEqualTo(1);
        assertThat(found.get().getHomeTeam().getName()).isEqualTo("Arsenal FC");
        assertThat(found.get().getAwayTeam().getName()).isEqualTo("Manchester City FC");
    }
}