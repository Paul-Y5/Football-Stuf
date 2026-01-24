package com.football.model;

/**
 * A match fixture in the draw.
 */
public class Fixture {

    private final Club home;
    private final Club away;
    private int matchday;

    public Fixture(Club home, Club away) {
        this.home = home;
        this.away = away;
        this.matchday = 0;
    }

    public Fixture(Club home, Club away, int matchday) {
        this.home = home;
        this.away = away;
        this.matchday = matchday;
    }

    public Club getHome() {
        return home;
    }

    public Club getAway() {
        return away;
    }

    public int getMatchday() {
        return matchday;
    }

    public void setMatchday(int matchday) {
        this.matchday = matchday;
    }

    @Override
    public String toString() {
        return String.format("%s vs %s", home.getShortName(), away.getShortName());
    }
}
