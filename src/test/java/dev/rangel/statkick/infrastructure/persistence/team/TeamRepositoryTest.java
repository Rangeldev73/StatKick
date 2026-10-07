package dev.rangel.statkick.infrastructure.persistence.team;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TeamRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private TeamJpaRepository teamJpaRepository;

    @Test
    @DisplayName("Should save and retrieve team from real PostgreSQL container")
    void shouldSaveAndRetrieveTeam() {
        TeamEntity team = new TeamEntity(57L, "Arsenal FC");

        teamJpaRepository.save(team);

        Optional<TeamEntity> found = teamJpaRepository.findById(57L);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(57L);
        assertThat(found.get().getName()).isEqualTo("Arsenal FC");
    }
}