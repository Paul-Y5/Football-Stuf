package com.football.controller;

import com.football.api.FootballDataApiClient;
import com.football.model.Club;
import com.football.model.CountryStats;
import com.football.service.RankingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Rankings Dashboard.
 */
@Controller
@RequestMapping("/rankings")
public class RankingController {

    private final RankingService rankingService;
    private final FootballDataApiClient apiClient;

    public RankingController(RankingService rankingService, FootballDataApiClient apiClient) {
        this.rankingService = rankingService;
        this.apiClient = apiClient;
    }

    /**
     * Main rankings dashboard.
     */
    @GetMapping
    public String rankings(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "100") int limit,
            Model model) {

        List<Club> clubs;

        if (search != null && !search.isEmpty()) {
            clubs = rankingService.searchByName(search);
        } else if (country != null && !country.isEmpty()) {
            clubs = rankingService.getClubsByCountry(country);
        } else {
            clubs = rankingService.getTopClubs(limit);
        }

        model.addAttribute("clubs", clubs);
        model.addAttribute("countries", rankingService.getAllCountries());
        model.addAttribute("selectedCountry", country);
        model.addAttribute("searchQuery", search);
        model.addAttribute("globalStats", rankingService.getGlobalStats());
        model.addAttribute("apiConfigured", apiClient.isConfigured());

        return "rankings";
    }

    /**
     * Country detail page.
     */
    @GetMapping("/country/{name}")
    public String countryDetail(@PathVariable String name, Model model) {
        CountryStats stats = rankingService.getCountryStats(name);

        if (stats == null) {
            return "redirect:/rankings";
        }

        model.addAttribute("stats", stats);
        model.addAttribute("clubs", stats.getClubs());

        return "country-detail";
    }

    /**
     * Refresh rankings from API.
     */
    @PostMapping("/refresh")
    public String refreshFromApi(@RequestParam(defaultValue = "PL") String competition, Model model) {
        if (!apiClient.isConfigured()) {
            model.addAttribute("error", "API key not configured. Set FOOTBALL_API_KEY environment variable.");
            return "redirect:/rankings";
        }

        List<Club> clubs = apiClient.getStandings(competition);
        if (!clubs.isEmpty()) {
            rankingService.addClubs(clubs);
        }

        return "redirect:/rankings";
    }

    /**
     * API endpoint for JSON data.
     */
    @GetMapping("/api/clubs")
    @ResponseBody
    public List<Club> getClubsJson(
            @RequestParam(required = false) String country,
            @RequestParam(defaultValue = "100") int limit) {

        if (country != null && !country.isEmpty()) {
            return rankingService.getClubsByCountry(country);
        }
        return rankingService.getTopClubs(limit);
    }

    /**
     * API endpoint for country stats.
     */
    @GetMapping("/api/countries")
    @ResponseBody
    public List<CountryStats> getCountryStatsJson() {
        return rankingService.getAllCountryStats();
    }
}
