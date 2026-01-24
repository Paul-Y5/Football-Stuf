package com.football.model;

/**
 * UEFA Competitions.
 */
public enum Competition {
    CHAMPIONS_LEAGUE("Champions League", "CL", 8, 4),
    EUROPA_LEAGUE("Europa League", "EL", 8, 4),
    CONFERENCE_LEAGUE("Conference League", "ECL", 6, 3);

    private final String displayName;
    private final String code;
    private final int matchesPerTeam;
    private final int homeMatches;

    Competition(String displayName, String code, int matchesPerTeam, int homeMatches) {
        this.displayName = displayName;
        this.code = code;
        this.matchesPerTeam = matchesPerTeam;
        this.homeMatches = homeMatches;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCode() {
        return code;
    }

    public int getMatchesPerTeam() {
        return matchesPerTeam;
    }

    public int getHomeMatches() {
        return homeMatches;
    }

    public int getAwayMatches() {
        return matchesPerTeam - homeMatches;
    }
}
