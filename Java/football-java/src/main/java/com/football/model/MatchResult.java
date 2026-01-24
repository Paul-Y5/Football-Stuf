package com.football.model;

/**
 * Match result with scores.
 */
public class MatchResult {

    private final Club home;
    private final Club away;
    private final int homeGoals;
    private final int awayGoals;
    private final int matchday;

    public MatchResult(Club home, Club away, int homeGoals, int awayGoals, int matchday) {
        this.home = home;
        this.away = away;
        this.homeGoals = homeGoals;
        this.awayGoals = awayGoals;
        this.matchday = matchday;
    }

    public Club getHome() {
        return home;
    }

    public Club getAway() {
        return away;
    }

    public int getHomeGoals() {
        return homeGoals;
    }

    public int getAwayGoals() {
        return awayGoals;
    }

    public int getMatchday() {
        return matchday;
    }

    public Club getWinner() {
        if (homeGoals > awayGoals)
            return home;
        if (awayGoals > homeGoals)
            return away;
        return null; // Draw
    }

    public boolean isDraw() {
        return homeGoals == awayGoals;
    }

    public String getScoreDisplay() {
        return homeGoals + " - " + awayGoals;
    }

    @Override
    public String toString() {
        return String.format("%s %d - %d %s", home.getShortName(), homeGoals, awayGoals, away.getShortName());
    }
}
