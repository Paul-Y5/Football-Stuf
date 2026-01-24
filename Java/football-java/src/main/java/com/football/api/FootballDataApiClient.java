package com.football.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.football.model.Club;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Client for football-data.org API.
 * 
 * Free tier: 10 calls/minute
 * Endpoints used:
 * - GET /v4/competitions/{code}/standings
 * - GET /v4/competitions/{code}/teams
 */
@Component
public class FootballDataApiClient {

    private static final Logger log = LoggerFactory.getLogger(FootballDataApiClient.class);
    private static final String BASE_URL = "https://api.football-data.org/v4";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${football.api.key:}")
    private String apiKey;

    public FootballDataApiClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Fetch standings for a competition.
     * 
     * @param competitionCode e.g., "PL" (Premier League), "CL" (Champions League)
     * @return List of clubs with updated rankings
     */
    public List<Club> getStandings(String competitionCode) {
        String url = BASE_URL + "/competitions/" + competitionCode + "/standings";

        try {
            ResponseEntity<String> response = makeRequest(url);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseStandings(response.getBody());
            }
        } catch (Exception e) {
            log.error("Error fetching standings for {}: {}", competitionCode, e.getMessage());
        }

        return List.of();
    }

    /**
     * Fetch teams for a competition.
     */
    public List<Club> getTeams(String competitionCode) {
        String url = BASE_URL + "/competitions/" + competitionCode + "/teams";

        try {
            ResponseEntity<String> response = makeRequest(url);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseTeams(response.getBody());
            }
        } catch (Exception e) {
            log.error("Error fetching teams for {}: {}", competitionCode, e.getMessage());
        }

        return List.of();
    }

    private ResponseEntity<String> makeRequest(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        if (apiKey != null && !apiKey.isEmpty()) {
            headers.set("X-Auth-Token", apiKey);
        }

        HttpEntity<String> entity = new HttpEntity<>(headers);

        log.debug("Making request to: {}", url);
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    private List<Club> parseStandings(String json) throws Exception {
        List<Club> clubs = new ArrayList<>();
        JsonNode root = objectMapper.readTree(json);

        JsonNode standings = root.path("standings");
        if (standings.isArray() && !standings.isEmpty()) {
            // Get TOTAL standings (not home/away)
            JsonNode totalStandings = standings.get(0);
            JsonNode table = totalStandings.path("table");

            for (JsonNode teamNode : table) {
                Club club = new Club();
                club.setRanking(teamNode.path("position").asInt());

                JsonNode teamInfo = teamNode.path("team");
                club.setId((long) teamInfo.path("id").asInt());
                club.setName(teamInfo.path("name").asText());
                club.setShortName(teamInfo.path("shortName").asText());
                club.setCrestUrl(teamInfo.path("crest").asText());

                club.setPoints(teamNode.path("points").asInt());

                // Parse form to determine trend
                String form = teamNode.path("form").asText("");
                club.setTrend(parseFormToTrend(form));

                clubs.add(club);
            }
        }

        // Get area/country from competition info
        String country = root.path("area").path("name").asText("Unknown");
        for (Club club : clubs) {
            club.setCountry(country);
            club.setCountryCode(root.path("area").path("code").asText());
        }

        return clubs;
    }

    private List<Club> parseTeams(String json) throws Exception {
        List<Club> clubs = new ArrayList<>();
        JsonNode root = objectMapper.readTree(json);

        JsonNode teams = root.path("teams");
        if (teams.isArray()) {
            int rank = 1;
            for (JsonNode teamNode : teams) {
                Club club = new Club();
                club.setId((long) teamNode.path("id").asInt());
                club.setName(teamNode.path("name").asText());
                club.setShortName(teamNode.path("shortName").asText());
                club.setCrestUrl(teamNode.path("crest").asText());
                club.setRanking(rank++);

                // Area info
                JsonNode area = teamNode.path("area");
                club.setCountry(area.path("name").asText());
                club.setCountryCode(area.path("code").asText());

                clubs.add(club);
            }
        }

        return clubs;
    }

    private com.football.model.TrendDirection parseFormToTrend(String form) {
        if (form == null || form.isEmpty()) {
            return com.football.model.TrendDirection.STABLE;
        }

        // Form is like "W,D,W,L,W"
        String[] results = form.split(",");
        if (results.length >= 2) {
            // Compare recent form
            int recent = countWins(results, 0, Math.min(2, results.length));
            int older = countWins(results, 2, Math.min(4, results.length));

            if (recent > older)
                return com.football.model.TrendDirection.UP;
            if (recent < older)
                return com.football.model.TrendDirection.DOWN;
        }
        return com.football.model.TrendDirection.STABLE;
    }

    private int countWins(String[] results, int start, int end) {
        int wins = 0;
        for (int i = start; i < end && i < results.length; i++) {
            if ("W".equals(results[i].trim()))
                wins++;
        }
        return wins;
    }

    /**
     * Check if API key is configured.
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }

    /**
     * Set API key programmatically.
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
