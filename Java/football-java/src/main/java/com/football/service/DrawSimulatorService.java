package com.football.service;

import com.football.model.Club;
import com.football.model.Competition;
import com.football.model.DrawResult;
import com.football.model.Fixture;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * UEFA Draw Simulator using backtracking algorithm.
 * 
 * Implements the official UEFA draw constraints:
 * - Clubs cannot face teams from their own country
 * - Maximum 2 opponents from the same country
 * - Balanced home/away matches
 */
@Service
public class DrawSimulatorService {

    private static final int MAX_SAME_COUNTRY = 2;
    private final Random random = new Random();
    private final RankingService rankingService;

    public DrawSimulatorService(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    /**
     * Simulate draw (alias for executeDraw).
     */
    public DrawResult simulateDraw(List<Club> clubs, Competition competition) {
        return executeDraw(competition, clubs);
    }

    /**
     * Get top N clubs for draw simulation.
     */
    public List<Club> getTopClubsForDraw(int count) {
        return rankingService.getTopClubs(count);
    }

    /**
     * Execute draw for a competition.
     */
    public DrawResult executeDraw(Competition competition, List<Club> clubs) {
        return executeDraw(competition, clubs, null);
    }

    /**
     * Execute draw with optional seed for reproducibility.
     */
    public DrawResult executeDraw(Competition competition, List<Club> clubs, Long seed) {
        if (seed != null) {
            random.setSeed(seed);
        }

        // Reset all clubs
        for (Club club : clubs) {
            club.resetDraw();
        }

        DrawResult result = new DrawResult(competition, clubs);

        // Execute backtracking algorithm
        boolean success = assignOpponentsOptimized(clubs, competition, result);
        result.setValid(success);

        if (success) {
            generateFixtures(result);
        }

        return result;
    }

    /**
     * Optimized recursive opponent assignment using MRV heuristic.
     */
    private boolean assignOpponentsOptimized(List<Club> clubs, Competition comp, DrawResult result) {
        // Find club with fewest valid opponents (MRV - Minimum Remaining Values)
        Club mostConstrained = null;
        int minValidOpponents = Integer.MAX_VALUE;
        boolean needsHome = true;

        for (Club club : clubs) {
            if (!club.isComplete(comp)) {
                int validCount = countValidOpponents(club, clubs, comp);
                if (validCount < minValidOpponents) {
                    minValidOpponents = validCount;
                    mostConstrained = club;
                    needsHome = club.needsHome(comp) > 0;
                }
            }
        }

        // All clubs complete
        if (mostConstrained == null) {
            return true;
        }

        // No valid opponents = dead end
        if (minValidOpponents == 0) {
            return false;
        }

        // Get valid opponents sorted by their constraint level
        List<Club> validOpponents = getValidOpponentsSorted(mostConstrained, clubs, needsHome, comp);

        // Shuffle to add randomness
        Collections.shuffle(validOpponents, random);

        for (Club opponent : validOpponents) {
            // Try assignment
            if (needsHome) {
                mostConstrained.addHomeOpponent(opponent);
                opponent.addAwayOpponent(mostConstrained);
            } else {
                mostConstrained.addAwayOpponent(opponent);
                opponent.addHomeOpponent(mostConstrained);
            }

            // Recurse
            if (assignOpponentsOptimized(clubs, comp, result)) {
                return true;
            }

            // Backtrack
            result.incrementBacktrack();
            if (needsHome) {
                mostConstrained.getHomeOpponents().remove(opponent);
                opponent.getAwayOpponents().remove(mostConstrained);
            } else {
                mostConstrained.getAwayOpponents().remove(opponent);
                opponent.getHomeOpponents().remove(mostConstrained);
            }
        }

        return false;
    }

    /**
     * Count valid opponents for a club.
     */
    private int countValidOpponents(Club club, List<Club> allClubs, Competition comp) {
        int count = 0;
        boolean needsHome = club.needsHome(comp) > 0;

        for (Club other : allClubs) {
            if (isValidOpponent(club, other, needsHome, comp)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get valid opponents sorted by constraint level (most constrained first).
     */
    private List<Club> getValidOpponentsSorted(Club club, List<Club> allClubs,
            boolean asHome, Competition comp) {
        List<Club> valid = new ArrayList<>();

        for (Club other : allClubs) {
            if (isValidOpponent(club, other, asHome, comp)) {
                valid.add(other);
            }
        }

        // Sort by number of valid opponents (ascending)
        valid.sort(Comparator.comparingInt(c -> countValidOpponents(c, allClubs, comp)));

        return valid;
    }

    /**
     * Check if opponent is valid for this match.
     */
    private boolean isValidOpponent(Club club, Club opponent, boolean asHome, Competition comp) {
        // Basic constraints
        if (!club.canFace(opponent, MAX_SAME_COUNTRY)) {
            return false;
        }

        // Check if opponent can accept the reciprocal match
        if (asHome) {
            // We play home, opponent plays away
            if (opponent.needsAway(comp) <= 0) {
                return false;
            }
        } else {
            // We play away, opponent plays home
            if (opponent.needsHome(comp) <= 0) {
                return false;
            }
        }

        // Opponent must also be able to face us
        return opponent.canFace(club, MAX_SAME_COUNTRY);
    }

    /**
     * Generate fixture list from club assignments.
     */
    private void generateFixtures(DrawResult result) {
        List<Fixture> fixtures = result.getFixtures();
        Set<String> added = new HashSet<>();

        for (Club club : result.getClubs()) {
            for (Club opponent : club.getHomeOpponents()) {
                String key = club.getName() + " vs " + opponent.getName();
                if (!added.contains(key)) {
                    fixtures.add(new Fixture(club, opponent));
                    added.add(key);
                }
            }
        }
    }

    /**
     * Validate a draw result.
     */
    public List<String> validateDraw(DrawResult result) {
        List<String> errors = new ArrayList<>();
        Competition comp = result.getCompetition();

        for (Club club : result.getClubs()) {
            // Check home/away balance
            if (club.getHomeOpponents().size() != comp.getHomeMatches()) {
                errors.add(club.getName() + " has " + club.getHomeOpponents().size()
                        + " home matches, expected " + comp.getHomeMatches());
            }
            if (club.getAwayOpponents().size() != comp.getAwayMatches()) {
                errors.add(club.getName() + " has " + club.getAwayOpponents().size()
                        + " away matches, expected " + comp.getAwayMatches());
            }

            // Check country constraints
            Map<String, Integer> countryCounts = club.getOpponentCountries();
            for (Map.Entry<String, Integer> entry : countryCounts.entrySet()) {
                if (entry.getValue() > MAX_SAME_COUNTRY) {
                    errors.add(club.getName() + " faces " + entry.getValue()
                            + " clubs from " + entry.getKey() + ", max is " + MAX_SAME_COUNTRY);
                }
            }

            // Check no same-country opponents
            for (Club opp : club.getAllOpponents()) {
                if (opp.getCountry().equals(club.getCountry())) {
                    errors.add(club.getName() + " faces same-country team " + opp.getName());
                }
            }
        }

        return errors;
    }
}
