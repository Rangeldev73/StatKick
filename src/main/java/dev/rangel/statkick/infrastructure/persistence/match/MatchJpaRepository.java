package dev.rangel.statkick.infrastructure.persistence.match;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MatchJpaRepository extends JpaRepository<MatchEntity, Long> {

    @Query("SELECT m FROM MatchEntity m JOIN FETCH m.homeTeam JOIN FETCH m.awayTeam WHERE m.id = :id")
    Optional<MatchEntity> findByIdWithTeams(@Param("id") Long id);
}