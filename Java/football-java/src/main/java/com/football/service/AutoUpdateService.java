package com.football.service;

import com.football.api.FootballDataApiClient;
import com.football.model.Club;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service that automatically updates rankings from the API.
 * Runs periodically to fetch latest standings from major leagues.
 * Can be disabled by setting football.autoupdate.enabled=false
 */
@Service
@ConditionalOnProperty(name = "football.autoupdate.enabled", havingValue = "true", matchIfMissing = true)
public class AutoUpdateService {

    private static final Logger log = LoggerFactory.getLogger(AutoUpdateService.class);

    private final FootballDataApiClient apiClient;
    private final RankingService rankingService;

    // Track last update times
    private final Map<String, LocalDateTime> lastUpdates = new ConcurrentHashMap<>();

    // Competitions to auto-update
    private static final String[] AUTO_UPDATE_COMPETITIONS = {
            "PL", // Premier League
            "PD", // La Liga
            "SA", // Serie A
            "BL1", // Bundesliga
            "FL1", // Ligue 1
            "PPL", // Primeira Liga
            "CL", // Champions League
            "EL" // Europa League
    };

    public AutoUpdateService(FootballDataApiClient apiClient, RankingService rankingService) {
        this.apiClient = apiClient;
        this.rankingService = rankingService;
    }

    /**
     * Auto-update rankings every 15 minutes.
     * Respects football-data.org API rate limits (10 calls/minute on free tier).
     */
    @Scheduled(fixedRate = 900000) // 15 minutes
    public void autoUpdateRankings() {
        if (!apiClient.isConfigured()) {
            return;
        }

        log.info("Starting auto-update of rankings...");

        int updated = 0;
        for (String comp : AUTO_UPDATE_COMPETITIONS) {
            try {
                // Rate limiting: wait 7 seconds between calls
                if (updated > 0) {
                    Thread.sleep(7000);
                }

                List<Club> clubs = apiClient.getStandings(comp);
                if (!clubs.isEmpty()) {
                    for (Club club : clubs) {
                        rankingService.addOrUpdateClub(club);
                    }
                    lastUpdates.put(comp, LocalDateTime.now());
                    updated++;
                    log.info("Updated {} clubs from {}", clubs.size(), comp);
                }
            } catch (Exception e) {
                log.warn("Failed to update {}: {}", comp, e.getMessage());
            }
        }

        log.info("Auto-update complete. Updated {} competitions.", updated);
    }

    /**
     * Update a specific competition on demand.
     */
    public int updateCompetition(String competitionCode) {
        if (!apiClient.isConfigured()) {
            return 0;
        }

        List<Club> clubs = apiClient.getStandings(competitionCode);
        if (!clubs.isEmpty()) {
            for (Club club : clubs) {
                rankingService.addOrUpdateClub(club);
            }
            lastUpdates.put(competitionCode, LocalDateTime.now());
            log.info("Manual update: {} clubs from {}", clubs.size(), competitionCode);
        }
        return clubs.size();
    }

    /**
     * Get last update time for a competition.
     */
    public LocalDateTime getLastUpdate(String competitionCode) {
        return lastUpdates.get(competitionCode);
    }

    /**
     * Get all last update times.
     */
    public Map<String, LocalDateTime> getAllLastUpdates() {
        return Map.copyOf(lastUpdates);
    }
}
