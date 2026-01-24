package com.football.integration;

import com.football.model.Club;
import com.football.service.RankingService;
import com.opencsv.exceptions.CsvException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for RankingService with CSV loading.
 */
@SpringBootTest
class RankingServiceIT {

    @Autowired
    private RankingService rankingService;

    @Test
    @DisplayName("Should load sample rankings from CSV")
    void shouldLoadSampleRankings() throws IOException, CsvException {
        List<Club> clubs = rankingService.loadFromResource("/data/rankings.csv");

        assertThat(clubs).isNotEmpty();
        assertThat(clubs).hasSizeGreaterThanOrEqualTo(50);

        // Verify first club
        Club first = clubs.get(0);
        assertThat(first.getRanking()).isEqualTo(1);
        assertThat(first.getName()).isEqualTo("Real Madrid");
        assertThat(first.getCountry()).isEqualTo("Spain");
    }

    @Test
    @DisplayName("Should filter clubs by country")
    void shouldFilterByCountry() throws IOException, CsvException {
        rankingService.loadFromResource("/data/rankings.csv");

        List<Club> germanClubs = rankingService.getClubsByCountry("Germany");

        assertThat(germanClubs).isNotEmpty();
        assertThat(germanClubs).allMatch(c -> c.getCountry().equals("Germany"));
    }

    @Test
    @DisplayName("Should search clubs by name")
    void shouldSearchByName() throws IOException, CsvException {
        rankingService.loadFromResource("/data/rankings.csv");

        List<Club> results = rankingService.searchByName("Madrid");

        assertThat(results).hasSizeGreaterThanOrEqualTo(2);
        assertThat(results).anyMatch(c -> c.getName().contains("Real Madrid"));
        assertThat(results).anyMatch(c -> c.getName().contains("Atletico Madrid"));
    }

    @Test
    @DisplayName("Should build country statistics")
    void shouldBuildCountryStats() throws IOException, CsvException {
        rankingService.loadFromResource("/data/rankings.csv");

        var stats = rankingService.getCountryStats("England");

        assertThat(stats).isNotNull();
        assertThat(stats.getTotalClubs()).isGreaterThanOrEqualTo(5);
        assertThat(stats.getAvgRanking()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should get all countries")
    void shouldGetAllCountries() throws IOException, CsvException {
        rankingService.loadFromResource("/data/rankings.csv");

        List<String> countries = rankingService.getAllCountries();

        assertThat(countries).contains("England", "Spain", "Germany", "Italy", "France");
    }
}
