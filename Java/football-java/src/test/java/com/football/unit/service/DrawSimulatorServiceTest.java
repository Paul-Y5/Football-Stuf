package com.football.unit.service;

import com.football.model.Club;
import com.football.model.Competition;
import com.football.model.DrawResult;
import com.football.service.DrawSimulatorService;
import com.football.service.RankingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DrawSimulatorService.
 */
@ExtendWith(MockitoExtension.class)
class DrawSimulatorServiceTest {

    @Mock
    private RankingService rankingService;
    private DrawSimulatorService service;
    private List<Club> clubs;

    @BeforeEach
    void setUp() {
        service = new DrawSimulatorService(rankingService);
        clubs = createTestClubs();
    }

    private List<Club> createTestClubs() {
        List<Club> result = new ArrayList<>();
        String[] countries = { "England", "Spain", "Germany", "Italy", "France", "Portugal", "Netherlands", "Belgium" };

        for (int i = 0; i < 16; i++) {
            Club club = new Club();
            club.setId((long) (i + 1));
            club.setName("Club" + (i + 1));
            club.setCountry(countries[i % countries.length]);
            club.setRanking(i + 1);
            club.setCoefficient(100.0 - i * 5);
            club.setPot((i / 4) + 1);
            result.add(club);
        }

        return result;
    }

    @Test
    @DisplayName("Should execute a valid draw")
    void shouldExecuteValidDraw() {
        DrawResult result = service.executeDraw(Competition.CHAMPIONS_LEAGUE, clubs, 12345L);

        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getClubs()).hasSize(16);
    }

    @Test
    @DisplayName("Should assign correct number of home and away matches")
    void shouldAssignCorrectMatchCounts() {
        DrawResult result = service.executeDraw(Competition.CHAMPIONS_LEAGUE, clubs, 42L);

        for (Club club : result.getClubs()) {
            assertThat(club.getHomeOpponents()).hasSize(Competition.CHAMPIONS_LEAGUE.getHomeMatches());
            assertThat(club.getAwayOpponents()).hasSize(Competition.CHAMPIONS_LEAGUE.getAwayMatches());
        }
    }

    @Test
    @DisplayName("Should not have same-country opponents")
    void shouldNotHaveSameCountryOpponents() {
        DrawResult result = service.executeDraw(Competition.CHAMPIONS_LEAGUE, clubs, 123L);

        for (Club club : result.getClubs()) {
            for (Club opponent : club.getAllOpponents()) {
                assertThat(opponent.getCountry())
                        .as("Club %s should not face same-country opponent %s", club.getName(), opponent.getName())
                        .isNotEqualTo(club.getCountry());
            }
        }
    }

    @Test
    @DisplayName("Should not exceed max same country limit")
    void shouldNotExceedMaxSameCountry() {
        DrawResult result = service.executeDraw(Competition.CHAMPIONS_LEAGUE, clubs, 456L);

        for (Club club : result.getClubs()) {
            for (Integer count : club.getOpponentCountries().values()) {
                assertThat(count)
                        .as("Club %s should not have more than 2 opponents from same country", club.getName())
                        .isLessThanOrEqualTo(2);
            }
        }
    }

    @RepeatedTest(5)
    @DisplayName("Should produce valid draws with different seeds")
    void shouldProduceValidDrawsWithRandomSeeds() {
        DrawResult result = service.executeDraw(Competition.CHAMPIONS_LEAGUE, clubs);

        List<String> errors = service.validateDraw(result);

        assertThat(result.isValid()).isTrue();
        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("Should produce reproducible draws with same seed")
    void shouldProduceReproducibleDraws() {
        long seed = 42L;

        DrawResult result1 = service.executeDraw(Competition.CHAMPIONS_LEAGUE, new ArrayList<>(clubs), seed);

        // Reset clubs
        for (Club club : clubs) {
            club.resetDraw();
        }

        DrawResult result2 = service.executeDraw(Competition.CHAMPIONS_LEAGUE, new ArrayList<>(clubs), seed);

        // Results should match
        for (int i = 0; i < result1.getClubs().size(); i++) {
            Club club1 = result1.getClubs().get(i);
            Club club2 = result2.getClubs().get(i);

            assertThat(club1.getHomeOpponents().stream().map(Club::getName).toList())
                    .isEqualTo(club2.getHomeOpponents().stream().map(Club::getName).toList());
        }
    }

    @Test
    @DisplayName("Should validate draw correctly")
    void shouldValidateDrawCorrectly() {
        DrawResult result = service.executeDraw(Competition.CHAMPIONS_LEAGUE, clubs, 789L);

        List<String> errors = service.validateDraw(result);

        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("Should track backtrack count")
    void shouldTrackBacktrackCount() {
        DrawResult result = service.executeDraw(Competition.CHAMPIONS_LEAGUE, clubs, 111L);

        // Backtracking may or may not occur, but count should be >= 0
        assertThat(result.getBacktrackCount()).isGreaterThanOrEqualTo(0);
    }
}
