package com.football.controller.api;

import com.football.model.Club;
import com.football.model.TrendDirection;
import com.football.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for club rankings.
 */
@RestController
@RequestMapping("/api/v1/rankings")
@Tag(name = "Rankings", description = "Club rankings and statistics API")
public class RankingApiController {

    private final RankingService rankingService;

    public RankingApiController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @Operation(summary = "Get all clubs", description = "Retrieve all clubs with optional pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved clubs"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    @GetMapping
    @Cacheable("rankings")
    public ResponseEntity<Page<Club>> getAllClubs(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "50") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "ranking") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") String direction
    ) {
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<Club> clubs = rankingService.getAllClubs(pageable);
        return ResponseEntity.ok(clubs);
    }

    @Operation(summary = "Get club by ID", description = "Retrieve a specific club by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Club found", 
                    content = @Content(schema = @Schema(implementation = Club.class))),
        @ApiResponse(responseCode = "404", description = "Club not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Club> getClubById(
            @Parameter(description = "Club ID") @PathVariable Long id
    ) {
        return rankingService.getClubById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get top clubs", description = "Retrieve top N clubs globally")
    @GetMapping("/top/{limit}")
    @Cacheable("topClubs")
    public ResponseEntity<List<Club>> getTopClubs(
            @Parameter(description = "Number of clubs to return") @PathVariable int limit
    ) {
        List<Club> topClubs = rankingService.getTopClubs(limit);
        return ResponseEntity.ok(topClubs);
    }

    @Operation(summary = "Get clubs by country", description = "Retrieve all clubs from a specific country")
    @GetMapping("/country/{country}")
    @Cacheable(value = "clubsByCountry", key = "#country")
    public ResponseEntity<List<Club>> getClubsByCountry(
            @Parameter(description = "Country name") @PathVariable String country
    ) {
        List<Club> clubs = rankingService.getClubsByCountry(country);
        if (clubs.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(clubs);
    }

    @Operation(summary = "Search clubs", description = "Search clubs by name or country")
    @GetMapping("/search")
    public ResponseEntity<List<Club>> searchClubs(
            @Parameter(description = "Search query") @RequestParam String query
    ) {
        List<Club> results = rankingService.searchClubs(query);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Get distinct countries", description = "Get list of all countries with clubs")
    @GetMapping("/countries")
    @Cacheable("countries")
    public ResponseEntity<List<String>> getCountries() {
        List<String> countries = rankingService.getDistinctCountries();
        return ResponseEntity.ok(countries);
    }

    @Operation(summary = "Get country statistics", description = "Get aggregated statistics for a country")
    @GetMapping("/countries/{country}/stats")
    @Cacheable(value = "countryStats", key = "#country")
    public ResponseEntity<Map<String, Object>> getCountryStats(
            @Parameter(description = "Country name") @PathVariable String country
    ) {
        Map<String, Object> stats = rankingService.getCountryStatistics(country);
        if (stats.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get improved clubs", description = "Get clubs with upward trend")
    @GetMapping("/trends/up")
    @Cacheable("improvedClubs")
    public ResponseEntity<List<Club>> getImprovedClubs() {
        List<Club> clubs = rankingService.getClubsByTrend(TrendDirection.UP);
        return ResponseEntity.ok(clubs);
    }

    @Operation(summary = "Get declined clubs", description = "Get clubs with downward trend")
    @GetMapping("/trends/down")
    @Cacheable("declinedClubs")
    public ResponseEntity<List<Club>> getDeclinedClubs() {
        List<Club> clubs = rankingService.getClubsByTrend(TrendDirection.DOWN);
        return ResponseEntity.ok(clubs);
    }

    @Operation(summary = "Refresh rankings", description = "Force refresh rankings from external API")
    @ApiResponse(responseCode = "200", description = "Rankings refreshed successfully")
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshRankings() {
        rankingService.refreshRankings();
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Rankings refresh initiated"
        ));
    }
}
