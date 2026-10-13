package dev.rangel.statkick.domain.repository;

import dev.rangel.statkick.domain.model.Match;
import java.util.List;
import java.util.Optional;

public interface MatchRepository {
    void save(Match match);
    void saveAll(List<Match> matches);
    Optional<Match> findById(Long id);
}