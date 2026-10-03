package dev.rangel.statkick.integration.footballapi.cache;

import dev.rangel.statkick.domain.model.Match;
import dev.rangel.statkick.domain.port.FootballMatchPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Primary
public class CachedFootballMatchAdapter implements FootballMatchPort {

    private final FootballMatchPort delegate;

    public CachedFootballMatchAdapter(@Qualifier("resilientFootballMatchAdapter") FootballMatchPort delegate) {
        this.delegate = delegate;
    }

    @Override
    @Cacheable(value = "matchesByCompetition", key = "#competitionCode", sync = true)
    public List<Match> fetchMatches(String competitionCode) {
        return delegate.fetchMatches(competitionCode);
    }
}