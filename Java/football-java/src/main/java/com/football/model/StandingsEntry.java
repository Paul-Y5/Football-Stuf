package com.football.model;

import java.util.Objects;

/**
 * League standings entry for a club.
 */
public class StandingsEntry implements Comparable<StandingsEntry> {

    private final Club club;
    private int played;
    private int won;
    private int drawn;
    private int lost;
    private int goalsFor;
    private int goalsAgainst;
    private int points;

    public StandingsEntry(Club club) {
        this.club = club;
        this.played = 0;
        this.won = 0;
        this.drawn = 0;
        this.lost = 0;
        this.goalsFor = 0;
        this.goalsAgainst = 0;
        this.points = 0;
    }

    /**
     * Record a match result.
     */
    public void recordResult(int scored, int conceded) {
        played++;
        goalsFor += scored;
        goalsAgainst += conceded;

        if (scored > conceded) {
            won++;
            points += 3;
        } else if (scored == conceded) {
            drawn++;
            points += 1;
        } else {
            lost++;
        }
    }

    /**
     * Goal difference.
     */
    public int getGoalDifference() {
        return goalsFor - goalsAgainst;
    }

    // Getters
    public Club getClub() {
        return club;
    }

    public int getPlayed() {
        return played;
    }

    public int getWon() {
        return won;
    }

    public int getDrawn() {
        return drawn;
    }

    public int getLost() {
        return lost;
    }

    public int getGoalsFor() {
        return goalsFor;
    }

    public int getGoalsAgainst() {
        return goalsAgainst;
    }

    public int getPoints() {
        return points;
    }

    @Override
    public int compareTo(StandingsEntry other) {
        // Higher points first
        int cmp = Integer.compare(other.points, this.points);
        if (cmp != 0)
            return cmp;

        // Goal difference
        cmp = Integer.compare(other.getGoalDifference(), this.getGoalDifference());
        if (cmp != 0)
            return cmp;

        // Goals scored
        cmp = Integer.compare(other.goalsFor, this.goalsFor);
        if (cmp != 0)
            return cmp;

        // Name alphabetically
        return this.club.getName().compareTo(other.club.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        StandingsEntry that = (StandingsEntry) o;
        return Objects.equals(club, that.club);
    }

    @Override
    public int hashCode() {
        return Objects.hash(club);
    }
}
