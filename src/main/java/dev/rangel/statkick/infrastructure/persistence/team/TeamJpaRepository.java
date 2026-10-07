package dev.rangel.statkick.infrastructure.persistence.team;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamJpaRepository extends JpaRepository<TeamEntity, Long> {
}