package com.football.controller;

import com.football.api.FootballDataApiClient;
import com.football.model.Club;
import com.football.model.Competition;
import com.football.model.DrawResult;
import com.football.service.DrawSimulatorService;
import com.football.service.MatchSimulationService;
import com.football.service.MatchSimulationService.SimulationResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller for Draw Simulator.
 */
@Controller
@RequestMapping("/draw")
public class DrawController {

    private final DrawSimulatorService drawService;
    private final MatchSimulationService simulationService;
    private final FootballDataApiClient apiClient;

    // Store draw and simulation results
    private final Map<String, DrawResult> drawResults = new ConcurrentHashMap<>();
    private final Map<String, SimulationResult> simulationResults = new ConcurrentHashMap<>();

    public DrawController(DrawSimulatorService drawService,
            MatchSimulationService simulationService,
            FootballDataApiClient apiClient) {
        this.drawService = drawService;
        this.simulationService = simulationService;
        this.apiClient = apiClient;
    }

    /**
     * Draw configuration page.
     */
    @GetMapping
    public String drawPage(Model model) {
        model.addAttribute("competitions", Competition.values());
        model.addAttribute("apiConfigured", apiClient.isConfigured());
        return "draw";
    }

    /**
     * Execute a draw.
     */
    @PostMapping("/execute")
    public String executeDraw(
            @RequestParam Competition competition,
            @RequestParam(required = false) Long seed,
            @RequestParam(defaultValue = "false") boolean simulate,
            Model model) {

        // Get teams from API or use defaults
        List<Club> clubs = getTeamsForCompetition(competition);

        if (clubs.size() < 4) {
            model.addAttribute("error", "Not enough teams to perform a draw.");
            model.addAttribute("competitions", Competition.values());
            return "draw";
        }

        // Assign pots (1-4 based on position)
        assignPots(clubs);

        // Execute draw
        DrawResult result = drawService.executeDraw(competition, clubs, seed);
        drawResults.put(result.getId(), result);

        // Simulate matches if requested
        if (simulate && result.isValid()) {
            SimulationResult simResult = simulationService.simulateCompetition(result, seed);
            simulationResults.put(result.getId(), simResult);
        }

        return "redirect:/draw/result/" + result.getId();
    }

    /**
     * View draw result.
     */
    @GetMapping("/result/{id}")
    public String viewResult(@PathVariable String id, Model model) {
        DrawResult result = drawResults.get(id);

        if (result == null) {
            return "redirect:/draw";
        }

        SimulationResult simResult = simulationResults.get(id);

        model.addAttribute("result", result);
        model.addAttribute("clubsByPot", result.getClubsByPot());
        model.addAttribute("errors", drawService.validateDraw(result));
        model.addAttribute("simulation", simResult);
        model.addAttribute("hasSimulation", simResult != null);

        return "draw-result";
    }

    /**
     * Simulate matches for existing draw.
     */
    @PostMapping("/simulate/{id}")
    public String simulateMatches(@PathVariable String id, @RequestParam(required = false) Long seed) {
        DrawResult result = drawResults.get(id);

        if (result != null && result.isValid()) {
            SimulationResult simResult = simulationService.simulateCompetition(result, seed);
            simulationResults.put(id, simResult);
        }

        return "redirect:/draw/result/" + id;
    }

    /**
     * View standings page.
     */
    @GetMapping("/standings/{id}")
    public String viewStandings(@PathVariable String id, Model model) {
        DrawResult drawResult = drawResults.get(id);
        SimulationResult simResult = simulationResults.get(id);

        if (drawResult == null || simResult == null) {
            return "redirect:/draw";
        }

        model.addAttribute("result", drawResult);
        model.addAttribute("simulation", simResult);
        model.addAttribute("standings", simResult.standings());
        model.addAttribute("top8", simResult.getTop8());
        model.addAttribute("playoffs", simResult.getPlayoffPositions());
        model.addAttribute("eliminated", simResult.getEliminatedPositions());
        model.addAttribute("matchdays", simResult.matchdays());

        return "standings";
    }

    /**
     * API endpoint: Execute draw and return JSON.
     */
    @PostMapping("/api/execute")
    @ResponseBody
    public Map<String, Object> executeDrawJson(
            @RequestParam Competition competition,
            @RequestParam(required = false) Long seed,
            @RequestParam(defaultValue = "false") boolean simulate) {

        List<Club> clubs = getTeamsForCompetition(competition);
        assignPots(clubs);

        DrawResult result = drawService.executeDraw(competition, clubs, seed);
        drawResults.put(result.getId(), result);

        Map<String, Object> response = result.toMap();

        if (simulate && result.isValid()) {
            SimulationResult simResult = simulationService.simulateCompetition(result, seed);
            simulationResults.put(result.getId(), simResult);
            response.put("simulation", formatSimulationForJson(simResult));
        }

        return response;
    }

    /**
     * API endpoint: Get draw result.
     */
    @GetMapping("/api/result/{id}")
    @ResponseBody
    public Map<String, Object> getResultJson(@PathVariable String id) {
        DrawResult result = drawResults.get(id);
        if (result == null) {
            return Map.of("error", "Draw not found");
        }

        Map<String, Object> response = new HashMap<>(result.toMap());

        SimulationResult simResult = simulationResults.get(id);
        if (simResult != null) {
            response.put("simulation", formatSimulationForJson(simResult));
        }

        return response;
    }

    private Map<String, Object> formatSimulationForJson(SimulationResult sim) {
        Map<String, Object> map = new HashMap<>();
        map.put("totalMatches", sim.getTotalMatches());
        map.put("totalGoals", sim.getTotalGoals());
        map.put("avgGoalsPerMatch", sim.getAvgGoalsPerMatch());

        List<Map<String, Object>> standingsList = new ArrayList<>();
        int pos = 1;
        for (var entry : sim.standings()) {
            Map<String, Object> e = new HashMap<>();
            e.put("position", pos++);
            e.put("club", entry.getClub().getName());
            e.put("played", entry.getPlayed());
            e.put("won", entry.getWon());
            e.put("drawn", entry.getDrawn());
            e.put("lost", entry.getLost());
            e.put("gf", entry.getGoalsFor());
            e.put("ga", entry.getGoalsAgainst());
            e.put("gd", entry.getGoalDifference());
            e.put("points", entry.getPoints());
            standingsList.add(e);
        }
        map.put("standings", standingsList);

        List<Map<String, Object>> resultsList = new ArrayList<>();
        for (var result : sim.results()) {
            Map<String, Object> r = new HashMap<>();
            r.put("home", result.getHome().getName());
            r.put("away", result.getAway().getName());
            r.put("homeGoals", result.getHomeGoals());
            r.put("awayGoals", result.getAwayGoals());
            r.put("matchday", result.getMatchday());
            resultsList.add(r);
        }
        map.put("results", resultsList);

        return map;
    }

    private List<Club> getTeamsForCompetition(Competition competition) {
        if (apiClient.isConfigured()) {
            List<Club> teams = apiClient.getTeams(competition.getCode());
            if (!teams.isEmpty()) {
                return teams;
            }
        }
        // Sample data only used when API is not configured (for testing/demo)
        return getSampleTeamsForDemo();
    }

    private void assignPots(List<Club> clubs) {
        clubs.sort(Comparator.comparingDouble(Club::getCoefficient).reversed()
                .thenComparingInt(Club::getRanking));

        int teamsPerPot = Math.max(1, clubs.size() / 4);
        for (int i = 0; i < clubs.size(); i++) {
            int pot = Math.min(4, (i / teamsPerPot) + 1);
            clubs.get(i).setPot(pot);
        }
    }

    /**
     * Sample teams for demo/testing when API is not configured.
     */
    private List<Club> getSampleTeamsForDemo() {
        List<Club> teams = new ArrayList<>();

        String[][] sampleData = {
                { "Real Madrid", "Spain", "https://crests.football-data.org/86.png", "136" },
                { "Manchester City", "England", "https://crests.football-data.org/65.png", "128" },
                { "Bayern Munich", "Germany", "https://crests.football-data.org/5.png", "124" },
                { "Paris Saint-Germain", "France", "https://crests.football-data.org/524.png", "104" },
                { "Liverpool", "England", "https://crests.football-data.org/64.png", "99" },
                { "Inter Milan", "Italy", "https://crests.football-data.org/108.png", "98" },
                { "Borussia Dortmund", "Germany", "https://crests.football-data.org/4.png", "91" },
                { "Barcelona", "Spain", "https://crests.football-data.org/81.png", "88" },
                { "Leverkusen", "Germany", "https://crests.football-data.org/3.png", "80" },
                { "Atletico Madrid", "Spain", "https://crests.football-data.org/78.png", "78" },
                { "Juventus", "Italy", "https://crests.football-data.org/109.png", "76" },
                { "Benfica", "Portugal", "https://crests.football-data.org/1903.png", "75" },
                { "Arsenal", "England", "https://crests.football-data.org/57.png", "72" },
                { "AC Milan", "Italy", "https://crests.football-data.org/98.png", "60" },
                { "Porto", "Portugal", "https://crests.football-data.org/503.png", "53" },
                { "Napoli", "Italy", "https://crests.football-data.org/113.png", "58" },
        };

        for (int i = 0; i < sampleData.length; i++) {
            Club club = new Club();
            club.setId((long) (i + 1));
            club.setName(sampleData[i][0]);
            club.setShortName(sampleData[i][0].length() > 12
                    ? sampleData[i][0].substring(0, 10)
                    : sampleData[i][0]);
            club.setCountry(sampleData[i][1]);
            club.setCrestUrl(sampleData[i][2]);
            club.setCoefficient(Double.parseDouble(sampleData[i][3]));
            club.setRanking(i + 1);
            teams.add(club);
        }

        return teams;
    }
}
