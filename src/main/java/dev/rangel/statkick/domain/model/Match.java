package dev.rangel.statkick.domain.model;

import java.time.Instant;

public class Match {
    private final Long id;
    private final Instant date;
    private final Team homeTeam;
    private final Team awayTeam;
    private final Score score;
    private final MatchStatus status;

    public Match(Long id, Instant date, Team homeTeam, Team awayTeam, Score score, MatchStatus status) {
        this.id = id;
        this.date = date;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.score = score;
        this.status = status;
    }

    public boolean isFinished() {
        return this.status == MatchStatus.FINISHED;
    }

    public Long getId() { return id; }
    public Instant getDate() { return date; }
    public Team getHomeTeam() { return homeTeam; }
    public Team getAwayTeam() { return awayTeam; }
    public Score getScore() { return score; }
    public MatchStatus getStatus() { return status; }
}