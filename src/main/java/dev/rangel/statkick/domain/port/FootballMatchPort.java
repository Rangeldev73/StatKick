package dev.rangel.statkick.domain.port;

import dev.rangel.statkick.domain.model.Match;
import java.util.List;

public interface FootballMatchPort {
    List<Match> fetchMatches(String competitionCode);
}