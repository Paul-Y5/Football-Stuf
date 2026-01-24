package com.football.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Final draw result.
 */
public class DrawResult {

    private final String id;
    private final Competition competition;
    private final List<Club> clubs;
    private final List<Fixture> fixtures;
    private boolean valid;
    private int backtrackCount;
    private LocalDateTime createdAt;

    public DrawResult(Competition competition, List<Club> clubs) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.competition = competition;
        this.clubs = new ArrayList<>(clubs);
        this.fixtures = new ArrayList<>();
        this.valid = true;
        this.backtrackCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Get all fixtures for a specific club.
     */
    public Map<String, List<String>> getClubFixtures(String clubName) {
        Map<String, List<String>> result = new HashMap<>();
        result.put("home", new ArrayList<>());
        result.put("away", new ArrayList<>());

        for (Club club : clubs) {
            if (club.getName().equals(clubName)) {
                for (Club opp : club.getHomeOpponents()) {
                    result.get("home").add(opp.getName());
                }
                for (Club opp : club.getAwayOpponents()) {
                    result.get("away").add(opp.getName());
                }
                break;
            }
        }
        return result;
    }

    /**
     * Get clubs grouped by pot.
     */
    public Map<Integer, List<Club>> getClubsByPot() {
        Map<Integer, List<Club>> result = new HashMap<>();
        for (Club club : clubs) {
            result.computeIfAbsent(club.getPot(), k -> new ArrayList<>()).add(club);
        }
        return result;
    }

    /**
     * Export to map for JSON serialization.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("competition", competition.getDisplayName());
        result.put("valid", valid);
        result.put("backtrackCount", backtrackCount);
        result.put("createdAt", createdAt.toString());

        List<Map<String, Object>> clubMaps = new ArrayList<>();
        for (Club c : clubs) {
            Map<String, Object> clubMap = new HashMap<>();
            clubMap.put("name", c.getName());
            clubMap.put("country", c.getCountry());
            clubMap.put("pot", c.getPot());
            clubMap.put("crest", c.getCrestUrl());
            clubMap.put("home", c.getHomeOpponents().stream().map(Club::getName).toList());
            clubMap.put("away", c.getAwayOpponents().stream().map(Club::getName).toList());
            clubMaps.add(clubMap);
        }
        result.put("clubs", clubMaps);

        return result;
    }

    /**
     * Print draw summary to console.
     */
    public void printSummary() {
        System.out.println("\n" + competition.getDisplayName() + " Draw Results");
        System.out.println("=".repeat(50));

        for (int pot = 1; pot <= 4; pot++) {
            final int currentPot = pot;
            List<Club> potClubs = clubs.stream()
                    .filter(c -> c.getPot() == currentPot)
                    .toList();

            if (!potClubs.isEmpty()) {
                System.out.println("\n[Pot " + pot + "]");
                for (Club club : potClubs) {
                    String home = String.join(", ",
                            club.getHomeOpponents().stream().map(Club::getShortName).toList());
                    String away = String.join(", ",
                            club.getAwayOpponents().stream().map(Club::getShortName).toList());
                    System.out.println("  " + club.getName() + " (" + club.getCountry() + ")");
                    System.out.println("    [H] Home: " + home);
                    System.out.println("    [A] Away: " + away);
                }
            }
        }
    }

    // === Getters and Setters ===

    public String getId() {
        return id;
    }

    public Competition getCompetition() {
        return competition;
    }

    public List<Club> getClubs() {
        return clubs;
    }

    public List<Fixture> getFixtures() {
        return fixtures;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public int getBacktrackCount() {
        return backtrackCount;
    }

    public void setBacktrackCount(int backtrackCount) {
        this.backtrackCount = backtrackCount;
    }

    public void incrementBacktrack() {
        this.backtrackCount++;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
