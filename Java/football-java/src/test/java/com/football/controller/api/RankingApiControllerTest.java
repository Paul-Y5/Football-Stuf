package com.football.controller.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.football.model.Club;
import com.football.model.TrendDirection;
import com.football.service.RankingService;

/**
 * Tests for RankingApiController.
 */
@WebMvcTest(RankingApiController.class)
class RankingApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RankingService rankingService;

    @Test
    void whenGetAllClubs_thenReturnPageOfClubs() throws Exception {
        // given
        Club club1 = createClub(1L, "Barcelona", "Spain", 1, 2000);
        Club club2 = createClub(2L, "Real Madrid", "Spain", 2, 1950);
        Page<Club> page = new PageImpl<>(List.of(club1, club2));
        
        when(rankingService.getAllClubs(any(PageRequest.class))).thenReturn(page);

        // when & then
        mockMvc.perform(get("/api/v1/rankings")
                .param("page", "0")
                .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name", is("Barcelona")))
                .andExpect(jsonPath("$.content[1].name", is("Real Madrid")));
    }

    @Test
    void whenGetClubById_withExistingId_thenReturnClub() throws Exception {
        // given
        Club club = createClub(1L, "Barcelona", "Spain", 1, 2000);
        when(rankingService.getClubById(1L)).thenReturn(Optional.of(club));

        // when & then
        mockMvc.perform(get("/api/v1/rankings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Barcelona")))
                .andExpect(jsonPath("$.country", is("Spain")))
                .andExpect(jsonPath("$.ranking", is(1)));
    }

    @Test
    void whenGetClubById_withNonExistingId_thenReturn404() throws Exception {
        // given
        when(rankingService.getClubById(999L)).thenReturn(Optional.empty());

        // when & then
        mockMvc.perform(get("/api/v1/rankings/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenGetTopClubs_thenReturnLimitedList() throws Exception {
        // given
        Club club1 = createClub(1L, "Barcelona", "Spain", 1, 2000);
        Club club2 = createClub(2L, "Real Madrid", "Spain", 2, 1950);
        when(rankingService.getTopClubs(10)).thenReturn(List.of(club1, club2));

        // when & then
        mockMvc.perform(get("/api/v1/rankings/top/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].ranking", is(1)))
                .andExpect(jsonPath("$[1].ranking", is(2)));
    }

    @Test
    void whenGetClubsByCountry_withExistingCountry_thenReturnClubs() throws Exception {
        // given
        Club club1 = createClub(1L, "Barcelona", "Spain", 1, 2000);
        Club club2 = createClub(2L, "Real Madrid", "Spain", 2, 1950);
        when(rankingService.getClubsByCountry("Spain")).thenReturn(List.of(club1, club2));

        // when & then
        mockMvc.perform(get("/api/v1/rankings/country/Spain"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].country", is("Spain")))
                .andExpect(jsonPath("$[1].country", is("Spain")));
    }

    @Test
    void whenGetClubsByCountry_withNonExistingCountry_thenReturn404() throws Exception {
        // given
        when(rankingService.getClubsByCountry("Unknown")).thenReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/v1/rankings/country/Unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenSearchClubs_thenReturnMatchingClubs() throws Exception {
        // given
        Club club = createClub(1L, "Barcelona", "Spain", 1, 2000);
        when(rankingService.searchClubs("Barcelona")).thenReturn(List.of(club));

        // when & then
        mockMvc.perform(get("/api/v1/rankings/search")
                .param("query", "Barcelona"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Barcelona")));
    }

    @Test
    void whenGetCountries_thenReturnList() throws Exception {
        // given
        when(rankingService.getDistinctCountries()).thenReturn(List.of("Spain", "England", "Germany"));

        // when & then
        mockMvc.perform(get("/api/v1/rankings/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$", containsInAnyOrder("Spain", "England", "Germany")));
    }

    @Test
    void whenGetCountryStats_withExistingCountry_thenReturnStats() throws Exception {
        // given
        Map<String, Object> stats = Map.of(
            "country", "Spain",
            "totalClubs", 2,
            "avgPoints", 1975.0
        );
        when(rankingService.getCountryStatistics("Spain")).thenReturn(stats);

        // when & then
        mockMvc.perform(get("/api/v1/rankings/countries/Spain/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.country", is("Spain")))
                .andExpect(jsonPath("$.totalClubs", is(2)))
                .andExpect(jsonPath("$.avgPoints", is(1975.0)));
    }

    @Test
    void whenGetCountryStats_withNonExistingCountry_thenReturn404() throws Exception {
        // given
        when(rankingService.getCountryStatistics("Unknown")).thenReturn(Map.of());

        // when & then
        mockMvc.perform(get("/api/v1/rankings/countries/Unknown/stats"))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenGetImprovedClubs_thenReturnClubsWithUpTrend() throws Exception {
        // given
        Club club = createClub(1L, "Barcelona", "Spain", 1, 2000);
        club.setTrend(TrendDirection.UP);
        when(rankingService.getClubsByTrend(TrendDirection.UP)).thenReturn(List.of(club));

        // when & then
        mockMvc.perform(get("/api/v1/rankings/trends/up"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].trend", is("UP")));
    }

    @Test
    void whenGetDeclinedClubs_thenReturnClubsWithDownTrend() throws Exception {
        // given
        Club club = createClub(1L, "Manchester City", "England", 3, 1900);
        club.setTrend(TrendDirection.DOWN);
        when(rankingService.getClubsByTrend(TrendDirection.DOWN)).thenReturn(List.of(club));

        // when & then
        mockMvc.perform(get("/api/v1/rankings/trends/down"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].trend", is("DOWN")));
    }

    @Test
    void whenRefreshRankings_thenReturnSuccess() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/rankings/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("Rankings refresh initiated")));
    }

    private Club createClub(Long id, String name, String country, int ranking, int points) {
        Club club = new Club();
        club.setId(id);
        club.setName(name);
        club.setCountry(country);
        club.setRanking(ranking);
        club.setPoints(points);
        club.setPreviousPoints(points - 50);
        club.setTrend(TrendDirection.STABLE);
        return club;
    }
}
