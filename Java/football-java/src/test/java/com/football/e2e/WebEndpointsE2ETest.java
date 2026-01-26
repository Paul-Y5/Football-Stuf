package com.football.e2e;

import com.football.config.TestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end tests for web endpoints using MockMvc.
 * Uses test profile to disable scheduled tasks and external API calls.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
class WebEndpointsE2ETest {

    @Autowired
    private MockMvc mockMvc;

    // === Rankings Endpoints ===

    @Test
    @DisplayName("Should access rankings page")
    void shouldAccessRankingsPage() throws Exception {
        mockMvc.perform(get("/rankings"))
                .andExpect(status().isOk())
                .andExpect(view().name("rankings"))
                .andExpect(model().attributeExists("clubs", "countries", "globalStats"));
    }

    @Test
    @DisplayName("Should filter rankings by country")
    void shouldFilterByCountry() throws Exception {
        mockMvc.perform(get("/rankings").param("country", "England"))
                .andExpect(status().isOk())
                .andExpect(view().name("rankings"))
                .andExpect(model().attributeExists("selectedCountry"));
    }

    @Test
    @DisplayName("Should search rankings by name")
    void shouldSearchByName() throws Exception {
        mockMvc.perform(get("/rankings").param("search", "Madrid"))
                .andExpect(status().isOk())
                .andExpect(view().name("rankings"))
                .andExpect(model().attributeExists("searchQuery"));
    }

    @Test
    @DisplayName("Should return JSON from rankings API")
    void shouldReturnJsonFromApi() throws Exception {
        mockMvc.perform(get("/rankings/api/clubs").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    // === Draw Endpoints ===

    @Test
    @DisplayName("Should access draw page")
    void shouldAccessDrawPage() throws Exception {
        mockMvc.perform(get("/draw"))
                .andExpect(status().isOk())
                .andExpect(view().name("draw"))
                .andExpect(model().attributeExists("competitions"));
    }

    @Test
    @DisplayName("Should execute draw and redirect to result")
    void shouldExecuteDrawAndRedirect() throws Exception {
        mockMvc.perform(post("/draw/execute")
                .param("competition", "CHAMPIONS_LEAGUE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/draw/result/*"));
    }

    @Test
    @DisplayName("Should execute draw with seed for reproducibility")
    void shouldExecuteDrawWithSeed() throws Exception {
        mockMvc.perform(post("/draw/execute")
                .param("competition", "CHAMPIONS_LEAGUE")
                .param("seed", "12345"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Should return JSON from draw API")
    void shouldReturnJsonFromDrawApi() throws Exception {
        mockMvc.perform(post("/draw/api/execute")
                .param("competition", "EUROPA_LEAGUE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.competition").value("Europa League"))
                .andExpect(jsonPath("$.valid").value(true));
    }

    // === Home Redirect ===

    @Test
    @DisplayName("Should redirect home to rankings")
    void shouldRedirectHomeToRankings() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/rankings"));
    }
}
