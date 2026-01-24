package com.football.service;

import com.football.model.Club;
import com.football.model.CountryStats;
import com.football.model.TrendDirection;
import com.football.repository.ClubRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing club rankings.
 */
@Service
@Transactional
public class RankingService {

    private final ClubRepository clubRepository;

    public RankingService(ClubRepository clubRepository) {
        this.clubRepository = clubRepository;
    }

    /**
     * Load clubs from CSV file.
     * Expected columns: Ranking, Club, Country, Points, 1YChange, PrevPoints, Trend
     */
    @CacheEvict(value = {"rankings", "topClubs", "clubsByCountry", "countries"}, allEntries = true)
    public List<Club> loadFromCsv(String path) throws IOException, CsvException {
        List<Club> clubs = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            List<String[]> rows = reader.readAll();

            // Skip header
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length >= 7) {
                    Club club = parseClubFromCsv(row);
                    clubs.add(club);
                }
            }
        }

        // Save all to database
        clubRepository.saveAll(clubs);
        return clubs;
    }

    /**
     * Load clubs from classpath resource.
     */
    @CacheEvict(value = {"rankings", "topClubs", "clubsByCountry", "countries"}, allEntries = true)
    public List<Club> loadFromResource(String resourcePath) throws IOException, CsvException {
        List<Club> clubs = new ArrayList<>();

        try (InputStream is = getClass().getResourceAsStream(resourcePath);
                CSVReader reader = new CSVReader(new InputStreamReader(is))) {

            List<String[]> rows = reader.readAll();

            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length >= 7) {
                    Club club = parseClubFromCsv(row);
                    clubs.add(club);
                }
            }
        }

        // Save all to database
        clubRepository.saveAll(clubs);
        return clubs;
    }

    private Club parseClubFromCsv(String[] row) {
        Club club = new Club();
        club.setRanking(Integer.parseInt(row[0].trim()));
        club.setName(row[1].trim());
        club.setCountry(row[2].trim());
        club.setPoints(Integer.parseInt(row[3].trim()));
        // 1YChange is row[4] - we can use it if needed
        club.setPreviousPoints(Integer.parseInt(row[5].trim()));
        club.setTrend(TrendDirection.fromSymbol(row[6].trim()));
        return club;
    }

    /**
     * Add or update a club.
     */
    @CacheEvict(value = {"rankings", "topClubs", "clubsByCountry"}, allEntries = true)
    public Club addClub(Club club) {
        return clubRepository.save(club);
    }

    @CacheEvict(value = {"rankings", "topClubs", "clubsByCountry"}, allEntries = true)
    public Club addOrUpdateClub(Club club) {
        return clubRepository.save(club);
    }

    /**
     * Add multiple clubs.
     */
    @CacheEvict(value = {"rankings", "topClubs", "clubsByCountry"}, allEntries = true)
    public List<Club> addClubs(List<Club> newClubs) {
        return clubRepository.saveAll(newClubs);
    }

    /**
     * Get all clubs with pagination.
     */
    public Page<Club> getAllClubs(Pageable pageable) {
        return clubRepository.findAll(pageable);
    }

    /**
     * Get all clubs sorted by ranking.
     */
    @Cacheable("rankings")
    public List<Club> getAllClubs() {
        return clubRepository.findAll().stream()
                .sorted(Comparator.comparingInt(Club::getRanking))
                .toList();
    }

    /**
     * Get club by ID.
     */
    public Optional<Club> getClubById(Long id) {
        return clubRepository.findById(id);
    }

    /**
     * Get clubs by country.
     */
    @Cacheable(value = "clubsByCountry", key = "#country")
    public List<Club> getClubsByCountry(String country) {
        return clubRepository.findByCountryOrderByRankingAsc(country);
    }

    /**
     * Get top N clubs worldwide.
     */
    @Cacheable(value = "topClubs", key = "#n")
    public List<Club> getTopClubs(int n) {
        return clubRepository.findAll().stream()
                .sorted(Comparator.comparingInt(Club::getRanking))
                .limit(n)
                .toList();
    }

    /**
     * Search clubs by name or country.
     */
    public List<Club> searchClubs(String query) {
        return clubRepository.searchByNameOrCountry(query);
    }

    /**
     * Search clubs by name.
     */
    public List<Club> searchByName(String query) {
        return clubRepository.searchByNameOrCountry(query);
    }

    /**
     * Get clubs by trend.
     */
    public List<Club> getClubsByTrend(TrendDirection trend) {
        if (trend == TrendDirection.UP) {
            return clubRepository.findImprovedClubs();
        } else if (trend == TrendDirection.DOWN) {
            return clubRepository.findDeclinedClubs();
        }
        return List.of();
    }

    /**
     * Get distinct countries.
     */
    @Cacheable("countries")
    public List<String> getDistinctCountries() {
        return clubRepository.findDistinctCountries();
    }

    /**
     * Get country statistics.
     */
    @Cacheable(value = "countryStats", key = "#country")
    public Map<String, Object> getCountryStatistics(String country) {
        List<Club> clubs = getClubsByCountry(country);
        if (clubs.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("country", country);
        stats.put("totalClubs", clubs.size());
        stats.put("avgPoints", clubs.stream().mapToInt(Club::getPoints).average().orElse(0));
        stats.put("avgRanking", clubs.stream().mapToInt(Club::getRanking).average().orElse(0));
        stats.put("topClub", clubs.get(0).getName());
        stats.put("topClubRanking", clubs.get(0).getRanking());
        
        long improvedCount = clubs.stream()
                .filter(c -> c.getTrend() == TrendDirection.UP)
                .count();
        stats.put("improvedClubs", improvedCount);

        return stats;
    }

    /**
     * Get country statistics.
     */
    public CountryStats getCountryStats(String country) {
        List<Club> clubs = getClubsByCountry(country);
        return new CountryStats(country, clubs);
    }

    /**
     * Get all country statistics.
     */
    public List<CountryStats> getAllCountryStats() {
        return getDistinctCountries().stream()
                .map(this::getCountryStats)
                .toList();
    }

    /**
     * Get list of all countries.
     */
    public List<String> getAllCountries() {
        return getDistinctCountries();
    }

    /**
     * Refresh rankings - clear caches.
     */
    @CacheEvict(value = {"rankings", "topClubs", "clubsByCountry", "countries", "countryStats"}, allEntries = true)
    public void refreshRankings() {
        // Cache evicted, next calls will fetch fresh data
    }

    /**
     * Get global statistics.
     */
    public Map<String, Object> getGlobalStats() {
        List<Club> allClubs = getAllClubs();
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalClubs", allClubs.size());
        stats.put("totalCountries", getDistinctCountries().size());

        if (!allClubs.isEmpty()) {
            stats.put("avgPoints", allClubs.stream().mapToInt(Club::getPoints).average().orElse(0));
            stats.put("maxPoints", allClubs.stream().mapToInt(Club::getPoints).max().orElse(0));
            stats.put("minPoints", allClubs.stream().mapToInt(Club::getPoints).min().orElse(0));
        }

        return stats;
    }

    public int getClubCount() {
        return (int) clubRepository.count();
    }
}
