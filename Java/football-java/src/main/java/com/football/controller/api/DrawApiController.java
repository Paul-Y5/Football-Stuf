package com.football.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.football.model.Club;
import com.football.model.Competition;
import com.football.model.DrawResult;
import com.football.service.DrawSimulatorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST API for UEFA draw simulation.
 */
@RestController
@RequestMapping("/api/v1/draw")
@Tag(name = "Draw Simulator", description = "UEFA draw simulation API")
public class DrawApiController {

    private final DrawSimulatorService drawService;

    public DrawApiController(DrawSimulatorService drawService) {
        this.drawService = drawService;
    }

    @Operation(summary = "Simulate draw", description = "Simulate UEFA competition draw with provided teams")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Draw completed successfully",
                    content = @Content(schema = @Schema(implementation = DrawResult.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input or draw impossible"),
        @ApiResponse(responseCode = "500", description = "Draw simulation failed")
    })
    @PostMapping("/simulate")
    public ResponseEntity<?> simulateDraw(
            @Parameter(description = "Competition type") 
            @RequestParam Competition competition,
            @Parameter(description = "List of clubs to include in draw")
            @RequestBody List<Club> clubs
    ) {
        try {
            DrawResult result = drawService.simulateDraw(clubs, competition);
            
            if (result.isValid()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
        .body(new DrawResult(competition, clubs));
        }
    }

    @Operation(summary = "Simulate Champions League", 
               description = "Simulate Champions League draw with top 36 clubs")
    @PostMapping("/champions-league")
    public ResponseEntity<DrawResult> simulateChampionsLeague() {
        List<Club> topClubs = drawService.getTopClubsForDraw(36);
        DrawResult result = drawService.simulateDraw(topClubs, Competition.CHAMPIONS_LEAGUE);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Simulate Europa League",
               description = "Simulate Europa League draw")
    @PostMapping("/europa-league")
    public ResponseEntity<DrawResult> simulateEuropaLeague(
            @Parameter(description = "Number of teams (default: 36)")
            @RequestParam(defaultValue = "36") int teamCount
    ) {
        List<Club> clubs = drawService.getTopClubsForDraw(teamCount);
        DrawResult result = drawService.simulateDraw(clubs, Competition.EUROPA_LEAGUE);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Simulate Conference League",
               description = "Simulate Conference League draw")
    @PostMapping("/conference-league")
    public ResponseEntity<DrawResult> simulateConferenceLeague(
            @Parameter(description = "Number of teams (default: 36)")
            @RequestParam(defaultValue = "36") int teamCount
    ) {
        List<Club> clubs = drawService.getTopClubsForDraw(teamCount);
        DrawResult result = drawService.simulateDraw(clubs, Competition.CONFERENCE_LEAGUE);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get eligible clubs for draw",
               description = "Get top N clubs eligible for competition")
    @GetMapping("/eligible-clubs")
    public ResponseEntity<List<Club>> getEligibleClubs(
            @Parameter(description = "Number of clubs to return")
            @RequestParam(defaultValue = "36") int count
    ) {
        List<Club> clubs = drawService.getTopClubsForDraw(count);
        return ResponseEntity.ok(clubs);
    }
}
