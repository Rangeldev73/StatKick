package dev.rangel.statkick.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TeamTest {

    @Test
    @DisplayName("Should consider two teams with same ID as equal and deduplicate in HashSet")
    void shouldDeduplicateTeamsWithSameIdInSet() {
        Team team1 = new Team(57L, "Arsenal FC");
        Team team2 = new Team(57L, "Arsenal FC");

        assertThat(team1).isEqualTo(team2);
        assertThat(team1.hashCode()).isEqualTo(team2.hashCode());

        Set<Team> teams = new HashSet<>(List.of(team1, team2));

        assertThat(teams).hasSize(1);
    }
}