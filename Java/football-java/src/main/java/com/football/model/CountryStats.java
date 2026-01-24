package com.football.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Statistical summary for a country's clubs.
 */
public class CountryStats {

    private final String country;
    private final List<Club> clubs;

    public CountryStats(String country) {
        this.country = country;
        this.clubs = new ArrayList<>();
    }

    public CountryStats(String country, List<Club> clubs) {
        this.country = country;
        this.clubs = new ArrayList<>(clubs);
    }

    public void addClub(Club club) {
        clubs.add(club);
    }

    public String getCountry() {
        return country;
    }

    public List<Club> getClubs() {
        return clubs;
    }

    /**
     * Number of clubs in country.
     */
    public int getTotalClubs() {
        return clubs.size();
    }

    /**
     * Average ranking position.
     */
    public double getAvgRanking() {
        if (clubs.isEmpty())
            return 0.0;
        return clubs.stream()
                .mapToInt(Club::getRanking)
                .average()
                .orElse(0.0);
    }

    /**
     * Average points.
     */
    public double getAvgPoints() {
        if (clubs.isEmpty())
            return 0.0;
        return clubs.stream()
                .mapToInt(Club::getPoints)
                .average()
                .orElse(0.0);
    }

    /**
     * Standard deviation of points (measures internal competitiveness).
     * Higher std dev = less competitive (bigger gap between clubs).
     */
    public double getStdDevPoints() {
        if (clubs.size() < 2)
            return 0.0;
        double avg = getAvgPoints();
        double variance = clubs.stream()
                .mapToDouble(c -> Math.pow(c.getPoints() - avg, 2))
                .sum() / clubs.size();
        return Math.sqrt(variance);
    }

    /**
     * Highest ranked club.
     */
    public Optional<Club> getBestClub() {
        return clubs.stream()
                .min(Comparator.comparingInt(Club::getRanking));
    }

    /**
     * Lowest ranked club.
     */
    public Optional<Club> getWorstClub() {
        return clubs.stream()
                .max(Comparator.comparingInt(Club::getRanking));
    }

    /**
     * Median ranking position.
     */
    public double getMedianRanking() {
        if (clubs.isEmpty())
            return 0.0;
        List<Integer> rankings = clubs.stream()
                .map(Club::getRanking)
                .sorted()
                .toList();
        int n = rankings.size();
        int mid = n / 2;
        if (n % 2 == 0) {
            return (rankings.get(mid - 1) + rankings.get(mid)) / 2.0;
        }
        return rankings.get(mid);
    }

    /**
     * Number of clubs in world top N.
     */
    public int getTopNCount(int n) {
        return (int) clubs.stream()
                .filter(c -> c.getRanking() <= n)
                .count();
    }

    public int getTop10Count() {
        return getTopNCount(10);
    }

    public int getTop50Count() {
        return getTopNCount(50);
    }

    public int getTop100Count() {
        return getTopNCount(100);
    }

    /**
     * Get top N clubs by ranking.
     */
    public List<Club> getTopN(int n) {
        return clubs.stream()
                .sorted(Comparator.comparingInt(Club::getRanking))
                .limit(n)
                .toList();
    }

    @Override
    public String toString() {
        return String.format("%s: %d clubs, avg rank: %.1f, avg pts: %.1f",
                country, getTotalClubs(), getAvgRanking(), getAvgPoints());
    }
}
