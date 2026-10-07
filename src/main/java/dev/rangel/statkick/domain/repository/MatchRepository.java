package dev.rangel.statkick.domain.repository;

import dev.rangel.statkick.domain.model.Match;
import java.util.Optional;

public interface MatchRepository {
    void save(Match match);
    Optional<Match> findById(Long id);
}