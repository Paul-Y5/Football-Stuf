package com.football.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Represents a football club with ranking data.
 * Used for both Rankings Dashboard and Draw Simulator.
 */
@Entity
@Table(name = "clubs", indexes = {
    @Index(name = "idx_club_ranking", columnList = "ranking"),
    @Index(name = "idx_club_country", columnList = "country"),
    @Index(name = "idx_club_name", columnList = "name")
})
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String shortName;

    @Column(nullable = false)
    private String country;

    private String countryCode;

    @Column(nullable = false)
    private int ranking;

    @Column(nullable = false)
    private int points;

    private int previousPoints;

    @Enumerated(EnumType.STRING)
    private TrendDirection trend;

    private double coefficient;

    private int pot;

    private String crestUrl;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Draw state (for simulator) - transient as not persisted
    @Transient
    private final List<Club> homeOpponents = new ArrayList<>();

    @Transient
    private final List<Club> awayOpponents = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Club() {
    }

    public Club(String name, String country, int ranking, int points) {
        this.name = name;
        this.country = country;
        this.ranking = ranking;
        this.points = points;
        this.previousPoints = points;
        this.trend = TrendDirection.STABLE;
    }

    // === Computed Properties ===

    /**
     * Calculate points difference from previous score.
     */
    public int getPointsChange() {
        return points - previousPoints;
    }

    /**
     * Check if club improved in ranking.
     */
    public boolean isImproved() {
        return trend == TrendDirection.UP;
    }

    /**
     * Check if club declined in ranking.
     */
    public boolean isDeclined() {
        return trend == TrendDirection.DOWN;
    }

    /**
     * All assigned opponents (home + away).
     */
    public List<Club> getAllOpponents() {
        List<Club> all = new ArrayList<>(homeOpponents);
        all.addAll(awayOpponents);
        return all;
    }

    /**
     * Count of opponents by country.
     */
    public Map<String, Integer> getOpponentCountries() {
        Map<String, Integer> countries = new HashMap<>();
        for (Club opp : getAllOpponents()) {
            countries.merge(opp.getCountry(), 1, Integer::sum);
        }
        return countries;
    }

    /**
     * Check if club has all opponents assigned.
     */
    public boolean isComplete(Competition competition) {
        return homeOpponents.size() == competition.getHomeMatches()
                && awayOpponents.size() == competition.getAwayMatches();
    }

    /**
     * Number of home matches still needed.
     */
    public int needsHome(Competition competition) {
        return competition.getHomeMatches() - homeOpponents.size();
    }

    /**
     * Number of away matches still needed.
     */
    public int needsAway(Competition competition) {
        return competition.getAwayMatches() - awayOpponents.size();
    }

    /**
     * Check if this club can face another (UEFA rules).
     * - Cannot face club from same country
     * - Cannot face same club twice
     * - Max 2 opponents from same country
     */
    public boolean canFace(Club other, int maxSameCountry) {
        if (this.name.equals(other.name)) {
            return false;
        }
        if (this.country.equals(other.country)) {
            return false;
        }
        if (getAllOpponents().contains(other)) {
            return false;
        }
        int currentFromCountry = getOpponentCountries().getOrDefault(other.country, 0);
        return currentFromCountry < maxSameCountry;
    }

    /**
     * Reset draw state.
     */
    public void resetDraw() {
        homeOpponents.clear();
        awayOpponents.clear();
    }

    // === Getters and Setters ===

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName != null ? shortName : name;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getPreviousPoints() {
        return previousPoints;
    }

    public void setPreviousPoints(int previousPoints) {
        this.previousPoints = previousPoints;
    }

    public TrendDirection getTrend() {
        return trend;
    }

    public void setTrend(TrendDirection trend) {
        this.trend = trend;
    }

    public double getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(double coefficient) {
        this.coefficient = coefficient;
    }

    public int getPot() {
        return pot;
    }

    public void setPot(int pot) {
        this.pot = pot;
    }

    public String getCrestUrl() {
        return crestUrl;
    }

    public void setCrestUrl(String crestUrl) {
        this.crestUrl = crestUrl;
    }

    public List<Club> getHomeOpponents() {
        return homeOpponents;
    }

    public List<Club> getAwayOpponents() {
        return awayOpponents;
    }

    public void addHomeOpponent(Club club) {
        homeOpponents.add(club);
    }

    public void addAwayOpponent(Club club) {
        awayOpponents.add(club);
    }
        public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Club club = (Club) o;
        return Objects.equals(name, club.name) && Objects.equals(country, club.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, country);
    }

    @Override
    public String toString() {
        return String.format("%d. %s (%s) - %d pts", ranking, name, country, points);
    }
}
