package dev.rangel.statkick.application.service;

import dev.rangel.statkick.domain.model.Match;
import dev.rangel.statkick.domain.model.Team;
import dev.rangel.statkick.domain.repository.MatchRepository;
import dev.rangel.statkick.domain.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MatchPersistenceService {

    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;

    @Transactional
    public void saveAtomically(Set<Team> teams, List<Match> matches) {
        teamRepository.saveAll(teams.stream().toList());
        matchRepository.saveAll(matches);
    }
}