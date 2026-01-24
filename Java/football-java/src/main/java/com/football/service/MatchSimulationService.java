package com.football.service;

import com.football.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for simulating match results and generating standings.
 */
@Service
public class MatchSimulationService {

    private final Random random = new Random();

    /**
     * Simulate all matches from a draw result and generate standings.
     */
    public SimulationResult simulateCompetition(DrawResult drawResult) {
        return simulateCompetition(drawResult, null);
    }

    /**
     * Simulate with optional seed for reproducibility.
     */
    public SimulationResult simulateCompetition(DrawResult drawResult, Long seed) {
        if (seed != null) {
            random.setSeed(seed);
        }

        Map<String, StandingsEntry> standings = new HashMap<>();
        List<MatchResult> results = new ArrayList<>();
        Map<Integer, List<MatchResult>> matchdays = new HashMap<>();

        // Initialize standings for all clubs
        for (Club club : drawResult.getClubs()) {
            standings.put(club.getName(), new StandingsEntry(club));
        }

        // Generate and simulate matches
        int matchday = 1;
        Set<String> simulatedMatches = new HashSet<>();

        for (Club club : drawResult.getClubs()) {
            for (Club opponent : club.getHomeOpponents()) {
                String matchKey = club.getName() + " vs " + opponent.getName();
                if (!simulatedMatches.contains(matchKey)) {
                    MatchResult result = simulateMatch(club, opponent, matchday);
                    results.add(result);
                    simulatedMatches.add(matchKey);

                    // Update standings
                    standings.get(club.getName()).recordResult(result.getHomeGoals(), result.getAwayGoals());
                    standings.get(opponent.getName()).recordResult(result.getAwayGoals(), result.getHomeGoals());

                    // Group by matchday
                    matchdays.computeIfAbsent(matchday, k -> new ArrayList<>()).add(result);

                    matchday = (matchday % 8) + 1; // Cycle through 8 matchdays
                }
            }
        }

        // Sort standings
        List<StandingsEntry> sortedStandings = new ArrayList<>(standings.values());
        Collections.sort(sortedStandings);

        return new SimulationResult(sortedStandings, results, matchdays, drawResult.getCompetition());
    }

    /**
     * Simulate a single match between two clubs.
     * Uses coefficient-weighted probability.
     */
    private MatchResult simulateMatch(Club home, Club away, int matchday) {
        // Base probability influenced by coefficients
        double homeStrength = 0.45 + (home.getCoefficient() / 400.0); // Home advantage
        double awayStrength = 0.35 + (away.getCoefficient() / 400.0);

        // Clamp probabilities
        homeStrength = Math.min(0.75, Math.max(0.25, homeStrength));
        awayStrength = Math.min(0.65, Math.max(0.15, awayStrength));

        // Generate goals using Poisson-like distribution
        int homeGoals = generateGoals(homeStrength);
        int awayGoals = generateGoals(awayStrength);

        return new MatchResult(home, away, homeGoals, awayGoals, matchday);
    }

    /**
     * Generate goals based on team strength.
     */
    private int generateGoals(double strength) {
        // Expected goals based on strength (0.5 to 3.0)
        double lambda = 0.5 + (strength * 2.5);

        // Simple Poisson approximation
        double L = Math.exp(-lambda);
        int k = 0;
        double p = 1.0;

        do {
            k++;
            p *= random.nextDouble();
        } while (p > L);

        return Math.min(k - 1, 7); // Cap at 7 goals
    }

    /**
     * Simulation result container.
     */
    public record SimulationResult(
            List<StandingsEntry> standings,
            List<MatchResult> results,
            Map<Integer, List<MatchResult>> matchdays,
            Competition competition) {
        public StandingsEntry getLeader() {
            return standings.isEmpty() ? null : standings.get(0);
        }

        public List<StandingsEntry> getTop8() {
            return standings.size() >= 8 ? standings.subList(0, 8) : standings;
        }

        public List<StandingsEntry> getPlayoffPositions() {
            // Positions 9-24 go to playoffs
            int start = Math.min(8, standings.size());
            int end = Math.min(24, standings.size());
            return standings.subList(start, end);
        }

        public List<StandingsEntry> getEliminatedPositions() {
            // Positions 25+ are eliminated
            int start = Math.min(24, standings.size());
            return standings.subList(start, standings.size());
        }

        public int getTotalGoals() {
            return results.stream()
                    .mapToInt(r -> r.getHomeGoals() + r.getAwayGoals())
                    .sum();
        }

        public int getTotalMatches() {
            return results.size();
        }

        public double getAvgGoalsPerMatch() {
            if (results.isEmpty())
                return 0;
            return (double) getTotalGoals() / getTotalMatches();
        }
    }
}
