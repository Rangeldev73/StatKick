package dev.rangel.statkick.infrastructure.persistence.team;

import dev.rangel.statkick.domain.model.Team;
import org.springframework.stereotype.Component;

@Component
public class TeamEntityMapper {

    public TeamEntity toEntity(Team domain) {
        if (domain == null) {
            return null;
        }
        return new TeamEntity(domain.getId(), domain.getName());
    }

    public Team toDomain(TeamEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Team(entity.getId(), entity.getName());
    }
}