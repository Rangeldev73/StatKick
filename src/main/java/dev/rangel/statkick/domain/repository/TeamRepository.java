package dev.rangel.statkick.domain.repository;

import dev.rangel.statkick.domain.model.Team;
import java.util.List;
import java.util.Optional;

public interface TeamRepository {
    void save(Team team);
    void saveAll(List<Team> teams);
    Optional<Team> findById(Long id);
}